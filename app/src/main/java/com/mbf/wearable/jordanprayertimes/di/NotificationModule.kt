package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.notification.NotificationHelper
import com.mbf.wearable.jordanprayertimes.notification.PrayerAlarmScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val notificationModule = module {
    single { NotificationHelper(androidContext()) }
    single { PrayerAlarmScheduler(androidContext()) }
}
