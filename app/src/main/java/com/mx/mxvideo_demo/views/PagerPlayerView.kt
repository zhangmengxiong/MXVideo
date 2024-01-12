package com.mx.mxvideo_demo.views

import android.content.Context
import android.util.AttributeSet
import com.mx.mxvideo_demo.R
import com.mx.video.MXVideoStd

class PagerPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : MXVideoStd(context, attrs) {
    override fun getLayoutId(): Int {
        return R.layout.pager_video_layout
    }
}