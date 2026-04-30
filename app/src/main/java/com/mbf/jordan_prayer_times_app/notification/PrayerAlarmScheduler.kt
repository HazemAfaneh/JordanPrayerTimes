package com.mbf.jordan_prayer_times_app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mbf.jordan_prayer_times_app.data.ui.PrayerUiModel
import java.util.Calendar

class PrayerAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedulePrayerAlarms(prayers: List<PrayerUiModel>) {
        cancelAllAlarms()
        val now = System.currentTimeMillis()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        prayers.forEachIndexed { index, prayer ->
            val parts = prayer.prayerTime.split(":")
            if (parts.size < 2) return@forEachIndexed
            val h = parts[0].trim().toIntOrNull() ?: return@forEachIndexed
            val m = parts[1].trim().toIntOrNull() ?: return@forEachIndexed
            val triggerMs = midnight + (h * 3600L + m * 60L) * 1000L
            if (triggerMs <= now) return@forEachIndexed

            val pendingIntent = buildPendingIntent(index, prayer.name)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            }
        }
    }

    fun cancelAllAlarms() {
        repeat(6) { index ->
            val pi = PendingIntent.getBroadcast(
                context, index,
                Intent(context, PrayerAlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { alarmManager.cancel(it) }
        }
    }

    private fun buildPendingIntent(index: Int, prayerName: String): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayerName)
            putExtra(PrayerAlarmReceiver.EXTRA_NOTIFICATION_ID, index)
        }
        return PendingIntent.getBroadcast(
            context, index, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
