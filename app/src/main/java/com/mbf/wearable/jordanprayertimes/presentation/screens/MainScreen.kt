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
import androidx.compose.runtime.derivedStateOf
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
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel
import com.mbf.wearable.jordanprayertimes.presentation.LocalAppSharedState
import com.mbf.wearable.jordanprayertimes.presentation.MainViewModel

@Composable
fun MainScreen(onScreenNavigation: () -> Unit) {
    val viewModel = LocalAppSharedState.current ?: return
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Extract stable values to prevent unnecessary recompositions
    val currentCityName = remember(uiState.currentCity) { uiState.currentCity.name }
    val nextPray = remember(uiState.nextPray) { uiState.nextPray }
    val currentDate = remember(uiState.currentDate) { uiState.currentDate }
    val prayers = remember(uiState.prayers) { uiState.prayers }

    // Memoize prayer rows to avoid recalculation on every recomposition
    val prayerRows by remember(prayers) {
        derivedStateOf { prayers.chunked(3) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "city_header") {
                CityHeader(
                    cityName = currentCityName,
                    onScreenNavigation = onScreenNavigation
                )
            }

            item(key = "next_prayer") {
                NextPrayerText(nextPray = nextPray)
            }

            item(key = "countdown") {
                CountdownDisplay(viewModel = viewModel)
            }

            item(key = "divider") {
                Divider()
            }

            item(key = "current_date") {
                CurrentDateText(currentDate = currentDate)
            }

            item(key = "prayers_grid") {
                PrayersGrid(prayerRows = prayerRows)
            }
        }
    }
}

/**
 * City header with navigation - isolated to prevent recomposition
 */
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

/**
 * Next prayer name display - isolated to prevent recomposition
 */
@Composable
private fun NextPrayerText(nextPray: String) {
    Text(
        text = nextPray,
        style = MaterialTheme.typography.body1,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

/**
 * Countdown display - only this composable recomposes when countdown updates
 */
@Composable
private fun CountdownDisplay(viewModel: MainViewModel) {
    val countdownText by viewModel.countdownFlow.collectAsStateWithLifecycle()

    Text(
        text = countdownText,
        style = MaterialTheme.typography.body1,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

/**
 * Divider - isolated as a separate composable
 */
@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colors.surface)
            .padding(vertical = 16.dp)
    )
}

/**
 * Current date display - isolated to prevent recomposition
 */
@Composable
private fun CurrentDateText(currentDate: String) {
    Text(
        text = currentDate,
        style = MaterialTheme.typography.body2,
        color = Color.White
    )
}

/**
 * Prayers grid - isolated and optimized with stable parameters
 */
@Composable
private fun PrayersGrid(prayerRows: List<List<PrayerUiModel>>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        prayerRows.forEach { row ->
            PrayerRow(prayers = row)
        }
    }
}

/**
 * Single row of prayer items - isolated to prevent recomposition
 */
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

/**
 * Single prayer item - optimized with stable parameters
 */
@Composable
private fun CircularItem(prayer: PrayerUiModel) {
    // Remember the prayer properties to avoid recomposition when parent recomposes
    val prayerName = remember(prayer.id) { prayer.name }
    val prayerTime = remember(prayer.id) { prayer.prayerTime }

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
                text = prayerName,
                color = Color.Cyan,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prayerTime,
                color = Color.White,
                style = TextStyle(
                    fontSize = 9.sp,
                    color = Color.White
                )
            )
        }
    }
}
