package com.mbf.jordan_prayer_times_app.notification

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.mbf.jordan_prayer_times_app.complication.MainComplicationService
import com.mbf.jordan_prayer_times_app.data.local.CityPreferences

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!CityPreferences(context).isNotificationsEnabled()) return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        NotificationHelper(context).showPrayerNotification(prayerName, notificationId)
        ComplicationDataSourceUpdateRequester
            .create(context, ComponentName(context, MainComplicationService::class.java))
            .requestUpdateAll()
    }

    companion object {
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
