package com.mx.video.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.mx.video.R
import com.mx.video.beans.MXViewAnimator

internal object MXAnimatorHelp {
    private val ANIMATOR_TAG = R.id.mxPlayerRootLay
    fun show(view: View, duration: Long) {
        (view.getTag(ANIMATOR_TAG) as AnimatorSet?)?.cancel()

        view.visibility = View.VISIBLE
        val set = AnimatorSet()
        val alpha = ObjectAnimator.ofFloat(view, "alpha", view.alpha, 1f)
        val translate = ObjectAnimator.ofFloat(view, "translationY", view.translationY, 0f)
        set.duration = duration
        set.playTogether(alpha, translate)
        view.setTag(ANIMATOR_TAG, set)
        set.start()
    }

    fun hide(view: View,duration: Long, prop: MXViewAnimator) {
        (view.getTag(ANIMATOR_TAG) as AnimatorSet?)?.end()

        val set = AnimatorSet()
        val alpha = ObjectAnimator.ofFloat(view, "alpha", view.alpha, prop.hideToAlpha)
        val height = view.height * prop.hideToTranslation
        val translate = ObjectAnimator.ofFloat(view, "translationY", view.translationY, height)
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        set.duration = duration
        set.playTogether(alpha, translate)
        view.setTag(ANIMATOR_TAG, set)
        set.start()
    }
}