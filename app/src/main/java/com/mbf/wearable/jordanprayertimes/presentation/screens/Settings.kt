package com.mbf.wearable.jordanprayertimes.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun SettingsScreen() {
    val viewModel = koinViewModel<SettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item(key = "notifications_toggle") {
                NotificationToggle(
                    notificationsEnabled = uiState.notificationsEnabled,
                    onToggle = { enabled ->
                        viewModel.actionTrigger(
                            SettingsViewModel.UIAction.ToggleNotifications(enabled)
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
                    style = MaterialTheme.typography.title3
                )
            }

            items(
                items = uiState.cities,
                key = { city -> city.id }
            ) { city ->
                CityChip(
                    city = city,
                    isSelected = city.id == uiState.selectedCity?.id,
                    onCitySelected = {
                        viewModel.actionTrigger(SettingsViewModel.UIAction.SelectCity(city))
                    }
                )
            }
        }

        if (uiState.isLoading) {
            LoadingOverlay()
        }
    }
}

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

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colors.surface)
    )
}

@Composable
private fun CityChip(
    city: CityUiModel,
    isSelected: Boolean,
    onCitySelected: () -> Unit
) {
    Chip(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCitySelected,
        label = {
            Text(
                text = city.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = ChipDefaults.chipColors(
            backgroundColor = if (isSelected) Color.Cyan.copy(alpha = 0.8f) else Color.Gray
        )
    )
}

@WearPreviewDevices
@Composable
fun PreviewSettingsScreen() {
    // Preview removed - requires proper setup
}
