package com.mx.video.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.BatteryManager
import android.util.AttributeSet
import android.widget.ImageView
import com.mx.video.R

internal class MXBatteryImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    private val levelImage = arrayOf(
        R.drawable.mx_video_icon_battery_v1,
        R.drawable.mx_video_icon_battery_v2,
        R.drawable.mx_video_icon_battery_v3,
        R.drawable.mx_video_icon_battery_v4,
        R.drawable.mx_video_icon_battery_v5,
        R.drawable.mx_video_icon_battery_charge
    )

    init {
        isFocusable = false
        isFocusableInTouchMode = false
        setImageResource(levelImage[3])
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_BATTERY_CHANGED == action) {
                val level = intent.getIntExtra("level", 0)
                val scale = intent.getIntExtra("scale", 100)
                val status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)
                val percent = (level.toDouble() / scale) * 100.0
                val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING)
                setLevel(percent.toInt(), isCharging)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (e: Exception) {
        }
    }

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }

    private fun setLevel(percent: Int, isCharging: Boolean) {
        val img = when {
            isCharging -> {
                R.drawable.mx_video_icon_battery_charge
            }
            percent in 0..15 -> {
                R.drawable.mx_video_icon_battery_v1
            }
            percent in 15..40 -> {
                R.drawable.mx_video_icon_battery_v2
            }
            percent in 40..65 -> {
                R.drawable.mx_video_icon_battery_v3
            }
            percent in 65..95 -> {
                R.drawable.mx_video_icon_battery_v4
            }
            else -> {
                R.drawable.mx_video_icon_battery_v5
            }
        }
        imageTintList = if (isCharging) {
            ColorStateList.valueOf(Color.GREEN)
        } else {
            ColorStateList.valueOf(resources.getColor(R.color.mx_video_color_main))
        }
        setImageResource(img)
    }

    fun release() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
        }
    }
}