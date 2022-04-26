package com.mx.mxvideo_demo.apps

import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.*
import com.mx.mxvideo_demo.player.MXIJKPlayer
import com.mx.mxvideo_demo.player.exo.MXExoPlayer
import com.mx.video.MXVideo
import com.mx.video.beans.MXOrientation
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScale
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import kotlinx.android.synthetic.main.activity_normal.*
import java.util.*

class NormalActivity : AppCompatActivity() {
    private var playerClass: Class<out IMXPlayer>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)

        Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())
        mxVideoStd.setOnEmptyPlayListener {
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse("http://ynjt.obs.cn-north-4.myhuaweicloud.com:80/video/Record/tuiliu.danengshou.com/live/continuous_record/mp4/1650505735583_2022-04-21-01-49-40/2022-04-21-01-49-40.mp4"),
                    titles.random(),
                    isLooping = (canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            mxVideoStd.startPlay()
        }
        randPlay.setOnClickListener {
            Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(ldjVideos.random()),
                    titles.random(),
                    isLooping = (canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            mxVideoStd.startPlay()
        }
        randTo10SecPlay.setOnClickListener {
            Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(ldjVideos.random()),
                    titles.random(),
                    isLooping = (canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 60
            )
            mxVideoStd.startPlay()
        }
        preloadPlay.setOnClickListener {
            Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(ldjVideos.random()),
                    titles.random(),
                    isLooping = (canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            mxVideoStd.startPreload()
        }
        livePlay.setOnClickListener {
            Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse("http://1011.hlsplay.aodianyun.com/demo/game.flv"),
                    titles.random(), isLiveSource = true,
                    isLooping = (canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            mxVideoStd.startPlay()
        }
        fun stringForTime(time: Int): String {
            if (time <= 0 || time >= 24 * 60 * 60 * 1000) {
                return "00:00"
            }
            val seconds = (time % 60)
            val minutes = (time / 60 % 60)
            val hours = (time / 3600)
            val stringBuilder = StringBuilder()
            val mFormatter = Formatter(stringBuilder, Locale.getDefault())
            return if (hours > 0) {
                mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
            } else {
                mFormatter.format("%02d:%02d", minutes, seconds).toString()
            }
        }
        mxVideoStd.setOnPlayTicketListener { position, duration ->
            timeTxv.text = "${stringForTime(position)} / ${stringForTime(duration)}"
        }
        mxVideoStd.setOnVideoSizeListener { width, height ->
            sizeVideoTxv.text = "$width x $height"
        }
        mxVideoStd.setOnStateListener { state ->
            statusTxv.text = state.name
        }

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
            } else if (checkedId == R.id.playerEXO) {
                playerClass = MXExoPlayer::class.java
            } else {
                playerClass = MXSystemPlayer::class.java
            }
            mxVideoStd.setPlayer(playerClass)
        }
        playerRG.getChildAt(0)?.performClick()

        canLoopRG.getChildAt(1)?.performClick()

        fillTypeRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.fill) {
                mxVideoStd.setScaleType(MXScale.FILL_PARENT)
            } else {
                mxVideoStd.setScaleType(MXScale.CENTER_CROP)
            }
        }
        centerCrop.performClick()

        canShowBottomSeekBar.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canShowBottomSeekBar.set(checkedId == R.id.canShowBottomSeekBarTrue)
        }
        canShowBottomSeekBarTrue.performClick()

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
        ratio_16_9.performClick()

        rotationRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rotation0) {
                mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_0)
            } else if (checkedId == R.id.rotation90) {
                mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_90)
            } else if (checkedId == R.id.rotation180) {
                mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_180)
            } else if (checkedId == R.id.rotation270) {
                mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_270)
            }
        }
        rotation0.performClick()

        canSeekRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canSeekByUser.set(checkedId == R.id.canSeekTrue)
        }
        canSeekTrue.performClick()

        muteRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.setAudioMute(checkedId == R.id.muteTrue)
        }
        muteFalse.performClick()

        canFullRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canFullScreen.set(checkedId == R.id.canFullTrue)
        }
        canFullTrue.performClick()

        canShowSystemTimeRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canShowSystemTime.set(checkedId == R.id.canShowSystemTimeTrue)
        }
        canShowSystemTimeTrue.performClick()

        canShowBatteryImgRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canShowBatteryImg.set(checkedId == R.id.canShowBatteryImgTrue)
        }
        canShowBatteryImgTrue.performClick()

        showTipIfNotWifiRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().showTipIfNotWifi.set(checkedId == R.id.showTipIfNotWifiTrue)
        }
        showTipIfNotWifiFalse.performClick()

        gotoNormalScreenWhenCompleteRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().gotoNormalScreenWhenComplete.set(checkedId == R.id.gotoNormalScreenWhenCompleteTrue)
        }
        gotoNormalScreenWhenCompleteTrue.performClick()

        mirrorRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().mirrorMode.set(checkedId == R.id.mirrorTrue)
        }
        mirrorFalse.performClick()

        canShowSpeedRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().canShowNetSpeed.set(checkedId == R.id.canShowSpeedTrue)
        }
        canShowSpeedTrue.performClick()

        gotoNormalScreenWhenErrorRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().gotoNormalScreenWhenError.set(checkedId == R.id.gotoNormalScreenWhenErrorTrue)
        }
        gotoNormalScreenWhenErrorTrue.performClick()

        sensorRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().autoFullScreenBySensor.set(checkedId == R.id.sensorTrue)
        }
        sensorFalse.performClick()

        liveRetryRG.setOnCheckedChangeListener { group, checkedId ->
            mxVideoStd.getConfig().replayLiveSourceWhenError.set(checkedId == R.id.liveRetryTrue)
        }
        liveRetryFalse.performClick()
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