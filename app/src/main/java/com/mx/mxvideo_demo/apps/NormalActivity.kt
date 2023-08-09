package com.mx.mxvideo_demo.apps

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.mx.dialog.MXDialog
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.SourceItem
import com.mx.mxvideo_demo.player.MXAliPlayer
import com.mx.mxvideo_demo.player.MXIJKPlayer
import com.mx.mxvideo_demo.player.exo.MXExoPlayer
import com.mx.video.MXVideo
import com.mx.video.beans.MXOrientation
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScale
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import kotlinx.android.synthetic.main.activity_normal.*
import java.util.Formatter
import java.util.Locale
import kotlin.math.roundToInt

class NormalActivity : AppCompatActivity() {
    private var playerClass: Class<out IMXPlayer>? = null
    private var currentSource: SourceItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)

        sourceBtn.setOnClickListener {
            val list = SourceItem.all()
            val currentIndex = list.indexOf(currentSource)
            MXDialog.select(this, list.mapIndexed { index, item ->
                "$index： ${item.type} - ${item.name} - ${item.url}"
            }, selectIndex = currentIndex) { index ->
                val item = list.getOrNull(index) ?: return@select
                currentSource = item
                sourceBtn.text = "${item.type} - ${item.name} - ${item.url}"
            }
        }
        Glide.with(this).load(SourceItem.random16x9().img).into(mxVideoStd.getPosterImageView())


        mxVideoStd.setOnEmptyPlayListener {
            val source = SourceItem.all().first()
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(source.url),
                    source.name,
                    isLiveSource = source.live(),
                    isLooping = (canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            mxVideoStd.startPlay()
        }
        startPlayBtn.setOnClickListener {
            val source = currentSource ?: SourceItem.random16x9()
            Glide.with(this).load(source.img).into(mxVideoStd.getPosterImageView())
            mxVideoStd.setPlayer(playerClass)
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(source.url),
                    source.name, isLiveSource = source.live(),
                    isLooping = (canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            mxVideoStd.startPlay()
        }

        preloadPlay.setOnClickListener {
            val source = currentSource ?: SourceItem.random16x9()
            Glide.with(this).load(source.img).into(mxVideoStd.getPosterImageView())

            mxVideoStd.setPlayer(playerClass)
            mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(source.url),
                    source.name, isLiveSource = source.live(),
                    isLooping = (canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            mxVideoStd.startPreload()
        }

        screenCaptureBtn.setOnClickListener {
            if (mxVideoStd.isPlaying()) {
                val bitmap: Bitmap? = mxVideoStd.getTextureView()?.bitmap
                screenCapImg.setImageBitmap(bitmap)
                screenCapImg.isVisible = true
            }
        }
        mxVideoStd.setOnPlayTicketListener { position, duration ->
            timeTxv.text = "${stringForTime(position.roundToInt())} / ${stringForTime(duration.roundToInt())}"
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
                    val source = SourceItem.random16x9()
                    mxVideoStd.setSource(
                        MXPlaySource(
                            Uri.parse(source.url),
                            source.name,
                            isLiveSource = source.live()
                        )
                    )
                }

                R.id.source4x3 -> {
                    val source = SourceItem.random4x3()
                    mxVideoStd.setSource(
                        MXPlaySource(
                            Uri.parse(source.url),
                            source.name,
                            isLiveSource = source.live()
                        )
                    )
                }

                R.id.source9x16 -> {
                    val source = SourceItem.random9x16()
                    mxVideoStd.setSource(
                        MXPlaySource(
                            Uri.parse(source.url),
                            source.name,
                            isLiveSource = source.live()
                        )
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
            } else if (checkedId == R.id.playerAli) {
                playerClass = MXAliPlayer::class.java
            } else {
                playerClass = MXSystemPlayer::class.java
            }
            mxVideoStd.setPlayer(playerClass)
        }
        playerRG.getChildAt(0)?.performClick()

        hidePlayBtnWhenNoSourceRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.hidePlayBtnWhenNoSourceTrue) {
                mxVideoStd.getConfig().hidePlayBtnWhenNoSource.set(true)
            } else {
                mxVideoStd.getConfig().hidePlayBtnWhenNoSource.set(false)
            }
        }
        hidePlayBtnWhenNoSourceRG.getChildAt(1)?.performClick()

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

    private fun stringForTime(time: Int): String {
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