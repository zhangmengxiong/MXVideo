package com.mx.video.listener

import android.content.Context
import com.mx.video.utils.MXVolumeHelp

internal abstract class MXVolumeTouchListener(context: Context) : MXBaseTouchListener {
    private val volumeHelp = MXVolumeHelp(context)
    private val maxVolume = volumeHelp.getMaxVolume()
    private var startVolume = 0
    override fun touchStart() {
        startVolume = volumeHelp.getVolume()
        onSeekStart()
        onSeekChange(startVolume, maxVolume)
    }

    override fun touchMove(percent: Float) {
        var targetVolume = startVolume + (maxVolume * percent * 1.5).toInt()
        if (targetVolume < 0) targetVolume = 0
        if (targetVolume > maxVolume) targetVolume = maxVolume
        volumeHelp.setVolume(targetVolume)
        onSeekChange(targetVolume, maxVolume)
    }

    override fun touchEnd(percent: Float) {
        var targetVolume = startVolume + (maxVolume * percent * 1.5).toInt()
        if (targetVolume < 0) targetVolume = 0
        if (targetVolume > maxVolume) targetVolume = maxVolume
        volumeHelp.setVolume(targetVolume)
        onSeekStop(targetVolume, maxVolume)
    }

    abstract fun onSeekStart()
    abstract fun onSeekChange(volume: Int, maxVolume: Int)
    abstract fun onSeekStop(volume: Int, maxVolume: Int)

    override fun release() {
        volumeHelp.release()
    }
}