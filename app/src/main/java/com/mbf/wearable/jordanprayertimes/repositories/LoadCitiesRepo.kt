package com.mbf.wearable.jordanprayertimes.repositories

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel

interface LoadCitiesRepo {
    suspend operator fun invoke(
    ): ResultData<List<CityUiModel>>
}