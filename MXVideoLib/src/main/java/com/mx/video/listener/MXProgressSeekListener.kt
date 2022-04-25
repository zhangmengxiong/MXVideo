package com.mx.video.listener

import android.widget.SeekBar
import com.mx.video.beans.MXConfig
import com.mx.video.utils.MXUtils

internal abstract class MXProgressSeekListener(private val config: MXConfig) :
    SeekBar.OnSeekBarChangeListener {
    var touchProgress = 0
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser && config.sourceCanSeek()) {
            this.touchProgress = progress
            onSeekChange(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        if (!config.sourceCanSeek()) return
        MXUtils.log("onStartTrackingTouch")
        this.touchProgress = seekBar?.progress ?: return
        onSeekStart()
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (!config.sourceCanSeek()) return
        MXUtils.log("onStopTrackingTouch")
        onSeekChange(touchProgress)
        onSeekStop(touchProgress)
    }

    abstract fun onSeekStart()
    abstract fun onSeekChange(seekTo: Int)
    abstract fun onSeekStop(seekTo: Int)
}