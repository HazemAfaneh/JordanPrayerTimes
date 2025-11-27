package com.mbf.wearable.jordanprayertimes.data.remote

import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
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
