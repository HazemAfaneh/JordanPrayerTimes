package com.mbf.jordan_prayer_times_app.data.remote

import com.mbf.jordan_prayer_times_app.data.ui.PrayerUiModel
import com.mbf.jordan_prayer_times_app.helper.timeToHoursAndMinutes

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
