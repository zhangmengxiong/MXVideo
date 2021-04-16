package com.mx.video.utils

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import com.mx.video.beans.MXDegree
import com.mx.video.beans.MXSensorListener
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * 屏幕旋转监听
 */
class MXSensorHelp private constructor(
    private val application: Application,
    private val minChangeTime: Long = 2000
) {
    private val DATA_X = 0
    private val DATA_Y = 1
    private val DATA_Z = 2

    private val mHandler = Handler()
    private val sensorManager by lazy { application.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val sensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private var isStart = false
    private var _preChangeTime = 0L
    private var _degree: MXDegree = MXDegree.DEGREE_0

    fun getDegree() = _degree

    private val listener = ArrayList<MXSensorListener>()
    fun addListener(call: MXSensorListener) {
        if (!listener.contains(call)) {
            listener.add(call)
        }
        if (!isStart) {
            start()
        }
    }

    fun deleteListener(call: MXSensorListener) {
        listener.remove(call)
    }


    fun start() {
        sensorManager.unregisterListener(sensorListener)
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        isStart = true
    }

    fun release() {
        listener.clear()
        sensorManager.unregisterListener(sensorListener)
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val values: FloatArray = event.values
            var dg = 0
            val x = -values[DATA_X]
            val y = -values[DATA_Y]
            val z = -values[DATA_Z]
            val magnitude = x * x + y * y
            if (magnitude * 4 >= z * z) {
                val oneEightyOverPi = 57.29577957855f
                val angle = (atan2(-y.toDouble(), x.toDouble()) * oneEightyOverPi).toFloat()
                dg = 90 - angle.roundToInt()
                // normalize to 0 - 359 range
                while (dg >= 360) {
                    dg -= 360
                }
                while (dg < 0) {
                    dg += 360
                }
            }
            val degree = when (dg) {
                in ((90 - 45) until (90 + 45)) -> {
                    MXDegree.DEGREE_90
                }
                in ((180 - 45) until (180 + 45)) -> {
                    MXDegree.DEGREE_180
                }
                in ((270 - 45) until (270 + 45)) -> {
                    MXDegree.DEGREE_270
                }
                else -> {
                    MXDegree.DEGREE_0
                }
            }
            synchronized(this@MXSensorHelp) {
                if (degree != _degree) {
                    _degree = degree
                    sendDegreeChange(degree)
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }

    private fun sendDegreeChange(degree: MXDegree) {
        mHandler.removeCallbacksAndMessages(null)
        val sendRun = Runnable {
            listener.toList().forEach { it.onChange(degree) }
            _preChangeTime = System.currentTimeMillis()
        }

        val delay = minChangeTime - abs(System.currentTimeMillis() - _preChangeTime)
        if (delay > 0) {
            mHandler.postDelayed(sendRun, delay)
        } else {
            sendRun.run()
        }
    }

    companion object {
        private var _instance: MXSensorHelp? = null
        val instance: MXSensorHelp
            get() = _instance!!

        /**
         * 使用前初始化感应器，这里使用单例模式来创建
         */
        @Synchronized
        fun init(application: Application) {
            if (_instance == null) {
                _instance = MXSensorHelp(application)
            }
        }
    }
}