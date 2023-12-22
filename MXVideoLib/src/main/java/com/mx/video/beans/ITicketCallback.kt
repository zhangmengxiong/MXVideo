package com.mx.video.beans

/**
 * 定时器回调
 */
internal interface ITicketCallback {
    suspend fun ticket()
}
