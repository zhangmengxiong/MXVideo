package com.mx.video.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

class MXTouchHelp(private val context: Context) {
    // 最低滑动距离
    private val minMoveDistance = ViewConfiguration.get(context).scaledTouchSlop
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

    fun onTouch(motionEvent: MotionEvent): Boolean {
        if (viewWidth == 0 || viewHeight == 0) return false
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
                if (!isSeekHorizontal && !isSeekVerticalLeft && !isSeekVerticalRight) {
                    val dx = motionEvent.x - downX
                    val dy = motionEvent.y - downY
                    if (abs(dx) > minMoveDistance) {
                        isSeekHorizontal = true
                        onHorizontalListener?.onStart()
                        return true
                    } else if (abs(dy) > minMoveDistance) {
                        val dpx = downX / viewWidth
                        if (dpx < 0.5f) {
                            isSeekVerticalLeft = true
                            onVerticalLeftListener?.onStart()
                        } else {
                            isSeekVerticalRight = true
                            onVerticalRightListener?.onStart()
                        }
                        return true
                    }
                } else {
                    when {
                        isSeekHorizontal -> {
                            val px = (motionEvent.x - downX) / viewWidth
                            onHorizontalListener?.onTouchMove(px)
                            return true
                        }
                        isSeekVerticalLeft -> {
                            val py = (downY - motionEvent.y) / viewHeight
                            onVerticalLeftListener?.onTouchMove(py)
                            return true
                        }
                        isSeekVerticalRight -> {
                            val py = (downY - motionEvent.y) / viewHeight
                            onVerticalRightListener?.onTouchMove(py)
                            return true
                        }
                    }
                }
            }
            MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_UP -> {
                when {
                    isSeekHorizontal -> {
                        val px = (motionEvent.x - downX) / viewWidth
                        onHorizontalListener?.onEnd(px)
                        return true
                    }
                    isSeekVerticalLeft -> {
                        val py = (downY - motionEvent.y) / viewHeight
                        onVerticalLeftListener?.onEnd(py)
                        return true
                    }
                    isSeekVerticalRight -> {
                        val py = (downY - motionEvent.y) / viewHeight
                        onVerticalRightListener?.onEnd(py)
                        return true
                    }
                }

                isInTouch = false
            }
        }
        return false
    }

    open class OnMXTouchListener {
        open fun onStart() {}
        open fun onTouchMove(percent: Float) {}
        open fun onEnd(percent: Float) {}
    }
}