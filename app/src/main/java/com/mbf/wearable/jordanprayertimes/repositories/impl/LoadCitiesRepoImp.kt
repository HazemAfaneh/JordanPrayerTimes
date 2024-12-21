package com.mbf.wearable.jordanprayertimes.repositories.impl

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.remote.CityRemoteModel
import com.mbf.wearable.jordanprayertimes.data.remote.toUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.repositories.LoadCitiesRepo

class LoadCitiesRepoImp : LoadCitiesRepo {
    override suspend fun invoke(): ResultData<List<CityUiModel>> {
        return ResultData.Success(
            listOf(
                CityRemoteModel(id =1,name= "Fajr", prayerTime =  18000000),
                CityRemoteModel(id =2,name= "Sunrise", prayerTime =  23400000),
                CityRemoteModel(id =3,name= "Dhuhr", prayerTime =  43200000),
                CityRemoteModel(id =4,name= "Asr", prayerTime =  56700000),
                CityRemoteModel(id =5,name= "Maghrib", prayerTime =  64800000),
                CityRemoteModel(id =6,name= "Ishaa", prayerTime =  70200000),
            ).map {
                it.toUiModel()
            }
        )
    }
}