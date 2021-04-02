package com.mx.video.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

class MXTouchHelp(private val context: Context, private val mxConfig: MXConfig) {

    // 最低滑动距离
    private val minMoveDistance = ViewConfiguration.get(context).scaledTouchSlop * 2
    private var downX = 0f
    private var downY = 0f
    private var isSeekHorizontal = false
    private var isSeekVerticalLeft = false
    private var isSeekVerticalRight = false

    private var isInTouch = false
    fun isInActive() = isInTouch

    private var viewWidth = 0
    private var viewHeight = 0
    fun setSize(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }

    private var onHorizontalListener: OnMXTouchListener? = null
    fun setHorizontalTouchCall(call: OnMXTouchListener?) {
        onHorizontalListener = call
    }

    private var onVerticalLeftListener: OnMXTouchListener? = null
    fun setVerticalLeftTouchCall(call: OnMXTouchListener) {
        onVerticalLeftListener = call
    }

    private var onVerticalRightListener: OnMXTouchListener? = null
    fun setVerticalRightTouchCall(call: OnMXTouchListener) {
        onVerticalRightListener = call
    }

    fun onTouch(motionEvent: MotionEvent) {
        if (viewWidth == 0 || viewHeight == 0) return
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                isInTouch = true
                downX = motionEvent.x
                downY = motionEvent.y
                isSeekHorizontal = false
                isSeekVerticalLeft = false
                isSeekVerticalRight = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dpx = downX / viewWidth
                val dpy = downY / viewHeight
                if (!isSeekHorizontal && !isSeekVerticalLeft && !isSeekVerticalRight) {
                    val dx = motionEvent.x - downX
                    val dy = motionEvent.y - downY
                    if (abs(dx) > minMoveDistance) {
                        isSeekHorizontal = true
                        onHorizontalListener?.onStart(dpx)
                    } else if (abs(dy) > minMoveDistance) {
                        if (dpy < 0.5f) {
                            isSeekVerticalLeft = true
                            onVerticalLeftListener?.onStart(dpy)
                        } else {
                            isSeekVerticalRight = true
                            onVerticalRightListener?.onStart(dpy)
                        }
                    }
                } else {
                    when {
                        isSeekHorizontal -> {
                            val px = motionEvent.x / viewWidth
                            onHorizontalListener?.onTouchMove(px)
                        }
                        isSeekVerticalLeft -> {
                            val py = motionEvent.y / viewHeight
                            onVerticalLeftListener?.onTouchMove(py)
                        }
                        isSeekVerticalRight -> {
                            val py = motionEvent.y / viewHeight
                            onVerticalRightListener?.onTouchMove(py)
                        }
                    }
                }
            }
            MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_UP -> {
                when {
                    isSeekHorizontal -> {
                        val px = motionEvent.x / viewWidth
                        onHorizontalListener?.onEnd(px)
                    }
                    isSeekVerticalLeft -> {
                        val py = motionEvent.y / viewHeight
                        onVerticalLeftListener?.onEnd(py)
                    }
                    isSeekVerticalRight -> {
                        val py = motionEvent.y / viewHeight
                        onVerticalRightListener?.onEnd(py)
                    }
                }

                isInTouch = false
            }
        }
    }

    open class OnMXTouchListener {
        protected var touchDownPercent: Float = 0f
        open fun onStart(touchDownPercent: Float) {
            this.touchDownPercent = touchDownPercent
        }

        open fun onTouchMove(percent: Float) {}
        open fun onEnd(percent: Float) {}
    }
}