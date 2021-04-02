package com.mx.video.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.AttributeSet
import android.widget.ImageView
import com.mx.video.R
import com.mx.video.utils.MXUtils
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
                val status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)
                val percent = (level.toDouble() / scale) * 100.0
                val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING)
                MXUtils.log("percent=$percent  isCharging=$isCharging")
                setLevel(percent.toInt(), isCharging)
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

    private fun setLevel(percent: Int, isCharging: Boolean) {
        val img = when {
            isCharging -> {
                R.drawable.mx_icon_battery_charge
            }
            percent in 0..15 -> {
                R.drawable.mx_icon_battery_v1
            }
            percent in 15..40 -> {
                R.drawable.mx_icon_battery_v2
            }
            percent in 40..65 -> {
                R.drawable.mx_icon_battery_v3
            }
            percent in 65..95 -> {
                R.drawable.mx_icon_battery_v4
            }
            else -> {
                R.drawable.mx_icon_battery_v5
            }
        }
        setImageResource(img)
    }
}