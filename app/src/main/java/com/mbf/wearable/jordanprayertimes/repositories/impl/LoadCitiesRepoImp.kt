package com.mbf.wearable.jordanprayertimes.repositories.impl

import com.mbf.wearable.jordanprayertimes.ErrorEntity
import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.remote.datasource.CitiesRemoteDataSource
import com.mbf.wearable.jordanprayertimes.data.remote.toUiModels
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.repositories.LoadCitiesRepo

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