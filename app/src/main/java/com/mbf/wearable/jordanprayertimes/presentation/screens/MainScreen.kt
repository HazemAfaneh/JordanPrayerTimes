package com.mbf.wearable.jordanprayertimes.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel
import com.mbf.wearable.jordanprayertimes.presentation.LocalAppSharedState
import java.util.Locale

@Composable
fun MainScreen(onScreenNavigation: () -> Unit) {
    val viewModel = LocalAppSharedState.current ?: return
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val countdownText by viewModel.countdownFlow.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    val prayerRows = remember(uiState.prayers) { uiState.prayers.chunked(3) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "city_header") {
                CityHeader(
                    cityName = uiState.currentCity.name,
                    onScreenNavigation = onScreenNavigation
                )
            }

            item(key = "next_prayer") {
                NextPrayerText(nextPray = uiState.nextPray)
            }

            item(key = "countdown") {
                CountdownDisplay(countdownText = countdownText)
            }

            item(key = "divider") {
                Divider()
            }

            item(key = "current_date") {
                CurrentDateText(currentDate = uiState.currentDate)
            }

            items(
                items = prayerRows,
                key = { row -> row.first().id }
            ) { row ->
                PrayerRow(prayers = row)
            }
        }
    }
}

@Composable
private fun CityHeader(
    cityName: String,
    onScreenNavigation: () -> Unit
) {
    Text(
        text = cityName,
        style = MaterialTheme.typography.title1,
        color = Color.White,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clickable { onScreenNavigation() }
    )
}

@Composable
private fun NextPrayerText(nextPray: String) {
    if (nextPray.isEmpty()) return
    Text(
        text = "Next: $nextPray",
        style = MaterialTheme.typography.body1,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun CountdownDisplay(countdownText: String) {
    if (countdownText.isEmpty()) return
    Text(
        text = countdownText,
        style = MaterialTheme.typography.body1,
        color = Color.White,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colors.surface)
    )
}

@Composable
private fun CurrentDateText(currentDate: String) {
    Text(
        text = currentDate,
        style = MaterialTheme.typography.body2,
        color = Color.White
    )
}

@Composable
private fun PrayerRow(prayers: List<PrayerUiModel>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        prayers.forEach { prayer ->
            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                CircularItem(prayer = prayer)
            }
        }
    }
}

private fun String.to12hFormat(): String {
    val parts = split(":")
    if (parts.size < 2) return this
    val h = parts[0].trim().toIntOrNull() ?: return this
    val m = parts[1].trim().toIntOrNull() ?: return this
    val amPm = if (h < 12) "AM" else "PM"
    val hour12 = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
    return String.format(Locale.US, "%d:%02d %s", hour12, m, amPm)
}

@Composable
private fun CircularItem(prayer: PrayerUiModel) {
    Box(
        modifier = Modifier
            .size(55.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = prayer.name,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prayer.prayerTime.to12hFormat(),
                style = TextStyle(
                    fontSize = 9.sp,
                    color = Color.White
                )
            )
        }
    }
}
