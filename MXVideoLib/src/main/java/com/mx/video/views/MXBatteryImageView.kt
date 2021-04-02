package com.mx.video.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.AttributeSet
import android.widget.ImageView
import com.mx.video.R
import java.lang.Exception

class MXBatteryImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    private val levelImage = arrayOf(
        R.drawable.mx_icon_battery_v1,
        R.drawable.mx_icon_battery_v2,
        R.drawable.mx_icon_battery_v3,
        R.drawable.mx_icon_battery_v4,
        R.drawable.mx_icon_battery_v5,
        R.drawable.mx_icon_battery_charge
    )

    init {
        setImageResource(levelImage[3])
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_BATTERY_CHANGED == action) {
                val level = intent.getIntExtra("level", 0)
                val scale = intent.getIntExtra("scale", 100)
                val percent = level * 100 / scale
                setLevel(percent)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onDetachedFromWindow() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
        }
        super.onDetachedFromWindow()
    }

    private fun setLevel(percent: Int) {
        setImageResource(levelImage.getOrNull(percent / 20) ?: levelImage[3])
    }
}