package com.mx.mxvideo_demo.apps

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.*
import com.mx.mxvideo_demo.player.MXIJKPlayer
import com.mx.video.MXVideo
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXState
import com.mx.video.utils.MXVideoListener
import com.mx.video.views.MXViewProvider
import kotlinx.android.synthetic.main.activity_normal.*

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

        mxVideoStd.addOnVideoListener(object : MXVideoListener() {
            override fun onStateChange(state: MXState, provider: MXViewProvider) {
                provider.mxReturnBtn.visibility = View.VISIBLE
                provider.mxReturnBtn.setOnClickListener {
                    onBackPressed()
                }
            }
        })

        // 屏蔽全屏按钮
        mxVideoStd.getConfig().canFullScreen.set(true)
        mxVideoStd.getConfig().showFullScreenButton.set(false)
        mxVideoStd.getConfig().gotoNormalScreenWhenComplete.set(false)
        mxVideoStd.getConfig().gotoNormalScreenWhenError.set(false)
        mxVideoStd.gotoFullScreen()
        mxVideoStd.setSource(
            MXPlaySource(Uri.parse(ldjVideos.first()), titles.random()),
            player = MXIJKPlayer::class.java, seekTo = 0
        )
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