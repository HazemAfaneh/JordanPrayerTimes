package com.mbf.jordan_prayer_times_app.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.mbf.jordan_prayer_times_app.data.local.CityPreferences
import com.mbf.jordan_prayer_times_app.presentation.screens.MainScreen
import com.mbf.jordan_prayer_times_app.presentation.screens.SettingsScreen
import com.mbf.jordan_prayer_times_app.presentation.theme.JordanPrayerTimesTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

        val hasSavedCity = CityPreferences(this).hasSavedCity()
        val startDestination = if (hasSavedCity) "home_screen" else "settings_screen"

        setContent {
            CompositionLocalProvider(
                LocalAppSharedState provides viewModel,
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
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
                            startDestination = startDestination
                        ) {
                            composable("home_screen") {
                                MainScreen {
                                    navController.navigate("settings_screen")
                                }
                            }
                            composable("settings_screen") {
                                SettingsScreen(
                                    onNavigateBack = {
                                        // First launch: settings is the root, navigate forward to home
                                        // Subsequent: pop back to home
                                        val popped = navController.popBackStack()
                                        if (!popped) {
                                            navController.navigate("home_screen") {
                                                popUpTo("settings_screen") { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
