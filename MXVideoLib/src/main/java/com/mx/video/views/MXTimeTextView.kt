package com.mx.video.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.widget.TextView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MXTimeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {
    private val mHandler = Handler()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        text = "00:00"
    }

    private val ticketRun = object : Runnable {
        override fun run() {
            try {
                text = dateFormat.format(Date(System.currentTimeMillis()))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mHandler.postDelayed(this, 3000)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mHandler.post(ticketRun)
    }

    override fun onDetachedFromWindow() {
        mHandler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }
}