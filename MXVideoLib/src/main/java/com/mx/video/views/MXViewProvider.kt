package com.mx.video.views

import android.view.View
import android.widget.*
import com.mx.video.*
import com.mx.video.utils.*
import kotlin.math.min
import kotlin.math.roundToInt

class MXViewProvider(
    private val mxVideo: MXVideo,
    private val mxConfig: MXConfig
) {

    val timeTicket = MXTicket()
    val timeDelay = MXDelay()
    val touchHelp by lazy { MXTouchHelp(mxVideo.context) }
    val volumeHelp by lazy { MXVolumeHelp(mxVideo.context) }
    val brightnessHelp by lazy { MXBrightnessHelp(mxVideo.context) }
    var mState: MXState = MXState.IDLE
    var mScreen: MXScreen = MXScreen.NORMAL

    private val mxPlayerRootLay: FrameLayout by lazy {
        mxVideo.findViewById(R.id.mxPlayerRootLay) ?: FrameLayout(mxVideo.context)
    }
    val mxSurfaceContainer: FrameLayout by lazy {
        mxVideo.findViewById(R.id.mxSurfaceContainer) ?: FrameLayout(mxVideo.context)
    }

    val mxPlaceImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxPlaceImg) ?: ImageView(mxVideo.context)
    }
    val mxLoading: ProgressBar by lazy {
        mxVideo.findViewById(R.id.mxLoading) ?: ProgressBar(mxVideo.context)
    }
    val mxBottomSeekProgress: ProgressBar by lazy {
        mxVideo.findViewById(R.id.mxBottomSeekProgress) ?: ProgressBar(mxVideo.context)
    }

    private val mxPlayBtn: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxPlayBtn) ?: LinearLayout(mxVideo.context)
    }
    private val mxRetryLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxRetryLay) ?: LinearLayout(mxVideo.context)
    }

    private val mxPlayPauseImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxPlayPauseImg) ?: ImageView(mxVideo.context)
    }
    val mxReturnBtn: ImageView by lazy {
        mxVideo.findViewById(R.id.mxReturnBtn) ?: ImageView(mxVideo.context)
    }
    private val mxBatteryImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxBatteryImg) ?: ImageView(mxVideo.context)
    }
    val mxCurrentTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxCurrentTimeTxv) ?: TextView(mxVideo.context)
    }
    private val mxSystemTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxSystemTimeTxv) ?: TextView(mxVideo.context)
    }
    private val mxTotalTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxTotalTimeTxv) ?: TextView(mxVideo.context)
    }
    val mxTitleTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxTitleTxv) ?: TextView(mxVideo.context)
    }
    private val mxSeekProgress: SeekBar by lazy {
        mxVideo.findViewById(R.id.mxSeekProgress) ?: SeekBar(mxVideo.context)
    }
    private val mxBottomLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxBottomLay) ?: LinearLayout(mxVideo.context)
    }
    private val mxTopLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxTopLay) ?: LinearLayout(mxVideo.context)
    }
    private val mxReplayLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxReplayLay) ?: LinearLayout(mxVideo.context)
    }
    private val mxQuickSeekLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxQuickSeekLay) ?: LinearLayout(mxVideo.context)
    }
    private val mxVolumeLightLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxVolumeLightLay) ?: LinearLayout(mxVideo.context)
    }
    val mxVolumeLightTypeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxVolumeLightTypeTxv) ?: TextView(mxVideo.context)
    }
    val mxVolumeLightTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxVolumeLightTxv) ?: TextView(mxVideo.context)
    }
    val mxQuickSeekCurrentTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxQuickSeekCurrentTxv) ?: TextView(mxVideo.context)
    }
    val mxQuickSeekMaxTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxQuickSeekMaxTxv) ?: TextView(mxVideo.context)
    }
    val mxFullscreenBtn: ImageView by lazy {
        mxVideo.findViewById(R.id.mxFullscreenBtn) ?: ImageView(mxVideo.context)
    }

    /**
     * 根节点View列表
     */
    private val allContentView = arrayOf(
        mxPlaceImg,
        mxLoading,
        mxPlayBtn,
        mxTopLay,
        mxBottomLay,
        mxRetryLay,
        mxReplayLay,
        mxQuickSeekLay,
        mxVolumeLightLay
    )

    /**
     * 播放时点击需要显示的状态
     */
    private val playingVisible = arrayOf(mxPlayBtn, mxTopLay, mxBottomLay)

    fun initView() {
        mxPlayBtn.setOnClickListener {
            if (mxVideo.getSource() == null) {
                Toast.makeText(mxVideo.context, R.string.mx_play_source_not_set, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val player = mxVideo.getPlayer()
            if (mState == MXState.PLAYING) {
                if (player != null) {
                    player.pause()
                    setState(MXState.PAUSE)
                }
            } else if (mState == MXState.PAUSE) {
                if (player != null) {
                    player.start()
                    setState(MXState.PLAYING)
                }
            } else if (mState == MXState.IDLE) {
                mxVideo.startPlay()
            }
        }
        mxPlayerRootLay.setOnClickListener {
            if (mState == MXState.PAUSE) {
                setPlayingControl(true)
                timeDelay.stop()
                return@setOnClickListener
            }
            if (mState == MXState.PLAYING) {
                setPlayingControl(!mxPlayBtn.isShown)
                timeDelay.start()
                timeTicket.start()
                return@setOnClickListener
            }
        }

        timeDelay.setDelayRun(5000) {
            if (!mxVideo.isShown || mState != MXState.PLAYING) {
                return@setDelayRun
            }
            mxPlayBtn.visibility = View.GONE
            mxBottomLay.visibility = View.GONE
            mxTopLay.visibility = View.GONE
        }
        timeTicket.setTicketRun(300) {
            if (!mxVideo.isShown) return@setTicketRun
            if (mState in arrayOf(
                    MXState.PREPARED,
                    MXState.PREPARING,
                    MXState.PLAYING,
                    MXState.PAUSE
                ) && mxVideo.isPlaying()
            ) {
                val duration = mxVideo.getDuration()
                val position = mxVideo.getCurrentPosition()
                if (mxSeekProgress.isShown) {
                    mxSeekProgress.max = duration
                }
                if (mxSeekProgress.isShown) {
                    mxSeekProgress.progress = position
                }
                if (mxBottomSeekProgress.isShown) {
                    mxBottomSeekProgress.max = duration
                }
                if (mxBottomSeekProgress.isShown) {
                    mxBottomSeekProgress.progress = position
                }
                if (mxCurrentTimeTxv.isShown) {
                    mxCurrentTimeTxv.text = MXUtils.stringForTime(position)
                }
                if (mxTotalTimeTxv.isShown) {
                    mxTotalTimeTxv.text = MXUtils.stringForTime(duration)
                }
            }
        }

        mxPlayerRootLay.setOnTouchListener { _, motionEvent ->
            if (mScreen == MXScreen.FULL && mState == MXState.PLAYING) {
                // 全屏且正在播放才会触发触摸滑动
                return@setOnTouchListener touchHelp.onTouch(motionEvent)
            }
            return@setOnTouchListener false
        }
        touchHelp.setHorizontalTouchCall(object : MXTouchHelp.OnMXTouchListener() {
            override fun onStart() {
                if (!mxConfig.canSeekByUser || mState != MXState.PLAYING) return
                allContentView.forEach {
                    if (it == mxQuickSeekLay) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
            }

            override fun onTouchMove(percent: Float) {
                if (!mxConfig.canSeekByUser || mState != MXState.PLAYING) return

                MXUtils.log("percent = $percent")
                val duration = mxVideo.getDuration()
                var position = mxVideo.getCurrentPosition() + (min(120, duration) * percent).toInt()
                if (position < 0) position = 0
                if (position > duration) position = duration

                mxQuickSeekCurrentTxv.text = MXUtils.stringForTime(position)
                mxQuickSeekMaxTxv.text = MXUtils.stringForTime(duration)
                mxBottomSeekProgress.progress = position
            }

            override fun onEnd(percent: Float) {
                mxQuickSeekLay.visibility = View.GONE
                if (!mxConfig.canSeekByUser || mState != MXState.PLAYING) return

                val duration = mxVideo.getDuration()
                var position = mxVideo.getCurrentPosition() + (min(120, duration) * percent).toInt()
                if (position < 0) position = 0
                if (position > duration) position = duration

                mxVideo.seekTo(position)
                timeDelay.start()
            }
        })

        touchHelp.setVerticalRightTouchCall(object : MXTouchHelp.OnMXTouchListener() {
            private var maxVolume = 0
            private var startVolume = 0
            override fun onStart() {
                mxVolumeLightLay.visibility = View.VISIBLE
                maxVolume = volumeHelp.getMaxVolume()
                startVolume = volumeHelp.getVolume()

                mxVolumeLightTypeTxv.setText(R.string.mx_play_volume)
                val percent = (startVolume * 100.0 / maxVolume.toDouble()).roundToInt()
                mxVolumeLightTxv.text = "${percent}%"
            }

            override fun onTouchMove(percent: Float) {
                MXUtils.log("percent = $percent")
                var targetVolume = startVolume + (maxVolume * percent).toInt()
                if (targetVolume < 0) targetVolume = 0
                if (targetVolume > maxVolume) targetVolume = maxVolume

                volumeHelp.setVolume(targetVolume)
                val percent = (targetVolume * 100.0 / maxVolume.toDouble()).roundToInt()
                mxVolumeLightTxv.text = "${percent}%"
            }

            override fun onEnd(percent: Float) {
                mxVolumeLightLay.visibility = View.GONE
                var targetVolume = startVolume + (maxVolume * percent).toInt()
                if (targetVolume < 0) targetVolume = 0
                if (targetVolume > maxVolume) targetVolume = maxVolume

                volumeHelp.setVolume(targetVolume)
            }
        })
        touchHelp.setVerticalLeftTouchCall(object : MXTouchHelp.OnMXTouchListener() {
            private var maxBrightness = 0
            private var startBrightness = 0
            override fun onStart() {
                mxVolumeLightLay.visibility = View.VISIBLE
                maxBrightness = brightnessHelp.getMaxBrightness()
                startBrightness = brightnessHelp.getBrightness()

                mxVolumeLightTypeTxv.setText(R.string.mx_play_brightness)
                val percent = (startBrightness * 100.0 / maxBrightness.toDouble()).roundToInt()
                mxVolumeLightTxv.text = "${percent}%"
            }

            override fun onTouchMove(percent: Float) {
                MXUtils.log("percent = $percent")
                val maxBrightness = brightnessHelp.getMaxBrightness()
                var targetBrightness = startBrightness + (maxBrightness * percent * 0.7).toInt()
                if (targetBrightness < 0) targetBrightness = 0
                if (targetBrightness > maxBrightness) targetBrightness = maxBrightness

                brightnessHelp.setBrightness(targetBrightness)
                val percent = (targetBrightness * 100.0 / maxBrightness.toDouble()).roundToInt()
                mxVolumeLightTxv.text = "${percent}%"
            }

            override fun onEnd(percent: Float) {
                mxVolumeLightLay.visibility = View.GONE
                var targetBrightness = startBrightness + (maxBrightness * percent * 0.7).toInt()
                if (targetBrightness < 0) targetBrightness = 0
                if (targetBrightness > maxBrightness) targetBrightness = maxBrightness

                brightnessHelp.setBrightness(targetBrightness)
            }
        })

        mxRetryLay.setOnClickListener {
            mxVideo.startPlay()
        }
        mxReplayLay.setOnClickListener {
            mxVideo.startPlay()
        }
        mxFullscreenBtn.setOnClickListener {
            if (mScreen == MXScreen.NORMAL) {
                mxVideo.gotoFullScreen()
            } else {
                mxVideo.gotoNormalScreen()
            }
        }
        mxReturnBtn.setOnClickListener {
            if (mScreen == MXScreen.FULL) {
                mxVideo.gotoNormalScreen()
            }
        }
    }

    private val onSeekBarListener = object : SeekBar.OnSeekBarChangeListener {
        var progress = 0
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser && mxConfig.canSeekByUser) {
                this.progress = progress
                mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            if (!mxConfig.canSeekByUser) return
            MXUtils.log("onStartTrackingTouch")
            this.progress = seekBar?.progress ?: return
            timeDelay.stop()
            timeTicket.stop()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if (!mxConfig.canSeekByUser) return
            MXUtils.log("onStopTrackingTouch")
            mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            mxVideo.seekTo(progress)
            timeDelay.start()
            timeTicket.start()
        }
    }

    fun setState(state: MXState) {
        mState = state
        if (!mxConfig.canFullScreen) {
            mxFullscreenBtn.visibility = View.GONE
        } else {
            mxFullscreenBtn.visibility = View.VISIBLE
        }
        mxSeekProgress.isEnabled = mxConfig.canSeekByUser
        if (!mxConfig.canShowSystemTime) {
            mxSystemTimeTxv.visibility = View.GONE
        } else {
            mxSystemTimeTxv.visibility = View.VISIBLE
        }
        if (!mxConfig.canShowBatteryImg) {
            mxBatteryImg.visibility = View.GONE
        } else {
            mxBatteryImg.visibility = View.VISIBLE
        }
        when (state) {
            MXState.IDLE, MXState.NORMAL -> {
                allContentView.forEach {
                    if (it in arrayOf(mxPlaceImg, mxPlayBtn)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            }
            MXState.PREPARING -> {
                allContentView.forEach {
                    if (it in arrayOf(mxPlaceImg, mxLoading)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
            }
            MXState.PREPARED -> {
                allContentView.forEach {
                    if (it in arrayOf(mxLoading, mxPlaceImg)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
                mxSeekProgress.setOnSeekBarChangeListener(onSeekBarListener)
                timeTicket.start()
            }
            MXState.PLAYING -> {
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_pause)
                allContentView.forEach {
                    if (it !in playingVisible) {
                        it.visibility = View.GONE
                    }
                }
                if (mxConfig.canSeekByUser) {
                    mxSeekProgress.isEnabled = true
                }
                setPlayingControl(playingVisible.any { it.isShown })

                timeTicket.start()
                timeDelay.start()
            }
            MXState.PAUSE -> {
                allContentView.forEach {
                    if (it in playingVisible) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                mxSeekProgress.isEnabled = false
                timeTicket.start()
                timeDelay.stop()
            }
            MXState.ERROR -> {
                allContentView.forEach {
                    if (it in arrayOf(mxPlaceImg, mxRetryLay, mxTopLay)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
            }
            MXState.COMPLETE -> {
                allContentView.forEach {
                    if (it in arrayOf(mxPlaceImg, mxReplayLay, mxTopLay)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
            }
        }
        mxReturnBtn.visibility = if (mScreen == MXScreen.FULL) View.VISIBLE else View.GONE
    }

    private fun setPlayingControl(show: Boolean) {
        playingVisible.forEach { it.visibility = if (show) View.VISIBLE else View.GONE }
        mxBottomSeekProgress.visibility = if (show) View.GONE else View.VISIBLE
    }

    fun refreshStatus() {
        setState(mState)
    }
}