package com.mbf.wearable.jordanprayertimes.data.ui

import androidx.compose.runtime.Immutable

@Immutable
data class PrayerUiModel(
    val id: Int,
    val name: String,
    val prayerTime: String
)
