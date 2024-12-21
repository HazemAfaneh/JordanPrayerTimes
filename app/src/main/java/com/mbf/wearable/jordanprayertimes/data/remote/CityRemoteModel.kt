package com.mbf.wearable.jordanprayertimes.data.remote

import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.helper.timeToHoursAndMinutes

data class CityRemoteModel(
    val id: Int,
    val name: String,
    val prayerTime:Long
)

fun CityRemoteModel.toUiModel() = CityUiModel(
    id = this.id,
    name = this.name,
    prayerTime = this.prayerTime.timeToHoursAndMinutes()
)
