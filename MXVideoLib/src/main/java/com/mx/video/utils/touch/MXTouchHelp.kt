package com.mx.video.utils.touch

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.mx.video.listener.MXBaseTouchListener
import kotlin.math.abs

internal class MXTouchHelp(context: Context) {
    // 最低滑动距离
    private val minMoveDistance = ViewConfiguration.get(context).scaledTouchSlop * 2
    private var downX = 0f
    private var downY = 0f
    private var isSeekHorizontal = false
    private var isSeekVerticalLeft = false
    private var isSeekVerticalRight = false

    private var isInTouch = false

    private var viewWidth = 0
    private var viewHeight = 0
    fun setSize(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }

    var horizontalTouch: MXBaseTouchListener? = null
    var verticalLeftTouch: MXBaseTouchListener? = null
    var verticalRightTouch: MXBaseTouchListener? = null

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
                        horizontalTouch?.touchStart()
                        return true
                    } else if (abs(dy) > minMoveDistance) {
                        val dpx = downX / viewWidth
                        if (dpx < 0.5f) {
                            isSeekVerticalLeft = true
                            verticalLeftTouch?.touchStart()
                        } else {
                            isSeekVerticalRight = true
                            verticalRightTouch?.touchStart()
                        }
                        return true
                    }
                } else {
                    when {
                        isSeekHorizontal -> {
                            val px = (motionEvent.x - downX) / viewWidth
                            horizontalTouch?.touchMove(px)
                            return true
                        }
                        isSeekVerticalLeft -> {
                            val py = (downY - motionEvent.y) / viewHeight
                            verticalLeftTouch?.touchMove(py)
                            return true
                        }
                        isSeekVerticalRight -> {
                            val py = (downY - motionEvent.y) / viewHeight
                            verticalRightTouch?.touchMove(py)
                            return true
                        }
                    }
                }
            }
            MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                when {
                    isSeekHorizontal -> {
                        val px = (motionEvent.x - downX) / viewWidth
                        horizontalTouch?.touchEnd(px)
                        return true
                    }
                    isSeekVerticalLeft -> {
                        val py = (downY - motionEvent.y) / viewHeight
                        verticalLeftTouch?.touchEnd(py)
                        return true
                    }
                    isSeekVerticalRight -> {
                        val py = (downY - motionEvent.y) / viewHeight
                        verticalRightTouch?.touchEnd(py)
                        return true
                    }
                }

                isInTouch = false
            }
        }
        return false
    }

    fun release() {
        horizontalTouch?.release()
        verticalLeftTouch?.release()
        verticalRightTouch?.release()

        horizontalTouch = null
        verticalLeftTouch = null
        verticalRightTouch = null
        viewWidth = 0
        viewHeight = 0
    }
}