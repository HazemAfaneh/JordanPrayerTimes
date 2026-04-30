package com.mbf.jordan_prayer_times_app.presentation

import androidx.lifecycle.viewModelScope
import com.mbf.jordan_prayer_times_app.data.local.CityPreferences
import com.mbf.jordan_prayer_times_app.data.ui.CityUiModel
import com.mbf.jordan_prayer_times_app.notification.PrayerAlarmScheduler
import com.mbf.jordan_prayer_times_app.repositories.LoadCitiesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val loadCitiesRepo: LoadCitiesRepo,
    private val cityPreferences: CityPreferences,
    private val prayerAlarmScheduler: PrayerAlarmScheduler
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
                                        selectedCity = preSelected,
                                        notificationsEnabled = cityPreferences.isNotificationsEnabled()
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
                    cityPreferences.saveNotificationsEnabled(action.enabled)
                    if (!action.enabled) prayerAlarmScheduler.cancelAllAlarms()
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
