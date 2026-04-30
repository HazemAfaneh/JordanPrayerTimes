package com.mbf.jordan_prayer_times_app.presentation

import android.app.Application
import android.content.ComponentName
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewModelScope
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.mbf.jordan_prayer_times_app.complication.MainComplicationService
import com.mbf.jordan_prayer_times_app.data.local.CityPreferences
import com.mbf.jordan_prayer_times_app.data.ui.CityUiModel
import com.mbf.jordan_prayer_times_app.data.ui.PrayerUiModel
import com.mbf.jordan_prayer_times_app.notification.PrayerAlarmScheduler
import com.mbf.jordan_prayer_times_app.usecase.LoadPrayerTimesForCityUseCase
import kotlinx.coroutines.Job
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

val LocalAppSharedState = compositionLocalOf<MainViewModel?> { null }

class MainViewModel(
    private val loadPrayerTimesForCityUseCase: LoadPrayerTimesForCityUseCase,
    private val cityPreferences: CityPreferences,
    private val prayerAlarmScheduler: PrayerAlarmScheduler,
    private val application: Application
) : BaseViewModel() {

    private var countdownJob: Job? = null

    private val _countdownFlow = MutableStateFlow("")
    val countdownFlow = _countdownFlow.asStateFlow()

    private val _citySelectedEvent = MutableSharedFlow<Unit>(replay = 0)
    val citySelectedEvent: SharedFlow<Unit> = _citySelectedEvent.asSharedFlow()

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
            "EEEE، yyyy-MM-dd",
            java.util.Locale("ar")
        ).format(java.util.Date()),
        val nextPray: String = "",
        val nextPrayTime: Long = System.currentTimeMillis() + (60 * 60 * 1000L),
        val currentCity: CityUiModel = CityUiModel(name = "", id = -1, isSelected = false),
    )

    fun loadData() {
        actionTrigger(UIAction.LoadInitialData)
    }

    fun actionTrigger(action: UIAction) {
        viewModelScope.launch {
            when (action) {
                is UIAction.LoadInitialData -> {
                    val savedCity = cityPreferences.getSavedCity()
                    if (savedCity != null) {
                        _uiState.update { it.copy(isLoading = true, currentCity = savedCity) }
                        viewModelScope.launch {
                            handleResult(
                                result = loadPrayerTimesForCityUseCase(savedCity),
                                onSuccess = { data ->
                                    _uiState.update { state ->
                                        state.copy(
                                            isLoading = false,
                                            prayers = data.prayers,
                                            nextPray = data.nextPray,
                                            nextPrayTime = data.nextPrayTime
                                        )
                                    }
                                    scheduleAlarmsIfEnabled(data.prayers)
                                    actionTrigger(UIAction.StartNextPrayerCountDown)
                                },
                                onError = { error ->
                                    _uiState.update { it.copy(isLoading = false, error = error) }
                                }
                            )
                        }
                    }
                    // No saved city: stay idle — MainActivity will show SettingsScreen instead
                }

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
                                scheduleAlarmsIfEnabled(data.prayers)
                                cityPreferences.saveCity(action.city)
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
            }
        }
    }

    private fun scheduleAlarmsIfEnabled(prayers: List<PrayerUiModel>) {
        if (cityPreferences.isNotificationsEnabled()) {
            prayerAlarmScheduler.schedulePrayerAlarms(prayers)
        }
        refreshComplication()
    }

    private fun refreshComplication() {
        ComplicationDataSourceUpdateRequester
            .create(application, ComponentName(application, MainComplicationService::class.java))
            .requestUpdateAll()
    }

    private fun startNextPrayerCountDown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remainingTime = _uiState.value.nextPrayTime - System.currentTimeMillis()
            while (remainingTime > 0) {
                val totalSeconds = remainingTime / 1000
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                _countdownFlow.value = if (hours > 0) {
                    String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                }
                delay(1000L)
                remainingTime -= 1000L
            }
            _countdownFlow.value = ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }

    sealed class UIAction {
        data object LoadInitialData : UIAction()
        data object StartNextPrayerCountDown : UIAction()
        data class SelectCity(val city: CityUiModel) : UIAction()
    }
}
