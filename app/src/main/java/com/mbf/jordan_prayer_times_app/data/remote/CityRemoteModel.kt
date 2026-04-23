package com.mbf.jordan_prayer_times_app.data.remote

import com.mbf.jordan_prayer_times_app.data.ui.CityUiModel
import com.mbf.jordan_prayer_times_app.data.ui.PrayerUiModel
import com.mbf.jordan_prayer_times_app.helper.timeToHoursAndMinutes
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
