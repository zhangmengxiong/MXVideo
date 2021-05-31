package com.mx.video.utils.touch

import android.view.View
import com.mx.video.R
import com.mx.video.utils.MXVolumeHelp
import com.mx.video.views.MXViewProvider
import kotlin.math.roundToInt

class VolumeTouchListener(private val provider: MXViewProvider) : MXTouchListener() {
    private val volumeHelp by lazy { MXVolumeHelp(provider.mxVideo.context) }
    private var maxVolume = 0
    private var startVolume = 0
    override fun onStart() {
        provider.mxVolumeLightLay.visibility = View.VISIBLE
        maxVolume = volumeHelp.getMaxVolume()
        startVolume = volumeHelp.getVolume()

        provider.mxVolumeLightTypeTxv.setText(R.string.mx_play_volume)
        val percent = (startVolume * 100.0 / maxVolume.toDouble()).roundToInt()
        provider.mxVolumeLightTxv.text = "${percent}%"
    }

    override fun onTouchMove(percent: Float) {
        var targetVolume = startVolume + (maxVolume * percent).toInt()
        if (targetVolume < 0) targetVolume = 0
        if (targetVolume > maxVolume) targetVolume = maxVolume

        volumeHelp.setVolume(targetVolume)
        val percent = (targetVolume * 100.0 / maxVolume.toDouble()).roundToInt()
        provider.mxVolumeLightTxv.text = "${percent}%"
    }

    override fun onEnd(percent: Float) {
        provider.mxVolumeLightLay.visibility = View.GONE
        var targetVolume = startVolume + (maxVolume * percent).toInt()
        if (targetVolume < 0) targetVolume = 0
        if (targetVolume > maxVolume) targetVolume = maxVolume

        volumeHelp.setVolume(targetVolume)
    }

    override fun release() {
        volumeHelp.release()
    }
}