package com.mx.video.utils

import android.content.Context
import android.media.AudioManager
import android.media.AudioTrack.getMaxVolume
import kotlin.math.roundToInt

class MXBrightnessHelp(val context: Context) {
    fun getMaxBrightness(): Int {
        return 100
    }

    fun getBrightness(): Int {
        return ((MXUtils.findWindows(context)?.attributes?.screenBrightness
            ?: 1f) * 100).roundToInt()
    }

    fun setBrightness(brightness: Int) {
        var brightness = brightness / 100f
        if (brightness < 0f) brightness = 0f
        if (brightness > 1f) brightness = 1f
        MXUtils.findWindows(context)?.attributes?.screenBrightness = brightness
    }
}