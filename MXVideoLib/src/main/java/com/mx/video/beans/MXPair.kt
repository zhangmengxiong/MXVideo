package com.mx.video.beans

import java.io.Serializable


data class MXPair<out A, out B>(val first: A, val second: B) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MXPair<*, *>

        if (first != other.first) return false
        if (second != other.second) return false

        return true
    }

    override fun hashCode(): Int {
        var result = first?.hashCode() ?: 0
        result = 31 * result + (second?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "($first, $second)"
}