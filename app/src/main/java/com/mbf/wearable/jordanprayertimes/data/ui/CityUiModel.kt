package com.mbf.wearable.jordanprayertimes.data.ui

import androidx.compose.runtime.Immutable

@Immutable
data class CityUiModel(
    val id: Int,
    val name: String,
    val isSelected: Boolean
)
