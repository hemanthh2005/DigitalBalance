package com.digitalbalance.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DigitalBalanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
