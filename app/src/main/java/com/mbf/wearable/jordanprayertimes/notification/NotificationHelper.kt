package com.mbf.wearable.jordanprayertimes.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mbf.wearable.jordanprayertimes.R

class NotificationHelper(private val context: Context) {

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "أوقات الصلاة",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات مواعيد الصلاة"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showPrayerNotification(prayerName: String, notificationId: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("وقت الصلاة")
            .setContentText("حان الان موعد اذان $prayerName")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "prayer_times_channel"
    }
}
