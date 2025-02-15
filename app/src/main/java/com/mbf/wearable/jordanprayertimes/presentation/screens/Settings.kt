package com.mbf.wearable.jordanprayertimes.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel
import com.mbf.wearable.jordanprayertimes.presentation.LocalAppSharedState
import com.mbf.wearable.jordanprayertimes.presentation.MainViewModel

@Composable
fun SettingsScreen() {
    val viewModel = LocalAppSharedState.current
    val uiState = viewModel?.uiState?.collectAsStateWithLifecycle()?.value
    val listState = rememberScalingLazyListState()
    var notificationsEnabled by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf(uiState?.currentCity) }
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {

        item {
            ToggleChip(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it },
                label = { Text("Enable Notifications",
                    fontSize = 12.sp) } ,
                toggleControl = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Cyan, // Color for the checked state
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(androidx.wear.compose.material.MaterialTheme.colors.surface)
                    .padding(vertical = 16.dp)
            )
        }
        item {
            Text(
                text = "Cities",
                modifier = Modifier ,
                style = MaterialTheme.typography.title3
            )
        }

        items(uiState?.cities?: emptyList()) { city ->
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    selectedCity = city
                    viewModel?.actionTrigger(MainViewModel.UIAction.SelectCity(city))

                },
                label = {
                    Text(
                        text = city.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = ChipDefaults.chipColors(
                    backgroundColor = if (city.id == selectedCity?.id) {
                        Color.Cyan.copy(alpha = 0.8f)
                    } else {
                        Color.Gray
                    }
                )
            )
        }
    }
}

@WearPreviewDevices
@Composable
fun PreviewSettingsScreen() {
//    SettingsScreen()
}