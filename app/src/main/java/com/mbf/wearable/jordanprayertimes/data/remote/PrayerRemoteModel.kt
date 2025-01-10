package com.mbf.wearable.jordanprayertimes.data.remote

import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel
import com.mbf.wearable.jordanprayertimes.helper.timeToHoursAndMinutes

data class PrayerRemoteModel(
    val id: Int,
    val name: String,
    val prayerTime:Long
)

fun PrayerRemoteModel.toUiModel() = PrayerUiModel(
    id = this.id,
    name = this.name,
    prayerTime = this.prayerTime.timeToHoursAndMinutes()
)
