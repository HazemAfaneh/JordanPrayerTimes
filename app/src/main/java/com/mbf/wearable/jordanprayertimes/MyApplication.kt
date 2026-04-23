package com.mbf.wearable.jordanprayertimes

import android.app.Application
import com.mbf.wearable.jordanprayertimes.di.networkModule
import com.mbf.wearable.jordanprayertimes.di.notificationModule
import com.mbf.wearable.jordanprayertimes.di.repositoryModule
import com.mbf.wearable.jordanprayertimes.di.useCaseModule
import com.mbf.wearable.jordanprayertimes.di.viewModelModule
import com.mbf.wearable.jordanprayertimes.notification.NotificationHelper
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
    }
}