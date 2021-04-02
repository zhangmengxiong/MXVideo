package com.mx.video.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

class MXTouchHelp(private val context: Context,private val mxConfig: MXConfig) {

    // 最低滑动距离
    private val minMoveDistance = ViewConfiguration.get(context).scaledTouchSlop * 2
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

    private var onSeekHorizontal: ((touchDownPercent: Float, percent: Float) -> Unit)? = null
    fun setHorizontalTouchCall(call: (touchDownPercent: Float, percent: Float) -> Unit) {
        onSeekHorizontal = call
    }

    private var onSeekVertical: ((touchDownPercent: Float, percent: Float) -> Unit)? = null
    fun setVerticalTouchCall(call: (touchDownPercent: Float, percent: Float) -> Unit) {
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
                        touchAction?.invoke(MotionEvent.ACTION_DOWN)
                    } else if (abs(dy) > minMoveDistance) {
                        isSeekVolume = true
                        touchAction?.invoke(MotionEvent.ACTION_DOWN)
                    }
                } else {
                    if (isSeekPosition) {
                        val dpx = downX / viewWidth
                        val px = motionEvent.x / viewWidth
                        onSeekHorizontal?.invoke(dpx, px)
                    } else if (isSeekVolume) {
                        val dpy = downY / viewHeight
                        val py = motionEvent.y / viewHeight
                        onSeekVertical?.invoke(dpy, py)
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