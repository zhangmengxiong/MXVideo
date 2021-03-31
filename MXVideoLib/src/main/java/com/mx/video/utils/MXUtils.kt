package com.mx.video.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.Window
import com.mx.video.BuildConfig
import java.util.*

object MXUtils {
    fun log(any: Any) {
        if (BuildConfig.DEBUG) {
            Log.v(MXUtils::class.java.simpleName, any.toString())
        }
    }

    fun stringForTime(time: Int): String? {
        if (time <= 0 || time >= 24 * 60 * 60 * 1000) {
            return "00:00"
        }
        val seconds = (time % 60)
        val minutes = (time / 60 % 60)
        val hours = (time / 3600)
        val stringBuilder = StringBuilder()
        val mFormatter = Formatter(stringBuilder, Locale.getDefault())
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
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