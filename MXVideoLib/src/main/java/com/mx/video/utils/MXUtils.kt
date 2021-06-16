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
import com.mx.video.beans.MXOrientation
import java.util.*

object MXUtils {
    private val activityFlagMap = HashMap<String, Int?>()
    private val activityOrientationMap = HashMap<String, Int?>()

    private var _appContext: Context? = null
    val applicationContext: Context
        get() = _appContext!!
    private val historyDb by lazy { MXHistoryDB(applicationContext) }

    fun init(context: Context) {
        if (_appContext == null) {
            this._appContext = context.applicationContext
        }
    }

    private var isDebug = false
    fun setDebug(debug: Boolean) {
        isDebug = debug
    }

    fun log(any: Any) {
        if (isDebug || BuildConfig.DEBUG) {
            Log.v(MXUtils::class.java.simpleName, any.toString())
        }
    }

    /**
     * 清空所有播放进度
     */
    fun clearProgress() {
        historyDb.cleanAll()
    }

    /**
     * 保存播放度条
     */
    fun saveProgress(uri: Uri, time: Int) {
        historyDb.addPlayTime(uri.toString(), time)
    }

    /**
     * 获取播放进度
     */
    fun getProgress(uri: Uri?): Int {
        if (uri == null) return 0
        return historyDb.getPlayTime(uri.toString())
    }

    fun stringForTime(time: Int): String {
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
     * 变更屏幕方向
     */
    fun setScreenOrientation(
        context: Context,
        orientation: MXOrientation = MXOrientation.DEGREE_0
    ) {
        val activity = findActivity(context) ?: return
        val currentActivityId = activity.toString()
        if (!activityOrientationMap.containsKey(currentActivityId)) {
            activityOrientationMap[currentActivityId] = activity.requestedOrientation
        }
        activity.requestedOrientation = when (orientation) {
            MXOrientation.DEGREE_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            MXOrientation.DEGREE_90 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            MXOrientation.DEGREE_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            MXOrientation.DEGREE_270 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    /**
     * 设置屏幕方向为默认方向
     */
    fun recoverScreenOrientation(context: Context) {
        val activity = findActivity(context) ?: return
        val currentActivityId = activity.toString()
        activityOrientationMap[currentActivityId]?.let {
            activity.requestedOrientation = it
        }
    }

    /**
     * @param context 页面上下文
     */
    fun setFullScreen(context: Context) {
        val activity = findActivity(context) ?: return
        val currentActivityId = activity.toString()

        activity.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        var uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        val currentSystemVisibility = activity.window?.decorView?.systemUiVisibility
        if (!activityFlagMap.containsKey(currentActivityId)) {
            activityFlagMap[currentActivityId] = currentSystemVisibility
        }
        activity.window?.decorView?.systemUiVisibility = uiOptions
    }

    /**
     * 退出全屏
     */
    fun recoverFullScreen(context: Context) {
        val activity = findActivity(context) ?: return
        val currentActivityId = activity.toString()

        activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        activityFlagMap[currentActivityId]?.let {
            activity.window?.decorView?.setSystemUiVisibility(it)
        }
    }
}