package com.mx.video.utils.touch

import android.view.View
import com.mx.video.beans.MXState
import com.mx.video.utils.MXDelay
import com.mx.video.utils.MXUtils
import com.mx.video.views.MXViewProvider
import kotlin.math.min

class SeekTouchListener(private val provider: MXViewProvider, private val timeDelay: MXDelay) :
    MXTouchListener() {
    override fun touchStart() {
        if (!provider.config.sourceCanSeek() || provider.config.state.get() != MXState.PLAYING) return
        provider.allContentView.forEach {
            val show = (it == provider.mxQuickSeekLay)
            provider.setViewShow(it, show)
        }
    }

    override fun touchMove(percent: Float) {
        if (!provider.config.sourceCanSeek() || provider.config.state.get() != MXState.PLAYING) return
        val duration = provider.mxVideo.getDuration()
        var position =
            provider.mxVideo.getCurrentPosition() + (min(120, duration) * percent).toInt()
        if (position < 0) position = 0
        if (position > duration) position = duration

        provider.mxQuickSeekCurrentTxv.text = MXUtils.stringForTime(position)
        provider.mxQuickSeekMaxTxv.text = MXUtils.stringForTime(duration)
        provider.mxBottomSeekProgress.progress = position
    }

    override fun touchEnd(percent: Float) {
        provider.mxQuickSeekLay.visibility = View.GONE
        if (!provider.config.sourceCanSeek() || provider.config.state.get() != MXState.PLAYING) return

        val duration = provider.mxVideo.getDuration()
        var position =
            provider.mxVideo.getCurrentPosition() + (min(120, duration) * percent).toInt()
        if (position < 0) position = 0
        if (position > duration) position = duration

        provider.mxVideo.seekTo(position)
        timeDelay.start()
    }

    override fun release() {
    }
}