package com.mbf.wearable.jordanprayertimes.usecase.impl

import com.mbf.wearable.jordanprayertimes.ErrorEntity
import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData
import com.mbf.wearable.jordanprayertimes.doIfSuccess
import com.mbf.wearable.jordanprayertimes.repositories.LoadCitiesRepo
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayer
import com.mbf.wearable.jordanprayertimes.usecase.LoadInitialHomeScreenDataUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class LoadInitialHomeScreenDataUseCaseImp(val loadCitiesRepo: LoadCitiesRepo, val loadPrayer: LoadPrayer): LoadInitialHomeScreenDataUseCase {

    override suspend fun invoke(): ResultData<InitialHomeScreenData> = coroutineScope {
        try {
            val prayersDeferred = async { loadPrayer() }
            val citiesDeferred = async { loadCitiesRepo() }

            val prayersResult = prayersDeferred.await()
            if (prayersResult is ResultData.Error) {
                return@coroutineScope ResultData.Error<InitialHomeScreenData>(prayersResult.data)
            }

            val citiesResult = citiesDeferred.await()
            if (citiesResult is ResultData.Error) {
                return@coroutineScope ResultData.Error<InitialHomeScreenData>(citiesResult.data)
            }

            val prayers = (prayersResult as ResultData.Success).data
            val cities = (citiesResult as ResultData.Success).data

            ResultData.Success(
                InitialHomeScreenData(
                    cities = cities,
                    prayers = prayers
                )
            )
        } catch (e: Exception) {
            ResultData.Error(ErrorEntity.Unknown)
        }
    }

}