package com.mbf.jordan_prayer_times_app.di

import com.mbf.jordan_prayer_times_app.notification.NotificationHelper
import com.mbf.jordan_prayer_times_app.notification.PrayerAlarmScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val notificationModule = module {
    single { NotificationHelper(androidContext()) }
    single { PrayerAlarmScheduler(androidContext()) }
}
