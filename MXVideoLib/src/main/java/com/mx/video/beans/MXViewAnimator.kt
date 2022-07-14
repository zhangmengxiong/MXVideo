package com.mx.video.beans

internal data class MXViewAnimator constructor(
    val hideToTranslation: Float,
    val hideToAlpha: Float
) {
    companion object {
        val TOP = MXViewAnimator(-1f, 0.1f)
        val CENTER = MXViewAnimator(0f, 0.1f)
        val BOTTOM = MXViewAnimator(1f, 0.1f)
    }
}