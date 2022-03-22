package com.mx.video.utils.touch

import android.view.View
import com.mx.video.R
import com.mx.video.utils.MXBrightnessHelp
import com.mx.video.views.MXViewProvider
import kotlin.math.roundToInt

class BrightnessTouchListener(private val provider: MXViewProvider) : MXTouchListener() {
    private val brightnessHelp by lazy { MXBrightnessHelp(provider.mxVideo.context) }
    private var maxBrightness = 0
    private var startBrightness = 0
    override fun touchStart() {
        provider.mxVolumeLightLay.visibility = View.VISIBLE
        maxBrightness = brightnessHelp.getMaxBrightness()
        startBrightness = brightnessHelp.getBrightness()

        provider.mxVolumeLightTypeTxv.setText(R.string.mx_play_brightness)
        val percent = (startBrightness * 100.0 / maxBrightness.toDouble()).roundToInt()
        provider.mxVolumeLightTxv.text = "${percent}%"
    }

    override fun touchMove(percent: Float) {
        val maxBrightness = brightnessHelp.getMaxBrightness()
        var targetBrightness = startBrightness + (maxBrightness * percent * 0.7).toInt()
        if (targetBrightness < 0) targetBrightness = 0
        if (targetBrightness > maxBrightness) targetBrightness = maxBrightness

        brightnessHelp.setBrightness(targetBrightness)
        val percent = (targetBrightness * 100.0 / maxBrightness.toDouble()).roundToInt()
        provider.mxVolumeLightTxv.text = "${percent}%"
    }

    override fun touchEnd(percent: Float) {
        provider.mxVolumeLightLay.visibility = View.GONE
        var targetBrightness = startBrightness + (maxBrightness * percent * 0.7).toInt()
        if (targetBrightness < 0) targetBrightness = 0
        if (targetBrightness > maxBrightness) targetBrightness = maxBrightness

        brightnessHelp.setBrightness(targetBrightness)
    }

    override fun release() {
        brightnessHelp.release()
    }
}