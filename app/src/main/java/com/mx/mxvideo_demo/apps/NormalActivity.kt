package com.mx.mxvideo_demo.apps

import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.*
import com.mx.mxvideo_demo.player.MXIJKPlayer
import com.mx.video.MXPlaySource
import com.mx.video.MXScale
import com.mx.video.MXState
import com.mx.video.MXVideo
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import com.mx.video.utils.MXVideoListener
import kotlinx.android.synthetic.main.activity_normal.*

class NormalActivity : AppCompatActivity() {
    private var playerClass: Class<out IMXPlayer>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)
        randPlay.setOnClickListener {
            Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(ldjVideos.random()),
                    titles.random()
                ), clazz = playerClass
            )
            mxVideoStd.startPlay()
        }
        randTo10SecPlay.setOnClickListener {
            Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(ldjVideos.random()),
                    titles.random()
                ), seekTo = 60, clazz = playerClass
            )
            mxVideoStd.startPlay()
        }
        preloadPlay.setOnClickListener {
            Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(ldjVideos.random()),
                    titles.random()
                ), clazz = playerClass
            )
            mxVideoStd.startPreload()
        }
        mxVideoStd.addOnVideoListener(object : MXVideoListener() {
            override fun onStateChange(state: MXState) {
                Toast.makeText(this@NormalActivity, state.name, Toast.LENGTH_SHORT).show()
            }

            override fun onPlayTicket(position: Int, duration: Int) {
                println("MXUtils $position / $duration")
            }
        })

        videoSourceRG.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.source16x9 -> {
                    mxVideoStd.setSource(
                        MXPlaySource(Uri.parse(VIDEO_16x9), titles.random())
                    )
                }
                R.id.source4x3 -> {
                    mxVideoStd.setSource(
                        MXPlaySource(Uri.parse(VIDEO_4x3), titles.random())
                    )
                }
                R.id.source9x16 -> {
                    mxVideoStd.setSource(
                        MXPlaySource(Uri.parse(VIDEO_9x16), titles.random())
                    )
                }
            }
            mxVideoStd.startPlay()
        }

        playerRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.playerIJK) {
                playerClass = MXIJKPlayer::class.java
            } else {
                playerClass = MXSystemPlayer::class.java
            }
        }
        playerRG.getChildAt(0)?.performClick()

        centerCrop.performClick()
        fillTypeRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.fill) {
                mxVideoStd.setDisplayType(MXScale.FILL_PARENT)
            } else {
                mxVideoStd.setDisplayType(MXScale.CENTER_CROP)
            }
        }
        centerCrop.performClick()

        ratioRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.ratio_16_9) {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                mxVideoStd.layoutParams = lp
                mxVideoStd.setDimensionRatio(16.0 / 9.0)
            } else if (checkedId == R.id.ratio_4_3) {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                mxVideoStd.layoutParams = lp
                mxVideoStd.setDimensionRatio(4.0 / 3.0)
            } else if (checkedId == R.id.ratio200dp) {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (resources.displayMetrics.density * 200).toInt()
                )
                mxVideoStd.layoutParams = lp
                mxVideoStd.setDimensionRatio(0.0)
            } else {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                mxVideoStd.layoutParams = lp
                mxVideoStd.setDimensionRatio(0.0)
            }
        }
        ratioEmpty.performClick()

        rotationRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rotation0) {
                mxVideoStd.setTextureViewRotation(0)
            } else if (checkedId == R.id.rotation90) {
                mxVideoStd.setTextureViewRotation(90)
            } else if (checkedId == R.id.rotation180) {
                mxVideoStd.setTextureViewRotation(180)
            } else if (checkedId == R.id.rotation270) {
                mxVideoStd.setTextureViewRotation(270)
            }
        }
        rotation0.performClick()

        canSeekRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canSeekByUser = (checkedId == R.id.canSeekTrue)
        }
        canSeekTrue.performClick()

        canFullRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canFullScreen = (checkedId == R.id.canFullTrue)
        }
        canFullTrue.performClick()

        canShowSystemTimeRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canShowSystemTime = (checkedId == R.id.canShowSystemTimeTrue)
        }
        canShowSystemTimeTrue.performClick()

        canShowBatteryImgRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canShowBatteryImg = (checkedId == R.id.canShowBatteryImgTrue)
        }
        canShowBatteryImgTrue.performClick()

        showTipIfNotWifiRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().showTipIfNotWifi = (checkedId == R.id.showTipIfNotWifiTrue)
        }
        showTipIfNotWifiTrue.performClick()

        gotoNormalScreenWhenCompleteRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().gotoNormalScreenWhenComplete =
                (checkedId == R.id.gotoNormalScreenWhenCompleteTrue)
        }
        gotoNormalScreenWhenCompleteTrue.performClick()

        gotoNormalScreenWhenErrorRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().gotoNormalScreenWhenError =
                (checkedId == R.id.gotoNormalScreenWhenErrorTrue)
        }
        gotoNormalScreenWhenErrorTrue.performClick()
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