package com.mbf.wearable.jordanprayertimes.presentation

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewModelScope
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel
import com.mbf.wearable.jordanprayertimes.usecase.LoadInitialHomeScreenDataUseCase
import com.mbf.wearable.jordanprayertimes.usecase.LoadPrayerTimesForCityUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

val LocalAppSharedState =
    compositionLocalOf<MainViewModel?> {
        null
    }

class MainViewModel(
    private val loadInitialHomeScreenDataUseCase: LoadInitialHomeScreenDataUseCase,
    private val loadPrayerTimesForCityUseCase: LoadPrayerTimesForCityUseCase
) : BaseViewModel() {
    private var countdownJob: Job? = null

    private val _countdownFlow = MutableStateFlow("")
    val countdownFlow = _countdownFlow.asStateFlow()

    private val _citySelectedEvent = MutableSharedFlow<Unit>(replay = 0)
    val citySelectedEvent: SharedFlow<Unit> = _citySelectedEvent.asSharedFlow()

    private fun startNextPrayerCountDown() {
        countdownJob?.cancel()

        countdownJob = viewModelScope.launch {
            var remainingTime = _uiState.value.nextPrayTime - System.currentTimeMillis()

            while (remainingTime > 0) {
                val formattedTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    ((remainingTime / 1000) % 3600) / 60,
                    (remainingTime / 1000) % 60
                )
                _countdownFlow.value = "Next prayer in: $formattedTime"
                delay(1000L)
                remainingTime -= 1000L
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState
        .onStart { loadData() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _uiState.value
        )

    @androidx.compose.runtime.Immutable
    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val prayers: List<PrayerUiModel> = emptyList(),
        val currentDate: String = java.text.SimpleDateFormat(
            "EEEE, yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).format(java.util.Date()),
        val nextPray: String = "Ishaa",
        val nextPrayTime: Long = System.currentTimeMillis() + (60 * 60 * 1000L),
        val currentCity: CityUiModel = CityUiModel(name = "Amman", id = 1, isSelected = true),
    )

    fun loadData() {
        actionTrigger(UIAction.LoadInitialData)
    }

    fun actionTrigger(action: UIAction) {
        viewModelScope.launch {
            when (action) {
                is UIAction.SelectCity -> {
                    _uiState.update { it.copy(isLoading = true, currentCity = action.city) }
                    viewModelScope.launch {
                        handleResult(
                            result = loadPrayerTimesForCityUseCase(action.city),
                            onSuccess = { data ->
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        prayers = data.prayers,
                                        nextPray = data.nextPray,
                                        nextPrayTime = data.nextPrayTime
                                    )
                                }
                                viewModelScope.launch {
                                _citySelectedEvent.emit(Unit)

                                }
                                actionTrigger(UIAction.StartNextPrayerCountDown)
                            },
                            onError = { error ->
                                _uiState.update { it.copy(isLoading = false, error = error) }
                                viewModelScope.launch {
                                    _citySelectedEvent.emit(Unit)
                                }
                            }
                        )
                    }
                }

                is UIAction.StartNextPrayerCountDown -> {
                    startNextPrayerCountDown()
                }

                is UIAction.LoadInitialData -> {
                    _uiState.update { it.copy(isLoading = true) }
                    viewModelScope.launch {
                        handleResult(
                            result = loadInitialHomeScreenDataUseCase(),
                            onSuccess = { data ->
                                _uiState.update { uiStates ->
                                    uiStates.copy(
                                        isLoading = false,
                                        currentCity = data.currentCity,
                                        prayers = data.prayers,
                                        currentDate = data.currentDate,
                                        nextPray = data.nextPray,
                                        nextPrayTime = data.nextPrayTime,
                                    )
                                }
                            },
                            onError = {
                                viewModelScope.launch {
                                    cancel()
                                    _uiState.emit(UiState(isLoading = false, error = it))
                                }
                            }
                        )
                        actionTrigger(UIAction.StartNextPrayerCountDown)
                    }
                }
            }
        }
    }

    sealed class UIAction {
        data object LoadInitialData : UIAction()
        data object StartNextPrayerCountDown : UIAction()
        data class SelectCity(val city: CityUiModel) : UIAction()
    }
}
