package com.mx.video.listener

import android.view.View
import kotlin.math.abs

/**
 * 双击响应
 */
internal abstract class MXDoubleClickListener : View.OnClickListener {
    private var clickTime = 0L
    override fun onClick(p0: View?) {
        onClick()

        val now = System.currentTimeMillis()
        if (abs(now - clickTime) < 500L) {
            clickTime = 0L
            onDoubleClick()
        } else {
            clickTime = System.currentTimeMillis()
        }
    }

    abstract fun onClick()

    abstract fun onDoubleClick()
}