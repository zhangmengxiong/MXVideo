package com.mx.video.utils

import com.mx.video.beans.IMXObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class MXObservable<T>(defaultValue: T) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val lock = Object()
    private val observerList = ArrayList<IMXObserver<T>>()
    private val active = AtomicBoolean(true)
    private var _value: T = defaultValue

    fun set(value: T) {
        if (value == _value) return
        _value = value
        scope.launch { notifyChangeSync() }
    }

    fun get() = _value

    internal suspend fun updateValue(value: T) {
        if (value == _value) return
        _value = value
        notifyChangeSync()
    }

    internal fun notifyChange() {
        scope.launch { notifyChangeSync() }
    }

    internal suspend fun notifyChangeSync() = withContext(Dispatchers.Main) {
        if (!active.get()) return@withContext
        val list = synchronized(lock) {
            observerList.toMutableList()
        }
        if (list.isEmpty()) return@withContext
        list.forEach { it.update(_value) }
    }

    internal fun addObserver(observer: IMXObserver<T>) {
        if (!active.get()) return
        synchronized(lock) {
            observerList.add(observer)
        }
        scope.launch { observer.update(_value) }
    }

    internal fun deleteObserver(observer: IMXObserver<T>) {
        if (!active.get()) return
        synchronized(lock) {
            observerList.remove(observer)
        }
    }

    internal fun release() {
        active.set(false)
        synchronized(lock) {
            observerList.clear()
        }
    }
}