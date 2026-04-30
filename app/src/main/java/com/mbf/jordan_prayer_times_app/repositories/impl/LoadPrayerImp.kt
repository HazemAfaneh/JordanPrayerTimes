package com.mbf.jordan_prayer_times_app.repositories.impl

import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.remote.PrayerRemoteModel
import com.mbf.jordan_prayer_times_app.data.remote.toUiModel
import com.mbf.jordan_prayer_times_app.data.ui.PrayerUiModel
import com.mbf.jordan_prayer_times_app.repositories.LoadPrayer

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