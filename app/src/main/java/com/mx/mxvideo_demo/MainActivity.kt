package com.mx.mxvideo_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mx.video.MXPlaySource
import com.mx.video.MXScale
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        randPlay.setOnClickListener {
            mxVideoStd.setSource(
                MXPlaySource(
                    ldjVideos.random(),
                    titles.random()
                )
            )
        }
        fillTypeRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.fill) {
                mxVideoStd.setDisplayType(MXScale.FILL_PARENT)
            } else {
                mxVideoStd.setDisplayType(MXScale.CENTER_CROP)
            }
        }
    }

    override fun onDestroy() {
        mxVideoStd.stopPlay()
        super.onDestroy()
    }
}