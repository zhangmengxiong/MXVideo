package com.mx.video.views

import android.view.View
import android.view.WindowManager
import android.widget.*
import com.mx.video.MXVideo
import com.mx.video.R
import com.mx.video.beans.MXConfig
import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXState
import com.mx.video.utils.*
import com.mx.video.utils.touch.BrightnessTouchListener
import com.mx.video.utils.touch.SeekTouchListener
import com.mx.video.utils.touch.VolumeTouchListener

class MXViewProvider(val mxVideo: MXVideo, val config: MXConfig) {
    init {
        MXUtils.log("MXViewProvider init")
    }

    /**
     * 播放进度
     * first = 当前播放进度 秒
     * second = 视频总长度 秒
     */
    private val position = MXValueObservable(Pair(-1, -1), true)
    private val timeTicket = MXTicket()
    private val touchHelp by lazy { MXTouchHelp(mxVideo.context) }
    private val timeDelay = MXDelay()

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
        config.screen.addObserver { _, screen ->
            mxReturnBtn.visibility = if (screen == MXScreen.FULL) View.VISIBLE else View.GONE
            mxFullscreenBtn.setImageResource(if (screen == MXScreen.FULL) R.drawable.mx_icon_small_screen else R.drawable.mx_icon_full_screen)
        }
        config.showFullScreenBtn.addObserver { _, show ->
            mxFullscreenBtn.visibility = if (show) View.VISIBLE else View.GONE
        }
        config.canShowSystemTime.addObserver { _, show ->
            mxSystemTimeTxv.visibility = if (show) View.VISIBLE else View.GONE
        }
        config.canShowBatteryImg.addObserver { _, show ->
            mxBatteryImg.visibility = if (show) View.VISIBLE else View.GONE
        }
        config.videoSize.addObserver { _, value ->
            config.videoListeners.toList().forEach { listener ->
                listener.onVideoSizeChange(value.first, value.second)
            }
        }
        config.canSeekByUser.addObserver { _, show ->
            mxSeekProgress.isEnabled = config.sourceCanSeek()
        }
        config.source.addObserver { _, show ->
            mxSeekProgress.isEnabled = config.sourceCanSeek()
        }

        config.state.addObserver { _, state ->
            val isLiveSource = (config.source.get()?.isLiveSource == true)
            if (config.isPreloading.get() && state == MXState.PREPARING) {
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
                        position.set(0 to 0)
                    }
                    MXState.PREPARING -> {
                        allContentView.forEach {
                            setViewShow(it, it in arrayOf(mxPlaceImg, mxLoading))
                        }
                        timeTicket.stop()
                        timeDelay.stop()
                    }
                    MXState.PREPARED -> {
                        MXUtils.findWindows(mxVideo.context)
                            ?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        if (config.isPreloading.get()) {
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
                        config.isPreloading.set(false)
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
                        MXUtils.findWindows(mxVideo.context)
                            ?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        allContentView.forEach {
                            setViewShow(it, it in arrayOf(mxPlaceImg, mxRetryLay, mxTopLay))
                        }
                        timeTicket.stop()
                        timeDelay.stop()
                    }
                    MXState.COMPLETE -> {
                        MXUtils.findWindows(mxVideo.context)
                            ?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        allContentView.forEach {
                            setViewShow(it, it in arrayOf(mxPlaceImg, mxReplayLay, mxTopLay))
                        }
                        timeTicket.stop()
                        timeDelay.stop()
                    }
                }
            }

