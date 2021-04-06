package com.mx.mxvideo_demo.apps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.ldjVideos
import com.mx.mxvideo_demo.thumbnails
import com.mx.mxvideo_demo.titles
import com.mx.video.MXPlaySource
import com.mx.video.MXScale
import com.mx.video.MXVideo
import kotlinx.android.synthetic.main.activity_normal.*

class NormalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)
        mxVideoStd.setDimensionRatio(16 / 9.0)
        mxVideoStd.setSource(
            MXPlaySource(
                ldjVideos.random(),
                titles.random()
            ), start = false
        )
        Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())

        randPlay.setOnClickListener {
            mxVideoStd.startPlay()
        }
        fillTypeRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.fill) {
                mxVideoStd.setDisplayType(MXScale.FILL_PARENT)
            } else {
                mxVideoStd.setDisplayType(MXScale.CENTER_CROP)
            }
        }
        ratioRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.ratio_16_9) {
                mxVideoStd.setDimensionRatio(16.0 / 9.0)
            } else if (checkedId == R.id.ratio_4_3) {
                mxVideoStd.setDimensionRatio(4.0 / 3.0)
            } else {
                mxVideoStd.setDimensionRatio(0.0)
            }
        }
    }

    override fun onBackPressed() {
        if (MXVideo.isFullScreen()) {
            MXVideo.gotoNormalScreen()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        MXVideo.releaseAll()
        super.onDestroy()
    }
}