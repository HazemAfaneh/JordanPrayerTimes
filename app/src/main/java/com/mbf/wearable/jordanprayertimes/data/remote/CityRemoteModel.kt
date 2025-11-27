package com.mbf.wearable.jordanprayertimes.data.remote

import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel
import com.mbf.wearable.jordanprayertimes.helper.timeToHoursAndMinutes
import kotlinx.serialization.Serializable

@Serializable
data class CityRemoteModel(
    val id: Int,
    val name: String,
)

fun CityRemoteModel.toUiModel() = CityUiModel(
    id = this.id,
    name = this.name,
    isSelected = false
)
