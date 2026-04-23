package com.mbf.jordan_prayer_times_app.data.ui

import androidx.compose.runtime.Immutable

@Immutable
data class PrayerUiModel(
    val id: Int,
    val name: String,
    val prayerTime: String
)
