package com.mx.mxvideo_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mx.video.MXPlaySource
import com.mx.video.MXScale
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val cndVideos = arrayOf(
        "https://jzvd.nathen.cn/video/cfe6c30-1767b1bc21f-0007-1823-c86-de200.mp4",//三个不同分辨率
        "https://jzvd.nathen.cn/0339d49439f947419576c33a0aa51545/79e1a938b0d2435d85bd964a77640506-f4e986e3e38ed3f473f7ba82bc07e188-ld.mp4",
        "https://jzvd.nathen.cn/0339d49439f947419576c33a0aa51545/79e1a938b0d2435d85bd964a77640506-8924c7da92ebd789d315bc5de0a81059-fd.mp4",

        "https://jzvd.nathen.cn/video/59aa468b-1767b6d891e-0007-1823-c86-de200.mp4",//饺子还小
        "https://jzvd.nathen.cn/video/25ae1b1c-1767b2a5e44-0007-1823-c86-de200.mp4",//饺子还年轻
        "https://jzvd.nathen.cn/video/5a6465ff-1767b2a5e28-0007-1823-c86-de200.mp4"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        randPlay.setOnClickListener {
            mxVideoStd.setSource(
                MXPlaySource(
                    cndVideos.random(),
                    "/v/ldj/01-ldj.mp4"
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