package com.mx.mxvideo_demo.apps

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.ldjVideos
import com.mx.mxvideo_demo.player.MXIJKPlayer
import com.mx.mxvideo_demo.thumbnails
import com.mx.mxvideo_demo.titles
import com.mx.video.MXVideo
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXState
import com.mx.video.listener.MXVideoListener
import com.mx.video.views.MXViewSet
import kotlinx.android.synthetic.main.activity_full.*

class FullScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full)
        Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())

        mxVideoStd.setOnStateListener { state ->
            statusTxv.text = state.name
        }
        mxVideoStd.addOnVideoListener(object : MXVideoListener() {
            override fun onStateChange(state: MXState, viewSet: MXViewSet) {
                viewSet.mxReturnBtn.visibility = View.VISIBLE
                viewSet.mxReturnBtn.setOnClickListener {
                    onBackPressed()
                }
            }
        })

        // 屏蔽全屏按钮
        mxVideoStd.getConfig().showFullScreenButton.set(false)
        mxVideoStd.getConfig().gotoNormalScreenWhenComplete.set(false)
        mxVideoStd.getConfig().gotoNormalScreenWhenError.set(false)
        mxVideoStd.setPlayer(MXIJKPlayer::class.java)
        mxVideoStd.setSource(
            MXPlaySource(
                Uri.parse("https://1258108869.vod2.myqcloud.com/1696dab2vodcq1258108869/3b1520aa387702301056235269/f0.mp4"),
                titles.random(), changeOrientationWhenFullScreen = true
            ), seekTo = 0
        )
        mxVideoStd.startPlay()
        mxVideoStd.switchToScreen(MXScreen.FULL)
    }

    override fun onStart() {
        super.onStart()
        mxVideoStd.onStart()
    }

    override fun onStop() {
        mxVideoStd.onStop()
        super.onStop()
    }

    override fun onBackPressed() {
        mxVideoStd.release()
        super.onBackPressed()
    }

    override fun onDestroy() {
        MXVideo.releaseAll()
        super.onDestroy()
    }
}