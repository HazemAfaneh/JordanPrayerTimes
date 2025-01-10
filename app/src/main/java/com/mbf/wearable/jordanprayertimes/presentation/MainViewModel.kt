package com.mbf.wearable.jordanprayertimes.presentation

import androidx.lifecycle.viewModelScope
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadCitiesRepoImp
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadPrayerImp
import com.mbf.wearable.jordanprayertimes.usecase.LoadInitialHomeScreenDataUseCase
import com.mbf.wearable.jordanprayertimes.usecase.impl.LoadInitialHomeScreenDataUseCaseImp
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel : BaseViewModel() {
    val loadInitialHomeScreenDataUseCase: LoadInitialHomeScreenDataUseCase =
        LoadInitialHomeScreenDataUseCaseImp(
            LoadCitiesRepoImp(),
            LoadPrayerImp()
        )


    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    //    val uiState = _uiState.onStart { loadData() }.stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(5000L),
//        false
//    )
    init {
        loadData()
    }

    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val initialData: InitialHomeScreenData = InitialHomeScreenData(),
        val currentCity:CityUiModel = CityUiModel(name = "Amman", id = 1, isSelected = true)

    )

    private fun loadData() {
        actionTrigger(UIAction.LoadInitialData)
    }

      fun actionTrigger(action: UIAction) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
//            delay(2000)
            when (action) {
                is UIAction.SelectCity -> {
                    viewModelScope.launch {

                        _uiState.update { uiStates ->
                            uiStates.copy(
                                isLoading = false,
                                currentCity = action.city,
                            )
                        }
                    }
                }
                is UIAction.StartNextPrayerCountDown -> {

                    viewModelScope.launch {
                        var remainingTime = _uiState.value.initialData.nextPrayTime
                        while (remainingTime > 0) {
                            delay(1000L) // Delay for 1 second
                            remainingTime -= 1000L // Decrease remaining time by 1 second
                            _uiState.update { uiStates ->
                                uiStates.copy(
                                    isLoading = false,
                                    initialData = uiStates.initialData.copy(
                                        nextPrayTimeIn = "Next prayer in: ${
                                            String.format(
                                                Locale.getDefault(),
                                                "%02d:%02d",
                                                ((remainingTime / 1000) % 3600) / 60,  // Total minutes
                                                (remainingTime / 1000) % 60   // Remaining seconds
                                            )
                                        }"
                                    )
                                )

                            }
                        }
                    }
                }

                is UIAction.LoadInitialData -> {
                    _uiState.update { it.copy(isLoading = true) }
                    viewModelScope.launch {
                        handleResult(result = loadInitialHomeScreenDataUseCase(),
                            onSuccess = { data ->
                                _uiState.update { uiStates ->
                                    uiStates.copy(
                                        isLoading = false,
                                        initialData = data
                                    )
                                }
                            }, onError = {
                                viewModelScope.launch {
                                    cancel()
                                    _uiState.emit(
                                        UiState(
                                            isLoading = false,
                                            error = it
                                        )
                                    )
                                }
                            })
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