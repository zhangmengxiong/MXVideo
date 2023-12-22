package com.mx.mxvideo_demo

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.mx.video.MXVideo

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MXVideo.setDebug(true)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}