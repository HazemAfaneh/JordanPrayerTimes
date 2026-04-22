package com.mbf.wearable.jordanprayertimes.presentation

import androidx.lifecycle.viewModelScope
import com.mbf.wearable.jordanprayertimes.data.local.CityPreferences
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.repositories.LoadCitiesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val loadCitiesRepo: LoadCitiesRepo,
    private val cityPreferences: CityPreferences
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState
        .onStart { actionTrigger(UIAction.LoadCities) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _uiState.value
        )

    @androidx.compose.runtime.Immutable
    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val cities: List<CityUiModel> = emptyList(),
        val selectedCity: CityUiModel? = null,
        val notificationsEnabled: Boolean = false
    )

    fun actionTrigger(action: UIAction) {
        viewModelScope.launch {
            when (action) {
                is UIAction.LoadCities -> {
                    _uiState.update { it.copy(isLoading = true) }
                    viewModelScope.launch {
                        handleResult(
                            result = loadCitiesRepo(),
                            onSuccess = { cities ->
                                val savedCity = cityPreferences.getSavedCity()
                                val preSelected = cities.find { it.id == savedCity?.id }
                                _uiState.update { state ->
                                    state.copy(
                                        isLoading = false,
                                        cities = cities,
                                        selectedCity = preSelected
                                    )
                                }
                            },
                            onError = { error ->
                                _uiState.update { it.copy(isLoading = false, error = error) }
                            }
                        )
                    }
                }

                is UIAction.SelectCity -> {
                    _uiState.update { it.copy(selectedCity = action.city) }
                }

                is UIAction.ToggleNotifications -> {
                    _uiState.update { it.copy(notificationsEnabled = action.enabled) }
                }
            }
        }
    }

    sealed class UIAction {
        data object LoadCities : UIAction()
        data class SelectCity(val city: CityUiModel) : UIAction()
        data class ToggleNotifications(val enabled: Boolean) : UIAction()
    }
}
