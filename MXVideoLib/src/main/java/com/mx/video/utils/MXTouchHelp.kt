package com.mx.video.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

class MXTouchHelp(private val context: Context) {    // 最低滑动距离
    private val minMoveDistance = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f
    private var isSeekPosition = false
    private var isSeekVolume = false

    private var isInTouch = false
    fun isInActive() = isInTouch

    private var viewWidth = 0
    private var viewHeight = 0
    fun setSize(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }

    private var touchAction: ((Int) -> Unit)? = null
    fun setOnTouchAction(call: (Int) -> Unit) {
        touchAction = call
    }

    private var onSeekHorizontal: ((percent: Float) -> Unit)? = null
    fun setHorizontalTouchCall(call: (percent: Float) -> Unit) {
        onSeekHorizontal = call
    }

    private var onSeekVertical: ((percent: Float) -> Unit)? = null
    fun setVerticalTouchCall(call: (percent: Float) -> Unit) {
        onSeekVertical = call
    }

    fun onTouch(motionEvent: MotionEvent) {
        if (viewWidth == 0 || viewHeight == 0) return
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                isInTouch = true
                downX = motionEvent.x
                downY = motionEvent.y
                isSeekPosition = false
                isSeekVolume = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = motionEvent.x - downX
                val dy = motionEvent.y - downY

                if (!isSeekPosition && !isSeekVolume) {
                    if (abs(dx) > minMoveDistance) {
                        isSeekPosition = true
                    } else if (abs(dy) > minMoveDistance) {
                        isSeekVolume = true
                    }
                    touchAction?.invoke(MotionEvent.ACTION_DOWN)
                } else {
                    if (isSeekPosition) {
                        val px = dx / viewWidth
                        onSeekHorizontal?.invoke(px)
                    } else if (isSeekVolume) {
                        val py = dy / viewHeight
                        onSeekVertical?.invoke(py)
                    }
                    touchAction?.invoke(MotionEvent.ACTION_MOVE)
                }
            }
            MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_UP -> {
                isInTouch = false
                touchAction?.invoke(MotionEvent.ACTION_UP)
                isSeekPosition = false
                isSeekVolume = false
            }
        }
    }

}