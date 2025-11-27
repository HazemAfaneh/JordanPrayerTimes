/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.mbf.wearable.jordanprayertimes.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import com.mbf.wearable.jordanprayertimes.presentation.screens.MainScreen
import com.mbf.wearable.jordanprayertimes.presentation.screens.SettingsScreen
import com.mbf.wearable.jordanprayertimes.presentation.theme.JordanPrayerTimesTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            CompositionLocalProvider(LocalAppSharedState provides viewModel) {
                JordanPrayerTimesTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background),
                        contentAlignment = Alignment.Center
                    ) {
                        TimeText()
                        val navController = rememberSwipeDismissableNavController()
                        SwipeDismissableNavHost(
                            navController = navController,
                            startDestination = "home_screen"
                        ) {
                            composable("home_screen") {
                                MainScreen {
                                    navController.navigate("settings_screen")
                                }
                            }
                            composable("settings_screen") {
                                SettingsScreen()
                            }
                        }

                    }
                }
            }


        }
    }
}



@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
//    WearApp(true, InitialHomeScreenData(), nextPrayTimeIn = "",currentCity = CityUiModel(0,"",false))
}