            config.videoListeners.toList().forEach { listener ->
                listener.onStateChange(state, this)
            }
        }

        position.addObserver { _, pair ->
            val position = pair.first
            val duration = pair.second
            mxSeekProgress.max = duration
            mxSeekProgress.progress = position
            mxBottomSeekProgress.max = duration
            mxBottomSeekProgress.progress = position
            mxCurrentTimeTxv.text = MXUtils.stringForTime(position)
            mxTotalTimeTxv.text = MXUtils.stringForTime(duration)

            config.videoListeners.toList().forEach { listener ->
                listener.onPlayTicket(position, duration)
            }
        }
        config.playerViewSize.addObserver { _, pair ->
            touchHelp.setSize(pair.first, pair.second)
        }

        config.loading.addObserver { _, loading ->
            mxLoading.visibility = if (loading) View.VISIBLE else View.GONE
            config.videoListeners.toList().forEach { listener ->
                listener.onBuffering(loading)
            }
        }

        mxPlayBtn.setOnClickListener {
            val source = config.source.get()
            val state = config.state.get()
            if (source == null) {
                config.videoListeners.toList().forEach { listener ->
                    listener.onEmptyPlay()
                }
                return@setOnClickListener
            }
            val player = mxVideo.getPlayer()
            if (state == MXState.PLAYING && config.canPauseByUser.get() && !source.isLiveSource) {
                mxVideo.pausePlay()
            } else if (state == MXState.PAUSE) {
                mxVideo.continuePlay()
            } else if (state == MXState.PREPARED) { // 预加载完成
                if (player != null) {
                    player.start()
                    mxVideo.seekToWhenPlay()
                    config.state.set(MXState.PLAYING)
                }
            } else if (config.isPreloading.get() && state == MXState.PREPARING) { // 预加载完成
                config.isPreloading.set(false)
                config.state.notifyChange()
            } else if (state == MXState.NORMAL) {
                mxVideo.startPlay()
            }
        }
        mxPlayerRootLay.setOnClickListener {
            val mState = config.state.get()
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
            val mState = config.state.get()
            if (!mxVideo.isShown || mState != MXState.PLAYING) {
                return@setDelayRun
            }
            mxPlayBtn.visibility = View.GONE
            mxBottomLay.visibility = View.GONE
            mxTopLay.visibility = View.GONE
        }
        timeTicket.setTicketRun(330) {
            if (!mxVideo.isShown) return@setTicketRun
            val source = config.source.get() ?: return@setTicketRun
            val curPosition = position.get().first

            if (mxVideo.isPlaying()) {
                val duration = mxVideo.getDuration()
                val position = mxVideo.getCurrentPosition()
                if (curPosition != position) {
                    if (duration > 0 && position > 0 && source.enableSaveProgress) {
                        MXUtils.saveProgress(source.playUri, position)
                    }
                }
                this.position.set(position to duration)
            }
        }

        mxPlayerRootLay.setOnTouchListener { _, motionEvent ->
            val mScreen = config.screen.get()
            val mState = config.state.get()

            if (mScreen == MXScreen.FULL && mState == MXState.PLAYING) {
                // 全屏且正在播放才会触发触摸滑动
                return@setOnTouchListener touchHelp.onTouch(motionEvent)
            }
            return@setOnTouchListener false
        }

        touchHelp.horizontalTouch = SeekTouchListener(this, timeDelay)
        touchHelp.verticalRightTouch = VolumeTouchListener(this)
        touchHelp.verticalLeftTouch = BrightnessTouchListener(this)

        mxRetryLay.setOnClickListener {
            // 播放错误重试，需要还原播放时间
            val source = config.source.get() ?: return@setOnClickListener
            val curPosition = position.get().first
            val curDuration = position.get().second
            if (curPosition > 0 && curDuration > 0 // 有旧的观看进度
                && config.seekWhenPlay.get() < 0  // 没有跳转值
                && !source.isLiveSource // 非直播源
            ) {
                config.seekWhenPlay.set(curPosition)
            }

            mxVideo.startPlay()
        }
        mxReplayLay.setOnClickListener {
            mxVideo.startPlay()
        }
        mxFullscreenBtn.setOnClickListener {
            if (config.screen.get() == MXScreen.NORMAL) {
                mxVideo.gotoFullScreen()
            } else {
                mxVideo.gotoNormalScreen()
            }
        }
        mxReturnBtn.setOnClickListener {
            if (config.screen.get() == MXScreen.FULL) {
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

    /**
     * 播放中状态时，控制View的显示
     */
    private fun setPlayingControl(show: Boolean) {
        playingVisible.forEach { setViewShow(it, show) }
        setViewShow(mxBottomSeekProgress, config.canShowBottomSeekBar.get() && !show)

        if (config.source.get()?.isLiveSource == true && config.state.get() == MXState.PLAYING) {
            setViewShow(mxPlayBtn, false)
        }
    }

    fun setViewShow(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
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