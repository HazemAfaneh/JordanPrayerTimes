package com.mbf.jordan_prayer_times_app.data.remote

import com.mbf.jordan_prayer_times_app.data.ui.CityUiModel
import kotlinx.serialization.Serializable

@Serializable
data class CitiesResponse(
    val cities: List<String>
)

fun CitiesResponse.toUiModels(): List<CityUiModel> {
    return cities.mapIndexed { index, cityName ->
        CityUiModel(
            id = index + 1,
            name = cityName,
            isSelected = false
        )
    }
}
