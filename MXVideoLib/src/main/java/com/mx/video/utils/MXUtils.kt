package com.mx.video.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.mx.video.BuildConfig
import java.security.MessageDigest
import java.util.*

object MXUtils {
    private const val SP_KEY = "MX_SP_KEY"
    private val activityFlagMap = HashMap<String, Int?>()
    private val activityOrientationMap = HashMap<String, Int?>()

    fun log(any: Any) {
        if (BuildConfig.DEBUG) {
            Log.v(MXUtils::class.java.simpleName, any.toString())
        }
    }

    fun saveProgress(context: Context, uri: Uri, time: Int) {
        val sp = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE)
        sp.edit().putInt(uri.toString().md5(), time).apply()
    }

    fun getProgress(context: Context, uri: Uri): Int {
        val sp = context.getSharedPreferences(SP_KEY, Context.MODE_PRIVATE)
        return sp.getInt(uri.toString().md5(), 0)
    }

    private fun String.md5(): String {
        try {
            val instance = MessageDigest.getInstance("MD5")//获取md5加密对象
            val digest = instance.digest(this.toByteArray())//对字符串加密，返回字节数组
            val sb = StringBuffer()
            for (b in digest) {
                val i: Int = b.toInt() and 0xff//获取低八位有效值
                var hexString = Integer.toHexString(i)//将整数转化为16进制
                if (hexString.length < 2) {
                    hexString = "0$hexString"//如果是一位的话，补0
                }
                sb.append(hexString)
            }
            return sb.toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
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

    fun isWifiConnected(context: Context): Boolean {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun findActivity(context: Context?): Activity? {
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

    fun findWindowsDecorView(context: Context?): ViewGroup? {
        return findActivity(context)?.window?.decorView as ViewGroup?
    }

    /**
     * @param context 页面上下文
     * @param willChangeOrientation 是否需要更改页面方向
     */
    fun setFullScreen(context: Context?, willChangeOrientation: Boolean) {
        val activity = findActivity(context) ?: return
        val currentActivityId = activity.toString()

        activity.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        if (willChangeOrientation) {
            activityOrientationMap[currentActivityId] = activity.requestedOrientation
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activityOrientationMap.remove(currentActivityId)
        }

        var uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        val currentSystemVisibility = activity.window?.decorView?.systemUiVisibility
        activityFlagMap[currentActivityId] = currentSystemVisibility
        activity.window?.decorView?.systemUiVisibility = uiOptions
    }

    fun recoverFullScreen(context: Context?) {
        val activity = findActivity(context) ?: return
        val currentActivityId = activity.toString()

        activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        activityOrientationMap[currentActivityId]?.let {
            activity.requestedOrientation = it
        }

        activityFlagMap[currentActivityId]?.let {
            activity.window?.decorView?.setSystemUiVisibility(it)
        }
    }
}