package com.mx.video.utils

import android.content.Context
import android.provider.Settings
import kotlin.math.roundToInt

class MXBrightnessHelp(val context: Context) {
    init {
        getBrightness()
    }

    fun getMaxBrightness(): Int {
        return 100
    }

    fun getBrightness(): Int {
        var screenBrightness = (MXUtils.findWindows(context)?.attributes?.screenBrightness ?: -1f)
        if (screenBrightness < 0f) {
            screenBrightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                125
            ) / 255f
        }
        return (screenBrightness * 100).roundToInt()
    }

    fun setBrightness(brightness: Int) {
        var brightness = brightness / 100f
        if (brightness < 0f) brightness = 0f
        if (brightness > 1f) brightness = 1f

        val windows = MXUtils.findWindows(context)
        val params = windows?.attributes ?: return
        params.screenBrightness = brightness
        windows.attributes = params
    }

    fun release() {
    }
}