package com.mx.mxvideo_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mx.video.MXPlaySource
import com.mx.video.MXVideoDisplay
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mxVideoStd.setSource(
            MXPlaySource(
                "http://8.136.101.204/v/ldj/01-ldj.mp4",
                "/v/ldj/01-ldj.mp4"
            )
        )

        fillTypeRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.fill) {
                mxVideoStd.setDisplayType(MXVideoDisplay.FILL_PARENT)
            } else {
                mxVideoStd.setDisplayType(MXVideoDisplay.CENTER_CROP)
            }
        }
    }
}