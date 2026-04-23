package com.mbf.jordan_prayer_times_app.data.remote.datasource

import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.remote.CitiesResponse

interface CitiesRemoteDataSource {
    suspend fun fetchCities(): ResultData<CitiesResponse?>
}
