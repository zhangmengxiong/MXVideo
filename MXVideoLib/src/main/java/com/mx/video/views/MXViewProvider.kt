package com.mx.video.views

import android.view.View
import android.widget.*
import com.mx.video.*
import com.mx.video.beans.MXConfig
import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXState
import com.mx.video.utils.*
import kotlin.math.min
import kotlin.math.roundToInt

class MXViewProvider(private val mxVideo: MXVideo, private val config: MXConfig) {
    private val timeTicket = MXTicket()
    private val timeDelay = MXDelay()
    private val touchHelp by lazy { MXTouchHelp(mxVideo.context) }
    private val volumeHelp by lazy { MXVolumeHelp(mxVideo.context) }
    private val brightnessHelp by lazy { MXBrightnessHelp(mxVideo.context) }

    var mState: MXState = MXState.IDLE
        private set
    var mScreen: MXScreen = MXScreen.NORMAL
        private set
    var preTicketTime: Int = -1

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

    val mxPlayBtn: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxPlayBtn) ?: LinearLayout(mxVideo.context)
    }
    val mxRetryLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxRetryLay) ?: LinearLayout(mxVideo.context)
    }

    val mxPlayPauseImg: ImageView by lazy {
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
            val source = config.source
            if (source == null) {
                Toast.makeText(mxVideo.context, R.string.mx_play_source_not_set, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val player = mxVideo.getPlayer()
            if (mState == MXState.PLAYING && config.canPauseByUser && !source.isLiveSource) {
                if (player != null) {
                    player.pause()
                    setPlayState(MXState.PAUSE)
                }
            } else if (mState == MXState.PAUSE) {
                if (player != null) {
                    player.start()
                    setPlayState(MXState.PLAYING)
                }
            } else if (mState == MXState.PREPARED) { // 预加载完成
                if (player != null) {
                    player.start()
                    mxVideo.seekBeforePlay()
                    setPlayState(MXState.PLAYING)
                }
            } else if (config.isPreloading && mState == MXState.PREPARING) { // 预加载完成
                config.isPreloading = false
                setPlayState(mState)
            } else if (mState == MXState.NORMAL) {
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
            val source = config.source ?: return@setTicketRun
            if (mxVideo.isPlaying()) {
                val duration = mxVideo.getDuration()
                val position = mxVideo.getCurrentPosition()
                if (preTicketTime != position) {
                    if (duration > 0 && position > 0 && source.enableSaveProgress) {
                        MXUtils.saveProgress(mxVideo.context, source.playUri, position)
                    }

                    config.videoListeners.toList().forEach { listener ->
                        listener.onPlayTicket(position, duration)
                    }
                }
                preTicketTime = position

                setViewProgress(position, duration)
            }
        }

        mxPlayerRootLay.setOnTouchListener { _, motionEvent ->
            if (mScreen == MXScreen.FULL && mState == MXState.PLAYING) {
                // 全屏且正在播放才会触发触摸滑动
                return@setOnTouchListener touchHelp.onTouch(motionEvent)
            }
            return@setOnTouchListener false
        }
        touchHelp.setHorizontalTouchCall(seekTouchCall)

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
            if (fromUser && config.sourceCanSeek()) {
                this.progress = progress
                mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            if (!config.sourceCanSeek()) return
            MXUtils.log("onStartTrackingTouch")
            this.progress = seekBar?.progress ?: return
            timeDelay.stop()
            timeTicket.stop()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if (!config.sourceCanSeek()) return
            MXUtils.log("onStopTrackingTouch")
            mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            mxVideo.seekTo(progress)
            timeDelay.start()
            timeTicket.start()
        }
    }

    fun setPlayState(state: MXState) {
        val isLiveSource = (config.source?.isLiveSource == true)
        if (!config.canFullScreen) {
            mxFullscreenBtn.visibility = View.GONE
        } else {
            mxFullscreenBtn.visibility = View.VISIBLE
        }
        mxSeekProgress.isEnabled = config.sourceCanSeek()

        if (!config.canShowSystemTime) {
            mxSystemTimeTxv.visibility = View.GONE
        } else {
            mxSystemTimeTxv.visibility = View.VISIBLE
        }
        if (!config.canShowBatteryImg) {
            mxBatteryImg.visibility = View.GONE
        } else {
            mxBatteryImg.visibility = View.VISIBLE
        }
        if (config.isPreloading && state == MXState.PREPARING) {
            // 正在预加载中
            allContentView.forEach {
                if (it in arrayOf(mxPlaceImg, mxPlayBtn)) {
                    it.visibility = View.VISIBLE
                } else {
                    it.visibility = View.GONE
                }
            }
            mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            timeTicket.stop()
            timeDelay.stop()
        } else {
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
                    timeTicket.stop()
                    timeDelay.stop()
                    setViewProgress(0, 0)
                }
                MXState.PREPARING -> {
                    allContentView.forEach {
                        if (it in arrayOf(mxPlaceImg, mxLoading)) {
                            it.visibility = View.VISIBLE
                        } else {
                            it.visibility = View.GONE
                        }
                    }
                    timeTicket.stop()
                    timeDelay.stop()
                }
                MXState.PREPARED -> {
                    allContentView.forEach {
                        if (it in arrayOf(mxPlaceImg, mxPlayBtn)) {
                            it.visibility = View.VISIBLE
                        } else {
                            it.visibility = View.GONE
                        }
                    }
                    timeTicket.stop()
                    timeDelay.stop()
                }
                MXState.PLAYING -> {
                    mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_pause)
                    allContentView.forEach {
                        if (it !in playingVisible) {
                            it.visibility = View.GONE
                        }
                    }
                    mxSeekProgress.setOnSeekBarChangeListener(onSeekBarListener)
                    if (isLiveSource) {
                        // 直播流，需要隐藏一些按钮
                        mxBottomSeekProgress.visibility = View.GONE
                        mxSeekProgress.visibility = View.INVISIBLE
                        mxCurrentTimeTxv.visibility = View.GONE
                        mxTotalTimeTxv.visibility = View.GONE
                        mxPlayBtn.visibility = View.GONE
                    } else {
                        mxSeekProgress.visibility = View.VISIBLE
                        mxCurrentTimeTxv.visibility = View.VISIBLE
                        mxTotalTimeTxv.visibility = View.VISIBLE
                        setPlayingControl(playingVisible.any { it.isShown })
                    }

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
                    timeTicket.stop()
                    timeDelay.stop()
                }
                MXState.COMPLETE -> {
                    allContentView.forEach {
                        if (it in arrayOf(mxPlaceImg, mxReplayLay, mxTopLay)) {
                            it.visibility = View.VISIBLE
                        } else {
                            it.visibility = View.GONE
                        }
                    }
                    timeTicket.stop()
                    timeDelay.stop()
                }
            }
        }
        mxReturnBtn.visibility = if (mScreen == MXScreen.FULL) View.VISIBLE else View.GONE

        val oldState = mState
        mState = state
        if (oldState != state) {
            config.videoListeners.toList().forEach { listener ->
                listener.onStateChange(state, this)
            }
        }
    }

    /**
     * 快进快退滑动处理
     */
    private val seekTouchCall = object : MXTouchHelp.OnMXTouchListener() {
        override fun onStart() {
            if (!config.sourceCanSeek() || mState != MXState.PLAYING) return
            allContentView.forEach {
                if (it == mxQuickSeekLay) {
                    it.visibility = View.VISIBLE
                } else {
                    it.visibility = View.GONE
                }
            }
        }

        override fun onTouchMove(percent: Float) {
            if (!config.sourceCanSeek() || mState != MXState.PLAYING) return

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
            if (!config.sourceCanSeek() || mState != MXState.PLAYING) return

            val duration = mxVideo.getDuration()
            var position = mxVideo.getCurrentPosition() + (min(120, duration) * percent).toInt()
            if (position < 0) position = 0
            if (position > duration) position = duration

            mxVideo.seekTo(position)
            timeDelay.start()
        }
    }

    /**
     * 播放中状态时，控制View的显示
     */
    private fun setPlayingControl(show: Boolean) {
        playingVisible.forEach { it.visibility = if (show) View.VISIBLE else View.GONE }
        mxBottomSeekProgress.visibility = if (show) View.GONE else View.VISIBLE
        if (config.source?.isLiveSource == true && mState == MXState.PLAYING) {
            mxPlayBtn.visibility = View.GONE
        }
    }

    /**
     * 设置进度
     */
    private fun setViewProgress(position: Int, duration: Int) {
        mxSeekProgress.max = duration
        mxSeekProgress.progress = position
        mxBottomSeekProgress.max = duration
        mxBottomSeekProgress.progress = position
        mxCurrentTimeTxv.text = MXUtils.stringForTime(position)
        mxTotalTimeTxv.text = MXUtils.stringForTime(duration)
    }

    fun refreshStatus() {
        setPlayState(mState)
    }

    /**
     * 设置播放器宽高
     */
    fun setViewSize(width: Int, height: Int) {
        touchHelp.setSize(width, height)
    }

    /**
     * 设置全屏状态
     */
    fun setScreenState(screen: MXScreen) {
        mxReturnBtn.visibility = if (screen == MXScreen.FULL) View.VISIBLE else View.GONE
        mxFullscreenBtn.setImageResource(if (screen == MXScreen.FULL) R.drawable.mx_icon_small_screen else R.drawable.mx_icon_full_screen)
        val oldScreen = mScreen
        mScreen = screen
        if (oldScreen != screen) {
            config.videoListeners.toList().forEach { listener ->
                listener.onScreenChange(screen, this)
            }
        }
    }

    /**
     * 设置缓冲状态
     */
    fun setOnBuffering(start: Boolean) {
        mxLoading.visibility = if (start) View.VISIBLE else View.GONE
    }

    /**
     * 释放资源，释放后无法再次播放
     */
    fun release() {
        val batteryImg = mxBatteryImg
        if (batteryImg is MXBatteryImageView) {
            batteryImg.release()
        }

        val timeTxv = mxSystemTimeTxv
        if (timeTxv is MXTimeTextView) {
            timeTxv.release()
        }

        mxSeekProgress.setOnSeekBarChangeListener(null)

        brightnessHelp.release()
        timeDelay.release()
        timeTicket.release()
        touchHelp.release()
        volumeHelp.release()
    }
}