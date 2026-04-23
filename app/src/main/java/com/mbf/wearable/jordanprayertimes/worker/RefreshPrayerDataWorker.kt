package com.mbf.wearable.jordanprayertimes.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.local.CityPreferences
import com.mbf.wearable.jordanprayertimes.data.local.MonthlyPrayerCache
import com.mbf.wearable.jordanprayertimes.data.remote.datasource.PrayerTimesRemoteDataSource
import com.mbf.wearable.jordanprayertimes.data.remote.findTodayPrayers
import com.mbf.wearable.jordanprayertimes.data.remote.toPrayerUiModels
import com.mbf.wearable.jordanprayertimes.notification.PrayerAlarmScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class RefreshPrayerDataWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val cityPreferences: CityPreferences by inject()
    private val prayerTimesRemoteDataSource: PrayerTimesRemoteDataSource by inject()
    private val monthlyPrayerCache: MonthlyPrayerCache by inject()
    private val prayerAlarmScheduler: PrayerAlarmScheduler by inject()

    override suspend fun doWork(): Result {
        val city = cityPreferences.getSavedCity()
            ?: return Result.success() // no city selected yet, nothing to refresh

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1

        return try {
            val result = prayerTimesRemoteDataSource.fetchPrayerTimes(city.name, year, month)
            if (result is ResultData.Success && result.data != null) {
                monthlyPrayerCache.put(city.name, year, month, result.data)

                if (cityPreferences.isNotificationsEnabled()) {
                    result.data.prayerData
                        .findTodayPrayers()
                        ?.toPrayerUiModels()
                        ?.let { prayerAlarmScheduler.schedulePrayerAlarms(it) }
                }
            }
            // Chain next month's refresh regardless of network success
            PrayerDataRefreshScheduler.scheduleNextMonth(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
