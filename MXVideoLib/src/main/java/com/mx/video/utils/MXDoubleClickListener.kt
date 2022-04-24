package com.mx.video.utils

import android.os.Handler
import android.os.Looper
import android.view.View
import kotlin.math.abs

/**
 * 双击响应
 */
internal abstract class MXDoubleClickListener : View.OnClickListener {
    private val mHandler = Handler(Looper.getMainLooper())
    private var clickTime = 0L
    override fun onClick(p0: View?) {
        mHandler.removeCallbacksAndMessages(null)
        if (abs(System.currentTimeMillis() - clickTime) < 200L) {
            clickTime = 0L
            onDoubleClick()
        } else {
            clickTime = System.currentTimeMillis()
            mHandler.postDelayed(clickDelayRun, 200)
        }
    }

    private val clickDelayRun = Runnable {
        onClick()
    }

    abstract fun onClick()

    abstract fun onDoubleClick()
}