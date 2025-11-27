package com.mbf.wearable.jordanprayertimes.data.remote.datasource

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.remote.CitiesResponse

interface CitiesRemoteDataSource {
    suspend fun fetchCities(): ResultData<CitiesResponse?>
}
