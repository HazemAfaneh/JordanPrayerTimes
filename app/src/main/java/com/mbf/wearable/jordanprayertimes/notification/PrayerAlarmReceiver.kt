package com.mbf.wearable.jordanprayertimes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mbf.wearable.jordanprayertimes.data.local.CityPreferences

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!CityPreferences(context).isNotificationsEnabled()) return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        NotificationHelper(context).showPrayerNotification(prayerName, notificationId)
    }

    companion object {
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
