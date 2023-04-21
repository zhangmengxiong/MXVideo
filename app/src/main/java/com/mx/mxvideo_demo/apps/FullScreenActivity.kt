package com.mx.mxvideo_demo.apps

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.SourceItem
import com.mx.mxvideo_demo.player.MXIJKPlayer
import com.mx.video.MXVideo
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXSensorMode
import com.mx.video.beans.MXState
import com.mx.video.listener.MXVideoListener
import com.mx.video.views.MXViewSet
import kotlinx.android.synthetic.main.activity_full.mxVideoStd
import kotlinx.android.synthetic.main.activity_full.statusTxv

class FullScreenActivity : AppCompatActivity() {
    private val source = SourceItem.random16x9()
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full)
        Glide.with(this).load(source.img).into(mxVideoStd.getPosterImageView())

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
        mxVideoStd.getConfig().autoFullScreenBySensor.set(false)
        mxVideoStd.getConfig().gotoNormalScreenWhenComplete.set(false)
        mxVideoStd.getConfig().gotoNormalScreenWhenError.set(false)
        mxVideoStd.getConfig().fullScreenSensorMode.set(MXSensorMode.SENSOR_AUTO)
//        mxVideoStd.setPlayer(MXIJKPlayer::class.java)
        mxVideoStd.setSource(
            MXPlaySource(
                Uri.parse(source.url),
                source.name, isLiveSource = source.live()
            ), seekTo = 0
        )
        mxVideoStd.startPlay()
        mxVideoStd.switchToScreen(MXScreen.FULL)
    }

    override fun onStart() {
        super.onStart()
        MXVideo.onStart()
    }

    override fun onStop() {
        MXVideo.onStop()
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