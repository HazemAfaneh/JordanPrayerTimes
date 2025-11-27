package com.mbf.wearable.jordanprayertimes.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.presentation.LocalAppSharedState
import com.mbf.wearable.jordanprayertimes.presentation.MainViewModel

@Composable
fun SettingsScreen() {
    val viewModel = LocalAppSharedState.current ?: return
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    var notificationsEnabled by remember { mutableStateOf(false) }
    var selectedCityId by remember(uiState.currentCity.id) {
        mutableStateOf(uiState.currentCity.id)
    }

    // Memoize cities list to prevent unnecessary recompositions
    val cities = remember(uiState.cities) { uiState.cities }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        item(key = "notifications_toggle") {
            NotificationToggle(
                notificationsEnabled = notificationsEnabled,
                onToggle = { notificationsEnabled = it }
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
                    selectedCityId = city.id
                    viewModel.actionTrigger(MainViewModel.UIAction.SelectCity(city))
                }
            )
        }
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
