package com.mx.mxvideo_demo.apps

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.mx.dialog.MXDialog
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.SourceItem
import com.mx.mxvideo_demo.databinding.ActivityNormalBinding
import com.mx.mxvideo_demo.player.MXAliPlayer
import com.mx.mxvideo_demo.player.MXIJKPlayer
import com.mx.mxvideo_demo.player.exo.MXExoPlayer
import com.mx.mxvideo_demo.player.vlc.MXVLCPlayer
import com.mx.video.MXVideo
import com.mx.video.beans.MXOrientation
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScale
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import java.util.Formatter
import java.util.Locale

class NormalActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNormalBinding.inflate(layoutInflater) }
    private var playerClass: Class<out IMXPlayer>? = null
    private var currentSource: SourceItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.sourceBtn.setOnClickListener {
            val list = SourceItem.all()
            val currentIndex = list.indexOf(currentSource)
            MXDialog.select(this, list.mapIndexed { index, item ->
                "$index： ${item.type} - ${item.name} - ${item.url}"
            }, selectIndex = currentIndex) { index ->
                val item = list.getOrNull(index) ?: return@select
                currentSource = item
                binding.sourceBtn.text = "${item.type} - ${item.name} - ${item.url}"
            }
        }
        Glide.with(this).load(SourceItem.random16x9().img)
            .into(binding.mxVideoStd.getPosterImageView())


        binding.mxVideoStd.setOnEmptyPlayListener {
            val source = SourceItem.all().first()
            binding.mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(source.url),
                    source.name,
                    isLiveSource = source.live(),
                    isLooping = (binding.canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            binding.mxVideoStd.startPlay()
        }
        binding.startPlayBtn.setOnClickListener {
            val source = currentSource ?: SourceItem.random16x9()
            Glide.with(this).load(source.img).into(binding.mxVideoStd.getPosterImageView())
            binding.mxVideoStd.setPlayer(playerClass)
            binding.mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(source.url),
                    source.name, isLiveSource = source.live(),
                    isLooping = (binding.canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            binding.mxVideoStd.startPlay()
        }

        binding.preloadPlay.setOnClickListener {
            val source = currentSource ?: SourceItem.random16x9()
            Glide.with(this).load(source.img).into(binding.mxVideoStd.getPosterImageView())

            binding.mxVideoStd.setPlayer(playerClass)
            binding.mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse(source.url),
                    source.name, isLiveSource = source.live(),
                    isLooping = (binding.canLoopRG.checkedRadioButtonId == R.id.canLoopTrue)
                ), seekTo = 0
            )
            binding.mxVideoStd.startPreload()
        }

        binding.screenCaptureBtn.setOnClickListener {
            if (binding.mxVideoStd.isPlaying()) {
                val bitmap: Bitmap? = binding.mxVideoStd.getTextureView()?.bitmap
                binding.screenCapImg.setImageBitmap(bitmap)
                binding.screenCapImg.isVisible = true
            }
        }
        binding.mxVideoStd.setOnPlayTicketListener { position, duration ->
            binding.timeTxv.text = "${stringForTime(position)} / ${stringForTime(duration)}"
        }
        binding.mxVideoStd.setOnVideoSizeListener { width, height ->
            binding.sizeVideoTxv.text = "$width x $height"
        }
        binding.mxVideoStd.setOnStateListener { state ->
            binding.statusTxv.text = state.name
        }

        binding.videoSourceRG.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.source16x9 -> {
                    val source = SourceItem.random16x9()
                    binding.mxVideoStd.setSource(
                        MXPlaySource(
                            Uri.parse(source.url),
                            source.name,
                            isLiveSource = source.live()
                        )
                    )
                }

                R.id.source4x3 -> {
                    val source = SourceItem.random4x3()
                    binding.mxVideoStd.setSource(
                        MXPlaySource(
                            Uri.parse(source.url),
                            source.name,
                            isLiveSource = source.live()
                        )
                    )
                }

                R.id.source9x16 -> {
                    val source = SourceItem.random9x16()
                    binding.mxVideoStd.setSource(
                        MXPlaySource(
                            Uri.parse(source.url),
                            source.name,
                            isLiveSource = source.live()
                        )
                    )
                }
            }
            binding.mxVideoStd.startPlay()
        }

        binding.playerRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.playerIJK) {
                playerClass = MXIJKPlayer::class.java
            } else if (checkedId == R.id.playerEXO) {
                playerClass = MXExoPlayer::class.java
            } else if (checkedId == R.id.playerAli) {
                playerClass = MXAliPlayer::class.java
            } else if (checkedId == R.id.playerVLC) {
                playerClass = MXVLCPlayer::class.java
            } else {
                playerClass = MXSystemPlayer::class.java
            }
            binding.mxVideoStd.setPlayer(playerClass)
        }
        binding.playerRG.getChildAt(0)?.performClick()

        binding.hidePlayBtnWhenNoSourceRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.hidePlayBtnWhenNoSourceTrue) {
                binding.mxVideoStd.getConfig().hidePlayBtnWhenNoSource.set(true)
            } else {
                binding.mxVideoStd.getConfig().hidePlayBtnWhenNoSource.set(false)
            }
        }
        binding.hidePlayBtnWhenNoSourceRG.getChildAt(1)?.performClick()

        binding.canLoopRG.getChildAt(1)?.performClick()

        binding.fillTypeRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.fill) {
                binding.mxVideoStd.setScaleType(MXScale.FILL_PARENT)
            } else {
                binding.mxVideoStd.setScaleType(MXScale.CENTER_CROP)
            }
        }
        binding.centerCrop.performClick()

        binding.canShowBottomSeekBar.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().canShowBottomSeekBar.set(checkedId == R.id.canShowBottomSeekBarTrue)
        }
        binding.canShowBottomSeekBarTrue.performClick()

        binding.ratioRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.ratio_16_9) {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                binding.mxVideoStd.layoutParams = lp
                binding.mxVideoStd.setDimensionRatio(16.0 / 9.0)
            } else if (checkedId == R.id.ratio_4_3) {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                binding.mxVideoStd.layoutParams = lp
                binding.mxVideoStd.setDimensionRatio(4.0 / 3.0)
            } else if (checkedId == R.id.ratio200dp) {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (resources.displayMetrics.density * 200).toInt()
                )
                binding.mxVideoStd.layoutParams = lp
                binding.mxVideoStd.setDimensionRatio(0.0)
            } else {
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                binding.mxVideoStd.layoutParams = lp
                binding.mxVideoStd.setDimensionRatio(0.0)
            }
        }
        binding.ratio169.performClick()

        binding.rotationRG.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rotation0) {
                binding.mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_0)
            } else if (checkedId == R.id.rotation90) {
                binding.mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_90)
            } else if (checkedId == R.id.rotation180) {
                binding.mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_180)
            } else if (checkedId == R.id.rotation270) {
                binding.mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_270)
            }
        }
        binding.rotation0.performClick()

        binding.canSeekRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().canSeekByUser.set(checkedId == R.id.canSeekTrue)
        }
        binding.canSeekTrue.performClick()

        binding.muteRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.setAudioMute(checkedId == R.id.muteTrue)
        }
        binding.muteFalse.performClick()

        binding.canFullRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().canFullScreen.set(checkedId == R.id.canFullTrue)
        }
        binding.canFullTrue.performClick()

        binding.playSpeedRG.setOnCheckedChangeListener { group, checkedId ->
            val speed =
                group.findViewById<RadioButton>(checkedId)?.tag?.toString()?.toFloatOrNull() ?: 1f
            binding.mxVideoStd.getConfig().playSpeed.set(speed)
        }
        binding.playSpeed10.performClick()

        binding.canShowSystemTimeRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().canShowSystemTime.set(checkedId == R.id.canShowSystemTimeTrue)
        }
        binding.canShowSystemTimeTrue.performClick()

        binding.canShowBatteryImgRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().canShowBatteryImg.set(checkedId == R.id.canShowBatteryImgTrue)
        }
        binding.canShowBatteryImgTrue.performClick()

        binding.showTipIfNotWifiRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().showTipIfNotWifi.set(checkedId == R.id.showTipIfNotWifiTrue)
        }
        binding.showTipIfNotWifiFalse.performClick()

        binding.gotoNormalScreenWhenCompleteRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().gotoNormalScreenWhenComplete.set(checkedId == R.id.gotoNormalScreenWhenCompleteTrue)
        }
        binding.gotoNormalScreenWhenCompleteTrue.performClick()

        binding.mirrorRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().mirrorMode.set(checkedId == R.id.mirrorTrue)
        }
        binding.mirrorFalse.performClick()

        binding.canShowSpeedRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().canShowNetSpeed.set(checkedId == R.id.canShowSpeedTrue)
        }
        binding.canShowSpeedTrue.performClick()

        binding.gotoNormalScreenWhenErrorRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().gotoNormalScreenWhenError.set(checkedId == R.id.gotoNormalScreenWhenErrorTrue)
        }
        binding.gotoNormalScreenWhenErrorTrue.performClick()

        binding.sensorRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().autoFullScreenBySensor.set(checkedId == R.id.sensorTrue)
        }
        binding.sensorFalse.performClick()

        binding.liveRetryRG.setOnCheckedChangeListener { group, checkedId ->
            binding.mxVideoStd.getConfig().replayLiveSourceWhenError.set(checkedId == R.id.liveRetryTrue)
        }
        binding.liveRetryFalse.performClick()
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
        binding.mxVideoStd.onStart()
        super.onStart()
    }

    override fun onStop() {
        binding.mxVideoStd.onStop()
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