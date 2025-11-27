package com.mbf.wearable.jordanprayertimes.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.presentation.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Settings screen with loading indicator
 * Shows a circular progress bar overlay while fetching cities data from the API
 */
@Composable
fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    // Memoize values to prevent unnecessary recompositions
    val isLoading = remember(uiState.isLoading) { uiState.isLoading }
    val cities = remember(uiState.cities) { uiState.cities }
    val selectedCityId = remember(uiState.selectedCity?.id) { uiState.selectedCity?.id ?: 0 }
    val notificationsEnabled =
        remember(uiState.notificationsEnabled) { uiState.notificationsEnabled }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Main content
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item(key = "notifications_toggle") {
                NotificationToggle(
                    notificationsEnabled = notificationsEnabled,
                    onToggle = { enabled ->
                        viewModel.actionTrigger(
                            SettingsViewModel.UIAction.ToggleNotifications(
                                enabled
                            )
                        )
                    }
                )
            }

            item(key = "divider") {
                SettingsDivider()
            }

            item(key = "cities_header") {
                Text(
                    text = "Cities",
                    modifier = Modifier,
                    style = MaterialTheme.typography.title3
                )
            }

            items(
                items = cities,
                key = { city -> city.id }
            ) { city ->
                CityChip(
                    city = city,
                    isSelected = city.id == selectedCityId,
                    onCitySelected = {
                        viewModel.actionTrigger(SettingsViewModel.UIAction.SelectCity(city))
                    }
                )
            }
        }

        // Loading overlay
        if (isLoading) {
            LoadingOverlay()
        }
    }
}

/**
 * Loading overlay with circular progress indicator
 */
@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background.copy(alpha = 0.7f)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        CircularProgressIndicator(
            indicatorColor = Color.Cyan,
            strokeWidth = 4.dp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Notification toggle - isolated to prevent recomposition
 */
@Composable
private fun NotificationToggle(
    notificationsEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ToggleChip(
        checked = notificationsEnabled,
        onCheckedChange = onToggle,
        label = {
            Text(
                "Enable Notifications",
                fontSize = 12.sp
            )
        },
        toggleControl = {
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Cyan,
                )
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

/**
 * Divider - isolated as a separate composable
 */
@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colors.surface)
            .padding(vertical = 16.dp)
    )
}

/**
 * City chip - isolated and optimized with stable parameters
 */
@Composable
private fun CityChip(
    city: CityUiModel,
    isSelected: Boolean,
    onCitySelected: () -> Unit
) {
    // Remember city properties to avoid recomposition
    val cityName = remember(city.id) { city.name }

    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCitySelected,
        label = {
            Text(
                text = cityName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = if (isSelected) {
                Color.Cyan.copy(alpha = 0.8f)
            } else {
                Color.Gray
            }
        )
    )
}

@WearPreviewDevices
@Composable
fun PreviewSettingsScreen() {
    // Preview removed - requires proper setup
}
