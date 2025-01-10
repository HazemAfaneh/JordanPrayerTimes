package com.mbf.wearable.jordanprayertimes.repositories.impl

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.remote.CityRemoteModel
import com.mbf.wearable.jordanprayertimes.data.remote.PrayerRemoteModel
import com.mbf.wearable.jordanprayertimes.data.remote.toUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel
import com.mbf.wearable.jordanprayertimes.repositories.LoadCitiesRepo

class LoadCitiesRepoImp : LoadCitiesRepo {
    override suspend fun invoke(): ResultData<List<CityUiModel>> {
        return ResultData.Success(
            listOf(
                CityRemoteModel(id =1,name= "Amman"),
                CityRemoteModel(id =2,name= "Al-Zarqa"),
                CityRemoteModel(id =3,name= "Salt"),
                CityRemoteModel(id =4,name= "Al-Ramtha"),
                CityRemoteModel(id =5,name= "Ajloun"),
                CityRemoteModel(id =6,name= "Mafraq"),
                CityRemoteModel(id =7,name= "Karak"),
                CityRemoteModel(id =8,name= "Tafila"),
                CityRemoteModel(id =9,name= "Ma'an"),
                CityRemoteModel(id =10,name= "Aqaba"),
                CityRemoteModel(id =11,name= "Irbid"),
                CityRemoteModel(id =12,name= "Jarash"),
            ).map {
                it.toUiModel()
            }
        )
    }
}