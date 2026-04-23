package com.mbf.jordan_prayer_times_app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.mbf.jordan_prayer_times_app.data.ui.CityUiModel
import com.mbf.jordan_prayer_times_app.presentation.LocalAppSharedState
import com.mbf.jordan_prayer_times_app.presentation.MainViewModel
import com.mbf.jordan_prayer_times_app.presentation.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<SettingsViewModel>()
    val mainViewModel = LocalAppSharedState.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainUiState by remember(mainViewModel) {
        mainViewModel?.uiState ?: MutableStateFlow(MainViewModel.UiState())
    }.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    // Navigate back once the prayer-times API call finishes (success or error)
    LaunchedEffect(mainViewModel) {
        mainViewModel?.citySelectedEvent?.collect {
            onNavigateBack()
        }
    }

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
                    text = "المدن",
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
                        mainViewModel?.actionTrigger(MainViewModel.UIAction.SelectCity(city))
                    }
                )
            }
        }

        // Show overlay while cities are loading OR while prayer times are being fetched
        if (uiState.isLoading || mainUiState.isLoading) {
            LoadingOverlay()
        }
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
                "تفعيل الإشعارات",
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
