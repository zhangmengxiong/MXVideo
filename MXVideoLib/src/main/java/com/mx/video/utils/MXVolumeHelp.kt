package com.mx.video.utils

import android.content.Context
import android.media.AudioManager

class MXVolumeHelp(context: Context) {
    private val mAudioManager =
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager


    companion object {
        private val audioFocus = AudioManager.OnAudioFocusChangeListener {

        }
    }

    init {
        mAudioManager.abandonAudioFocus(audioFocus)
    }

    fun getMaxVolume(): Int {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }

    fun getVolume(): Int {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun setVolume(volume: Int) {
        val max = getMaxVolume()
        val volume = kotlin.math.max(kotlin.math.min(max, volume), 0)
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }

    fun release() {
    }
}