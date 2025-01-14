package com.mbf.wearable.jordanprayertimes.presentation.screens

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel
import com.mbf.wearable.jordanprayertimes.presentation.LocalAppSharedState
import com.mbf.wearable.jordanprayertimes.presentation.MainViewModel

@Composable
fun MainScreen(onScreenNavigation: () -> Unit) {
    val viewModel = LocalAppSharedState.current
    val uiState = viewModel?.uiState?.collectAsStateWithLifecycle()?.value
//    val countdownFlow = viewModel?.countdownFlow?.collectAsStateWithLifecycle()?.value
    val isLoading =  uiState?.isLoading == true
    val prayers =  uiState?.prayers
    val currentCity =  uiState?.currentCity

    Box( modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center) {
//        if(isLoading){
//            CircularProgressIndicator(
//                indicatorColor = Color.Cyan, // Customize as needed
////                    strokeWidth = 4.dp
//            )
//        }
    ScalingLazyColumn(
        modifier = Modifier.composed {
            Log.d("Composition", "MainScreen recomposed")
            this
        }.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = currentCity?.name?:"",
                style = MaterialTheme.typography.title1,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clickable { onScreenNavigation.invoke() }
            )
        }
        item {
            Text(
                text =  uiState?.nextPray?:"",
                style = MaterialTheme.typography.body1,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item {
            CountdownDisplay(viewModel = viewModel)
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
        // Current Date Text
        item {
            Text(
                text =  uiState?.currentDate?:"",
                style = MaterialTheme.typography.body2,
                color = Color.White
            )
        }
        item {
            val items = prayers

            // Calculate the number of rows needed based on the number of items
            val rows = items?.chunked(3)  // Divide the list into chunks of 3 items each

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in rows?: emptyList()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center, // Changed from spacedBy to Center
                    ) {
                        // For each item in the row
                        for (item in row) {
                            Box(modifier = Modifier.padding(horizontal = 8.dp)) { // Added horizontal padding
                                CircularItem(item)
                            }
                        }
                    }
                }
            }
        }
    }
    }

}
@Composable
private fun CountdownDisplay(viewModel: MainViewModel?) {
    val countdownText = viewModel?.countdownFlow?.collectAsStateWithLifecycle() ?: remember { mutableStateOf("") }

    Text(
        text = countdownText.value,
        style = MaterialTheme.typography.body1,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}
@Composable
fun CircularItem(city: PrayerUiModel) {
    Box(
        modifier = Modifier
            .size(55.dp) // Set the size of the circle
            .clip(CircleShape) // Make it a circle
            .background(Color.Transparent) // Background color of the circle
            .padding(8.dp), // Padding inside the circle
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = city.name,
                color = Color.Cyan,
                style = TextStyle(
                    fontSize = 10.sp,  // Set the desired smaller font size
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = city.prayerTime,
                color = Color.White,
                style = TextStyle(
                    fontSize = 9.sp,  // Set the desired smaller font size
                    color = Color.White
                )
            )
        }
    }
}
