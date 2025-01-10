/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.mbf.wearable.jordanprayertimes.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData
import com.mbf.wearable.jordanprayertimes.presentation.screens.SettingsScreen
import com.mbf.wearable.jordanprayertimes.presentation.theme.JordanPrayerTimesTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            val isLoading = uiState.isLoading
            val initialData = uiState.initialData
            WearApp(isLoading, initialData)
        }
    }
}

@Composable
fun WearApp( isLoading: Boolean, initialHomeScreenData: InitialHomeScreenData) {
    JordanPrayerTimesTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            if(isLoading){
                CircularProgressIndicator(
                    indicatorColor = Color.Cyan, // Customize as needed
//                    strokeWidth = 4.dp
                )
            }
            TimeText()
            val navController = rememberSwipeDismissableNavController()
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "home_screen"
            ) {
                composable("home_screen") {
                    MainScreen(initialHomeScreenData, onScreenNavigation = {
                        navController.navigate("settings_screen")
                    })
                }
                composable("settings_screen") {
                    SettingsScreen(initialHomeScreenData.cities, initialHomeScreenData.currentCity)
                }
            }

        }
    }
}

@Composable
fun MainScreen(initialHomeScreenData: InitialHomeScreenData, onScreenNavigation:()->Unit) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    )  {
        item {
            Text(
                text = initialHomeScreenData.currentCity.name,
                style = MaterialTheme.typography.title1,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp).clickable { onScreenNavigation.invoke() }
            )
        }
        item {
            Text(
                text = initialHomeScreenData.nextPray,
                style = MaterialTheme.typography.body1,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        item {
            Text(
                text = initialHomeScreenData.nextPrayTimeIn,
                style = MaterialTheme.typography.body1,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
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
        // Current Date Text
        item {
            Text(
                text = initialHomeScreenData.currentDate,
                style = MaterialTheme.typography.body2,
                color = Color.White
            )
        }
        item {
            val items = initialHomeScreenData.cities

            // Calculate the number of rows needed based on the number of items
            val rows = items.chunked(3)  // Divide the list into chunks of 3 items each

            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                // For each row
                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp), // Space between items
                    ) {
                        // For each item in the row
                        for (item in row) {
                            CircularItem(item)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun CircularItem(city: CityUiModel) {
    Box(
        modifier = Modifier
            .size(50.dp) // Set the size of the circle
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
                    fontSize = 9.sp,  // Set the desired smaller font size
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = city.prayerTime,
                color = Color.White,
                style = TextStyle(
                    fontSize = 8.sp,  // Set the desired smaller font size
                    color = Color.White
                )
            )
        }
    }
}
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(true, InitialHomeScreenData())
}