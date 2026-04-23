package com.mbf.jordan_prayer_times_app

import android.app.Application
import com.mbf.jordan_prayer_times_app.di.networkModule
import com.mbf.jordan_prayer_times_app.di.notificationModule
import com.mbf.jordan_prayer_times_app.di.repositoryModule
import com.mbf.jordan_prayer_times_app.di.useCaseModule
import com.mbf.jordan_prayer_times_app.di.viewModelModule
import com.mbf.jordan_prayer_times_app.notification.NotificationHelper
import com.mbf.jordan_prayer_times_app.worker.PrayerDataRefreshScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(
                networkModule,
                repositoryModule,
                useCaseModule,
                viewModelModule,
                notificationModule
            )
        }

        NotificationHelper(this).createChannel()
        PrayerDataRefreshScheduler.ensureScheduled(this)
    }
}