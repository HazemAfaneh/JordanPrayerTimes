package com.mbf.jordan_prayer_times_app.presentation.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.mbf.jordan_prayer_times_app.data.ui.PrayerUiModel
import com.mbf.jordan_prayer_times_app.helper.to12hFormat
import com.mbf.jordan_prayer_times_app.presentation.LocalAppSharedState

@Composable
fun MainScreen(onScreenNavigation: () -> Unit) {
    val viewModel = LocalAppSharedState.current ?: return
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val countdownText by viewModel.countdownFlow.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    // Two per row: three no longer fit a small round screen once the user enlarges the font.
    val prayerRows = remember(uiState.prayers) { uiState.prayers.chunked(2) }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
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
                // Round screens are narrowest at the top and bottom: keep text off the edges at
                // any system font size (Wear app quality, "Wear font size").
                contentPadding = PaddingValues(horizontal = ROUND_SCREEN_PADDING, vertical = 24.dp),
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

            if (uiState.isLoading) {
                LoadingOverlay()
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
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable { onScreenNavigation() }
    )
}

@Composable
private fun NextPrayerText(nextPray: String) {
    if (nextPray.isEmpty()) return
    Text(
        text = "التالي: $nextPray",
        style = MaterialTheme.typography.body1,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
    )
}

@Composable
private fun CountdownDisplay(countdownText: String) {
    if (countdownText.isEmpty()) return
    Text(
        text = countdownText,
        style = MaterialTheme.typography.body1,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
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
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
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
            Box(
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularItem(prayer = prayer)
            }
        }
    }
}

@Composable
private fun CircularItem(prayer: PrayerUiModel) {
    // Grows with the text instead of a fixed 55dp circle, which clipped large fonts.
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 55.dp, minHeight = 55.dp)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = prayer.name,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prayer.prayerTime.to12hFormat(),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = Color.White
                )
            )
        }
    }
}

/** Horizontal inset for list content on round screens (about 12% of a small watch's width). */
internal val ROUND_SCREEN_PADDING = 22.dp
