package com.mbf.jordan_prayer_times_app.data.ui

import androidx.compose.runtime.Immutable

@Immutable
data class CityUiModel(
    val id: Int,
    val name: String,
    val isSelected: Boolean
)
