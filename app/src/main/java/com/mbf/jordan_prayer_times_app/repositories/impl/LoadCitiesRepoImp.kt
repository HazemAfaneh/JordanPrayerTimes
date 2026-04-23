package com.mbf.jordan_prayer_times_app.repositories.impl

import com.mbf.jordan_prayer_times_app.ErrorEntity
import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.remote.datasource.CitiesRemoteDataSource
import com.mbf.jordan_prayer_times_app.data.remote.toUiModels
import com.mbf.jordan_prayer_times_app.data.ui.CityUiModel
import com.mbf.jordan_prayer_times_app.repositories.LoadCitiesRepo

class LoadCitiesRepoImp(
    private val citiesRemoteDataSource: CitiesRemoteDataSource
) : LoadCitiesRepo {
    override suspend fun invoke(): ResultData<List<CityUiModel>> {
        return when (val result = citiesRemoteDataSource.fetchCities()) {
            is ResultData.Success -> {
                val citiesResponse = result.data
                if (citiesResponse != null) {
                    ResultData.Success(citiesResponse.toUiModels())
                } else {
                    ResultData.Error(
                        ErrorEntity.InternalError("Empty response from server")
                    )
                }
            }
            is ResultData.Error -> {
                ResultData.Error(result.data)
            }
        }
    }
}