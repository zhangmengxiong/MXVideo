package com.mx.mxvideo_demo.apps

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.ldjVideos
import com.mx.mxvideo_demo.thumbnails
import com.mx.mxvideo_demo.titles
import com.mx.video.MXVideoStd
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScreen
import com.mx.video.player.IMXPlayer
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {
    private var playerClass: Class<out IMXPlayer>? = null

    private val mxVideoStd: MXVideoStd by lazy { this.findViewById(R.id.mxVideoStd) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        mxVideoStd.setOnEmptyPlayListener {
            Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setPlayer(playerClass)
            mxVideoStd.setSource(
                MXPlaySource(Uri.parse(ldjVideos.random()), titles.random()), seekTo = 0
            )
            mxVideoStd.startPlay()
        }

        startFullPlay.setOnClickListener {
            mxVideoStd.switchToScreen(MXScreen.FULL)
            Handler().postDelayed({
                mxVideoStd.switchToScreen(MXScreen.NORMAL)
                mxVideoStd.stopPlay()
            }, 10L * 1000)
        }
    }

    override fun onStart() {
        mxVideoStd.onStart()
        super.onStart()
    }

    override fun onStop() {
        mxVideoStd.onStop()
        super.onStop()
    }

    override fun onBackPressed() {
        if (mxVideoStd.currentScreen() == MXScreen.FULL) {
            mxVideoStd.switchToScreen(MXScreen.NORMAL)
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        mxVideoStd.release()
        super.onDestroy()
    }
}