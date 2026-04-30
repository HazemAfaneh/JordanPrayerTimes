package com.mbf.jordan_prayer_times_app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object PrayerDataRefreshScheduler {

    private const val WORK_NAME = "monthly_prayer_refresh"

    fun ensureScheduled(context: Context) {
        enqueue(context, ExistingWorkPolicy.KEEP)
    }

    fun scheduleNextMonth(context: Context) {
        enqueue(context, ExistingWorkPolicy.REPLACE)
    }

    private fun enqueue(context: Context, policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<RefreshPrayerDataWorker>()
            .setInitialDelay(delayToNextFirstOfMonth(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, policy, request)
    }

    private fun delayToNextFirstOfMonth(): Long {
        val now = Calendar.getInstance()
        val nextFirst = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return nextFirst.timeInMillis - now.timeInMillis
    }
}
