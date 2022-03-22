package com.mx.video.utils

import android.os.Handler
import android.os.Looper

class MXValueObservable<T>(defaultValue: T, private val debug: Boolean = false) {
    private val mHandler = Handler(Looper.getMainLooper())
    private val lock = Object()
    private val observerList = ArrayList<((value: T) -> Unit)>()

    private var _value: T = defaultValue

    fun set(value: T) {
        if (value == _value) {
            return
        }
        val old = _value
        _value = value
        if (debug) {
            MXUtils.log("属性变化：${old} -> $value")
        }
        val list = synchronized(lock) {
            observerList.toMutableList()
        }
        if (list.isEmpty()) return
        mHandler.post {
            list.forEach { it.invoke(value) }
        }
    }

    fun notifyChange() {
        val list = synchronized(lock) {
            observerList.toMutableList()
        }
        if (list.isEmpty()) return
        mHandler.post {
            list.forEach { it.invoke(_value) }
        }
    }

    fun get() = _value

    fun addObserver(o: ((value: T) -> Unit)?) {
        o ?: return
        synchronized(lock) {
            observerList.add(o)
        }
        mHandler.post { o.invoke(_value) }
    }

    fun deleteObserver(o: ((value: T) -> Unit)?) {
        o ?: return
        synchronized(lock) {
            observerList.remove(o)
        }
    }

    fun deleteObservers() {
        synchronized(lock) {
            observerList.clear()
        }
    }
}