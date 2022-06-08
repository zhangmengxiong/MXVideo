package com.mx.video.utils

import android.os.Handler
import android.os.Looper

class MXValueObservable<T>(defaultValue: T) {
    private val mHandler = Handler(Looper.getMainLooper())
    private val lock = Object()
    private val observerList = ArrayList<((value: T) -> Unit)>()

    private var _value: T = defaultValue

    fun set(value: T) {
        if (value == _value) {
            return
        }
        _value = value
        val list = synchronized(lock) {
            observerList.toMutableList()
        }
        if (list.isEmpty()) return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            list.forEach { it.invoke(value) }
        } else {
            mHandler.post {
                list.forEach { it.invoke(value) }
            }
        }
    }

    internal fun notifyChange() {
        val list = synchronized(lock) {
            observerList.toMutableList()
        }
        if (list.isEmpty()) return
        mHandler.post {
            list.forEach { it.invoke(_value) }
        }
    }

    fun get() = _value

    internal fun addObserver(o: ((value: T) -> Unit)?) {
        o ?: return
        synchronized(lock) {
            observerList.add(o)
        }
        mHandler.post { o.invoke(_value) }
    }

    internal fun deleteObserver(o: ((value: T) -> Unit)?) {
        o ?: return
        synchronized(lock) {
            observerList.remove(o)
        }
    }

    internal fun deleteObservers() {
        synchronized(lock) {
            mHandler.removeCallbacksAndMessages(null)
            observerList.clear()
        }
    }
}