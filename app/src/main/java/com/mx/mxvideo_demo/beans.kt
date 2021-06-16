package com.mx.mxvideo_demo

data class HomePages(val title: String, val clazz: Class<*>?, val action: (() -> Unit)? = null)