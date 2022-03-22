package com.mx.video.beans

data class MXSize(val width: Int, val height: Int) {
    fun clone(): MXSize {
        return MXSize(width, height)
    }

    override fun toString(): String {
        return "($width,$height)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MXSize

        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        return result
    }
}