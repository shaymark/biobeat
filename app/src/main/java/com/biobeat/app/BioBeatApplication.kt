package com.biobeat.app

import android.app.Application
import com.biobeat.sdk.BioBeatSdk
import com.biobeat.sdk.BioBeatSdkConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BioBeatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        BioBeatSdk.init(
            context = this,
            config = BioBeatSdkConfig(
                autoReconnect = true,
                logLevel = if (BuildConfig.DEBUG) {
                    BioBeatSdkConfig.LogLevel.DEBUG
                } else {
                    BioBeatSdkConfig.LogLevel.WARN
                },
            ),
        )
    }
}
