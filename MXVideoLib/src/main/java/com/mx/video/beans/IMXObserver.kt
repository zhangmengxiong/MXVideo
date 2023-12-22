package com.mx.video.beans

/**
 * 观察者回调监听接口
 */
internal interface IMXObserver<T> {
    suspend fun update(value: T)
}