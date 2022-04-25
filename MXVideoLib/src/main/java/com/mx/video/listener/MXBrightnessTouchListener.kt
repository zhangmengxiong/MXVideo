package com.mx.video.listener

import android.content.Context
import com.mx.video.utils.MXBrightnessHelp

internal abstract class MXBrightnessTouchListener(context: Context) :
    MXBaseTouchListener {
    private val brightnessHelp = MXBrightnessHelp(context)
    private val maxBrightness = brightnessHelp.getMaxBrightness()
    private var startBrightness = 0
    override fun touchStart() {
        startBrightness = brightnessHelp.getBrightness()
        onSeekStart()
        onSeekChange(startBrightness, maxBrightness)
    }

    override fun touchMove(percent: Float) {
        val maxBrightness = brightnessHelp.getMaxBrightness()
        var targetBrightness = startBrightness + (maxBrightness * percent * 1.5).toInt()
        if (targetBrightness < 0) targetBrightness = 0
        if (targetBrightness > maxBrightness) targetBrightness = maxBrightness
        brightnessHelp.setBrightness(targetBrightness)
        onSeekChange(targetBrightness, maxBrightness)
    }

    override fun touchEnd(percent: Float) {
        var targetBrightness = startBrightness + (maxBrightness * percent * 1.5).toInt()
        if (targetBrightness < 0) targetBrightness = 0
        if (targetBrightness > maxBrightness) targetBrightness = maxBrightness
        brightnessHelp.setBrightness(targetBrightness)
        onSeekStop(targetBrightness, maxBrightness)
    }


    abstract fun onSeekStart()
    abstract fun onSeekChange(brightness: Int, maxBrightness: Int)
    abstract fun onSeekStop(brightness: Int, maxBrightness: Int)

    override fun release() {
        brightnessHelp.release()
    }
}