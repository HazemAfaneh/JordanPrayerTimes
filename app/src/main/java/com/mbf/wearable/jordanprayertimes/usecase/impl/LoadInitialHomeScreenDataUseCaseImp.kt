package com.mbf.wearable.jordanprayertimes.usecase.impl

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData
import com.mbf.wearable.jordanprayertimes.doIfSuccess
import com.mbf.wearable.jordanprayertimes.repositories.LoadCitiesRepo
import com.mbf.wearable.jordanprayertimes.usecase.LoadInitialHomeScreenDataUseCase

class LoadInitialHomeScreenDataUseCaseImp(val loadCitiesRepo: LoadCitiesRepo): LoadInitialHomeScreenDataUseCase {
    override suspend fun invoke(): ResultData<InitialHomeScreenData> {
        return loadCitiesRepo().doIfSuccess {
            InitialHomeScreenData(
                cities = it
            )
        }
    }
}