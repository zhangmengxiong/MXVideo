package com.mx.video.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * 屏幕旋转监听
 */
class MXSensorHelp(val context: Context, private val minChangeTime: Long = 1500) {
    private val DATA_X = 0
    private val DATA_Y = 1
    private val DATA_Z = 2

    private val mHandler = Handler()
    private val sensorManager by lazy { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val sensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private var _preChangeTime = 0L
    private var isActive = false
    private var _rotationDegree: Int? = null

    fun getRotationDegree() = (_rotationDegree ?: 0)

    private var changeCall: ((degree: Int) -> Unit)? = null
    fun setRotationChangeCall(call: ((degree: Int) -> Unit)?) {
        changeCall = call
    }

    fun start() {
        if (isActive) return
        sensorManager.unregisterListener(sensorListener)
        sensorManager.registerListener(
            sensorListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        isActive = true
    }

    fun stop() {
        sensorManager.unregisterListener(sensorListener)
        isActive = false
    }

    fun release() {
        changeCall = null
        isActive = false
        sensorManager.unregisterListener(sensorListener)
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val values: FloatArray = event.values
            var orientation = 0
            val x = -values[DATA_X]
            val y = -values[DATA_Y]
            val z = -values[DATA_Z]
            val magnitude = x * x + y * y
            if (magnitude * 4 >= z * z) {
                val oneEightyOverPi = 57.29577957855f
                val angle = (atan2(-y.toDouble(), x.toDouble()) * oneEightyOverPi).toFloat()
                orientation = 90 - angle.roundToInt()
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360
                }
                while (orientation < 0) {
                    orientation += 360
                }
            }
            if (orientation in ((90 - 45) until (90 + 45))) {
                orientation = 90
            } else if (orientation in ((180 - 45) until (180 + 45))) {
                orientation = 180
            } else if (orientation in ((270 - 45) until (270 + 45))) {
                orientation = 270
            } else {
                orientation = 0
            }
            if (orientation != _rotationDegree) {
                sendDegreeChange(orientation)
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }

    private fun sendDegreeChange(degree: Int) {
        mHandler.removeCallbacksAndMessages(null)
        val sendRun = {
            _rotationDegree = degree
            changeCall?.invoke(degree)
            _preChangeTime = System.currentTimeMillis()
        }

        val delay = minChangeTime - abs(System.currentTimeMillis() - _preChangeTime)
        if (delay > 0) {
            mHandler.postDelayed(sendRun, delay)
        } else {
            sendRun.invoke()
        }
    }
}