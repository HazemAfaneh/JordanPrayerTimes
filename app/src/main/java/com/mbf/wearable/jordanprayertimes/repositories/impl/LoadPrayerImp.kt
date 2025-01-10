package com.mbf.wearable.jordanprayertimes.repositories.impl

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.remote.PrayerRemoteModel
import com.mbf.wearable.jordanprayertimes.data.remote.toUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayer

class LoadPrayerImp: LoadPrayer {
    override suspend fun invoke(): ResultData<List<PrayerUiModel>> {
        return ResultData.Success(
            listOf(
                PrayerRemoteModel(id =1,name= "Fajr", prayerTime =  18000000),
                PrayerRemoteModel(id =2,name= "Sunrise", prayerTime =  23400000),
                PrayerRemoteModel(id =3,name= "Dhuhr", prayerTime =  43200000),
                PrayerRemoteModel(id =4,name= "Asr", prayerTime =  56700000),
                PrayerRemoteModel(id =5,name= "Maghrib", prayerTime =  64800000),
                PrayerRemoteModel(id =6,name= "Ishaa", prayerTime =  70200000),
            ).map {
                it.toUiModel()
            }
        )
    }
}