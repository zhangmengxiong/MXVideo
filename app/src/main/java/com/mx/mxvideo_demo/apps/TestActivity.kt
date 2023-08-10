package com.mx.mxvideo_demo.apps

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.SourceItem
import com.mx.mxvideo_demo.player.exo.MXExoPlayer
import com.mx.video.MXVideoStd
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScreen
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import kotlinx.android.synthetic.main.activity_test.startFullPlay

class TestActivity : AppCompatActivity() {
    private var playerClass: Class<out IMXPlayer>? = null

    private val mxVideoStd: MXVideoStd by lazy { this.findViewById(R.id.mxVideoStd) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        mxVideoStd.setOnErrorListener { source, message ->
            println("播放錯誤：$message --> $source")
        }
        mxVideoStd.setOnEmptyPlayListener {
            val source = SourceItem.random16x9()
            Glide.with(this).load(source.img).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setPlayer(playerClass)
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse("https://media6.smartstudy.com/ae/07/3997/2/dest.m3u8"),
                    source.name,
                    isLiveSource = source.live()
                ),
                seekTo = 0
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

        var preTime = 0L
        var prePosition = 0
        mxVideoStd.setOnPlayTicketListener { position, duration ->
            val positionDiff = position - prePosition
            val now = System.currentTimeMillis()
            if (prePosition <= 0) {
                preTime = now
                prePosition = position
                return@setOnPlayTicketListener
            }
            if (positionDiff < 15) return@setOnPlayTicketListener
            val diff = (now - preTime) / 1000f
            println("TIME -> diff = $diff  positionDiff=$positionDiff  position=$position / $duration ")
            preTime = now
            prePosition = position
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