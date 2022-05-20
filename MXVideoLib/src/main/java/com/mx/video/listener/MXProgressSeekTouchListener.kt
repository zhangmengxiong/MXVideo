package com.mx.video.listener

import com.mx.video.beans.MXConfig
import com.mx.video.beans.MXState
import com.mx.video.utils.MXValueObservable
import kotlin.math.min

internal abstract class MXProgressSeekTouchListener(
    private val config: MXConfig,
    private val position: MXValueObservable<Pair<Int, Int>>
) : MXBaseTouchListener {
    override fun touchStart() {
        if (!config.sourceCanSeek() || config.state.get() != MXState.PLAYING) return
        onSeekStart()
    }

    override fun touchMove(percent: Float) {
        if (!config.sourceCanSeek() || config.state.get() != MXState.PLAYING) return
        val duration = position.get().second
        var position = position.get().first + (min(120, duration) * percent).toInt()
        if (position < 0) position = 0
        if (position > duration) position = duration
        onSeekChange(position, duration)
    }

    override fun touchEnd(percent: Float) {
        if (!config.sourceCanSeek() || config.state.get() != MXState.PLAYING) return
        val duration = position.get().second
        var position = position.get().first + (min(120, duration) * percent).toInt()
        if (position < 0) position = 0
        if (position > duration) position = duration

        onSeekStop(position, duration)
    }

    abstract fun onSeekStart()
    abstract fun onSeekChange(position: Int, duration: Int)
    abstract fun onSeekStop(position: Int, duration: Int)

    override fun release() {
    }
}