package com.mx.video.views

import android.view.View
import android.widget.*
import com.mx.video.*
import com.mx.video.beans.MXConfig
import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXState
import com.mx.video.utils.*
import com.mx.video.utils.touch.BrightnessTouchListener
import com.mx.video.utils.touch.SeekTouchListener
import com.mx.video.utils.touch.VolumeTouchListener

class MXViewProvider(val mxVideo: MXVideo, val config: MXConfig) {
    val timeTicket = MXTicket()
    val touchHelp by lazy { MXTouchHelp(mxVideo.context) }
    val timeDelay = MXDelay()

    var curDuration: Int = -1
    var curPosition: Int = -1

    var mState: MXState = MXState.IDLE
        private set
    var mScreen: MXScreen = MXScreen.NORMAL
        private set

    val mxPlayerRootLay: FrameLayout by lazy {
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
    val mxBatteryImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxBatteryImg) ?: ImageView(mxVideo.context)
    }
    val mxCurrentTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxCurrentTimeTxv) ?: TextView(mxVideo.context)
    }
    val mxSystemTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxSystemTimeTxv) ?: TextView(mxVideo.context)
    }
    val mxTotalTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxTotalTimeTxv) ?: TextView(mxVideo.context)
    }
    val mxTitleTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxTitleTxv) ?: TextView(mxVideo.context)
    }
    val mxSeekProgress: SeekBar by lazy {
        mxVideo.findViewById(R.id.mxSeekProgress) ?: SeekBar(mxVideo.context)
    }
    val mxBottomLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxBottomLay) ?: LinearLayout(mxVideo.context)
    }
    val mxTopLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxTopLay) ?: LinearLayout(mxVideo.context)
    }
    val mxReplayLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxReplayLay) ?: LinearLayout(mxVideo.context)
    }
    val mxQuickSeekLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxQuickSeekLay) ?: LinearLayout(mxVideo.context)
    }
    val mxVolumeLightLay: LinearLayout by lazy {
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
    val allContentView = arrayOf(
        mxPlaceImg, mxLoading, mxPlayBtn, mxTopLay, mxBottomLay,
        mxRetryLay, mxReplayLay, mxQuickSeekLay, mxVolumeLightLay
    )

    /**
     * 播放时点击需要显示的状态
     */
    private val playingVisible = arrayOf(mxPlayBtn, mxTopLay, mxBottomLay)

    fun initView() {
        mxPlayBtn.setOnClickListener {
            val source = config.source
            if (source == null) {
                mxVideo.post {
                    config.videoListeners.toList().forEach { listener ->
                        listener.onEmptyPlay()
                    }
                }
                return@setOnClickListener
            }
            val player = mxVideo.getPlayer()
            if (mState == MXState.PLAYING && config.canPauseByUser && !source.isLiveSource) {
                mxVideo.pausePlay()
            } else if (mState == MXState.PAUSE) {
                mxVideo.continuePlay()
            } else if (mState == MXState.PREPARED) { // 预加载完成
                if (player != null) {
                    player.start()
                    mxVideo.seekToWhenPlay()
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

        timeDelay.setDelayRun(3000) {
            if (!mxVideo.isShown || mState != MXState.PLAYING) {
                return@setDelayRun
            }
            mxPlayBtn.visibility = View.GONE
            mxBottomLay.visibility = View.GONE
            mxTopLay.visibility = View.GONE
        }
        timeTicket.setTicketRun(330) {
            if (!mxVideo.isShown) return@setTicketRun
            val source = config.source ?: return@setTicketRun
            if (mxVideo.isPlaying()) {
                val duration = mxVideo.getDuration()
                val position = mxVideo.getCurrentPosition()
                if (curPosition != position) {
                    if (duration > 0 && position > 0 && source.enableSaveProgress) {
                        MXUtils.saveProgress(source.playUri, position)
                    }

                    mxVideo.post {
                        config.videoListeners.toList().forEach { listener ->
                            listener.onPlayTicket(position, duration)
                        }
                    }
                }
                curPosition = position
                curDuration = duration

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

        touchHelp.horizontalTouch = SeekTouchListener(this)
        touchHelp.verticalRightTouch = VolumeTouchListener(this)
        touchHelp.verticalLeftTouch = BrightnessTouchListener(this)

        mxRetryLay.setOnClickListener {
            // 播放错误重试，需要还原播放时间
            val source = config.source ?: return@setOnClickListener
            if (curPosition > 0 && curDuration > 0 // 有旧的观看进度
                && config.seekWhenPlay < 0  // 没有跳转值
                && !source.isLiveSource // 非直播源
            ) {
                config.seekWhenPlay = curPosition
            }

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
        var touchProgress = 0
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser && config.sourceCanSeek()) {
                this.touchProgress = progress
                mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            if (!config.sourceCanSeek()) return
            MXUtils.log("onStartTrackingTouch")
            this.touchProgress = seekBar?.progress ?: return
            timeDelay.stop()
            timeTicket.stop()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if (!config.sourceCanSeek()) return
            MXUtils.log("onStopTrackingTouch")
            mxCurrentTimeTxv.text = MXUtils.stringForTime(touchProgress)
            mxVideo.seekTo(touchProgress)
            timeDelay.start()
            timeTicket.start()
        }
    }

    fun setPlayState(state: MXState) {
        val isLiveSource = (config.source?.isLiveSource == true)
        mxSeekProgress.isEnabled = config.sourceCanSeek()
        setViewShow(mxFullscreenBtn, config.showFullScreenBtn)
        setViewShow(mxSystemTimeTxv, config.canShowSystemTime)
        setViewShow(mxBatteryImg, config.canShowBatteryImg)

        if (config.isPreloading && state == MXState.PREPARING) {
            // 正在预加载中
            allContentView.forEach {
                setViewShow(it, it in arrayOf(mxPlaceImg, mxPlayBtn))
            }
            mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            timeTicket.stop()
            timeDelay.stop()
        } else {
            when (state) {
                MXState.IDLE, MXState.NORMAL -> {
                    allContentView.forEach {
                        setViewShow(it, it in arrayOf(mxPlaceImg, mxPlayBtn))
                    }
                    mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                    timeTicket.stop()
                    timeDelay.stop()
                    setViewProgress(0, 0)
                }
                MXState.PREPARING -> {
                    allContentView.forEach {
                        setViewShow(it, it in arrayOf(mxPlaceImg, mxLoading))
                    }
                    timeTicket.stop()
                    timeDelay.stop()
                }
                MXState.PREPARED -> {
                    if (config.isPreloading) {
                        allContentView.forEach {
                            setViewShow(it, it in arrayOf(mxPlaceImg, mxPlayBtn))
                        }
                    } else {
                        allContentView.forEach {
                            setViewShow(it, it in arrayOf(mxPlaceImg, mxLoading))
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
                        setViewShow(it, it in playingVisible)
                    }
                    mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                    timeTicket.start()
                    timeDelay.stop()
                }
                MXState.ERROR -> {
                    allContentView.forEach {
                        setViewShow(it, it in arrayOf(mxPlaceImg, mxRetryLay, mxTopLay))
                    }
                    timeTicket.stop()
                    timeDelay.stop()
                }
                MXState.COMPLETE -> {
                    allContentView.forEach {
                        setViewShow(it, it in arrayOf(mxPlaceImg, mxReplayLay, mxTopLay))
                    }
                    timeTicket.stop()
                    timeDelay.stop()
                }
            }
        }
        setViewShow(mxReturnBtn, mScreen == MXScreen.FULL)

        val oldState = mState
        mState = state
        if (oldState != state) {
            mxVideo.post {
                config.videoListeners.toList().forEach { listener ->
                    listener.onStateChange(state, this)
                }
            }
        }
    }

    /**
     * 播放中状态时，控制View的显示
     */
    private fun setPlayingControl(show: Boolean) {
        playingVisible.forEach { setViewShow(it, show) }
        setViewShow(mxBottomSeekProgress, !show)

        if (config.source?.isLiveSource == true && mState == MXState.PLAYING) {
            setViewShow(mxPlayBtn, false)
        }
    }

    fun setViewShow(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
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
            mxVideo.post {
                config.videoListeners.toList().forEach { listener ->
                    listener.onScreenChange(screen, this)
                }
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
        timeDelay.release()
        timeTicket.release()
        touchHelp.release()
    }
}