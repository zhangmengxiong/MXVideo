package com.mx.video.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.Window
import com.mx.video.BuildConfig

object MXUtils {
    fun log(any: Any) {
        if (BuildConfig.DEBUG) {
            Log.v(MXUtils::class.java.simpleName, any.toString())
        }
    }

    fun findActivity(context: Context?): Activity? {
        if (context == null) return null
        if (context is Activity) {
            return context
        } else if (context is ContextWrapper) {
            return findActivity(context.baseContext)
        }
        return null
    }

    fun findWindows(context: Context?): Window? {
        return findActivity(context)?.window
    }
}