package com.mbf.wearable.jordanprayertimes.repositories

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.remote.CityRemoteModel
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel

interface LoadCitiesRepo {
    suspend operator fun invoke(
    ): ResultData<List<CityUiModel>>
}