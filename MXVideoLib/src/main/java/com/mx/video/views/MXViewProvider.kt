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
import com.mx.video.utils.touch.MXBaseTouchListener
import com.mx.video.utils.touch.MXBrightnessTouchListener
import com.mx.video.utils.touch.MXVolumeTouchListener
import kotlin.math.min

class MXViewProvider(val mxVideo: MXVideo, val config: MXConfig) {
    /**
     * 播放进度
     * first = 当前播放进度 秒
     * second = 视频总长度 秒
     */
    private val position = MXValueObservable(Pair(-1, -1))

    // 播放状态，相关View是否显示
    private val playingControlShow = MXValueObservable(false)

    private val timeTicket = MXTicket()
    private val touchHelp by lazy { MXTouchHelp(mxVideo.context) }
    private val timeDelay = MXDelay()
    private val speedHelp = MXSpeedHelp()

    val mxPlayerRootLay: View by lazy {
        mxVideo.findViewById(R.id.mxPlayerRootLay) ?: View(mxVideo.context)
    }
    val mxSurfaceContainer: FrameLayout by lazy {
        mxVideo.findViewById(R.id.mxSurfaceContainer) ?: FrameLayout(mxVideo.context)
    }

    val mxPlaceImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxPlaceImg) ?: ImageView(mxVideo.context)
    }
    val mxLoading: View by lazy {
        mxVideo.findViewById(R.id.mxLoading) ?: View(mxVideo.context)
    }
    val mxLoadingTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxLoadingTxv) ?: TextView(mxVideo.context)
    }
    val mxBottomSeekProgress: ProgressBar by lazy {
        mxVideo.findViewById(R.id.mxBottomSeekProgress) ?: ProgressBar(mxVideo.context)
    }

    val mxPlayBtn: View by lazy {
        mxVideo.findViewById(R.id.mxPlayBtn) ?: View(mxVideo.context)
    }
    val mxRetryLay: View by lazy {
        mxVideo.findViewById(R.id.mxRetryLay) ?: View(mxVideo.context)
    }

    val mxPlayPauseImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxPlayPauseImg) ?: ImageView(mxVideo.context)
    }
    val mxReturnBtn: View by lazy {
        mxVideo.findViewById(R.id.mxReturnBtn) ?: View(mxVideo.context)
    }
    val mxBatteryImg: View by lazy {
        mxVideo.findViewById(R.id.mxBatteryImg) ?: View(mxVideo.context)
    }
    val mxCurrentTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxCurrentTimeTxv) ?: TextView(mxVideo.context)
    }
    val mxSystemTimeTxv: View by lazy {
        mxVideo.findViewById(R.id.mxSystemTimeTxv) ?: View(mxVideo.context)
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
    val mxBottomLay: View by lazy {
        mxVideo.findViewById(R.id.mxBottomLay) ?: View(mxVideo.context)
    }
    val mxTopLay: View by lazy {
        mxVideo.findViewById(R.id.mxTopLay) ?: View(mxVideo.context)
    }
    val mxReplayLay: View by lazy {
        mxVideo.findViewById(R.id.mxReplayLay) ?: View(mxVideo.context)
    }
    val mxReplayImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxReplayImg) ?: ImageView(mxVideo.context)
    }
    val mxQuickSeekLay: View by lazy {
        mxVideo.findViewById(R.id.mxQuickSeekLay) ?: View(mxVideo.context)
    }
    val mxVolumeLightLay: View by lazy {
        mxVideo.findViewById(R.id.mxVolumeLightLay) ?: View(mxVideo.context)
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
    internal val allContentView = arrayOf(
        mxPlaceImg, mxLoading, mxLoadingTxv, mxPlayBtn, mxTopLay, mxBottomLay,
        mxRetryLay, mxReplayLay, mxQuickSeekLay, mxVolumeLightLay
    )

    fun initView() {
        // 全屏切换时，显示设置
        config.screen.addObserver { screen ->
            mxReturnBtn.visibility = if (screen == MXScreen.FULL) View.VISIBLE else View.GONE
            mxFullscreenBtn.setImageResource(if (screen == MXScreen.FULL) R.drawable.mx_icon_small_screen else R.drawable.mx_icon_full_screen)

            config.playerViewSize.notifyChange()
            config.videoListeners.toList().forEach { listener ->
                listener.onScreenChange(screen, this)
            }
        }

        playingControlShow.addObserver { show ->
            timeDelay.stop()
            if (config.state.get() != MXState.PLAYING) {
                return@addObserver
            }

            val playBtnShow = if (config.source.get()?.isLiveSource == true) {
                false
            } else show
            setViewShow(mxPlayBtn, playBtnShow)
            setViewShow(mxTopLay, show)
            setViewShow(mxBottomLay, show)
            setViewShow(mxBottomSeekProgress, config.canShowBottomSeekBar.get() && !show)
            if (show) {
                timeDelay.start()
            }
        }

        // 全屏按钮显示控制
        val fullScreenObserver = { _: Boolean ->
            mxFullscreenBtn.visibility = if (config.canFullScreen.get()
                && config.showFullScreenButton.get()
            ) View.VISIBLE else View.GONE
        }
        config.canFullScreen.addObserver(fullScreenObserver)
        config.showFullScreenButton.addObserver(fullScreenObserver)

        // 时间控件显示设置
        config.canShowSystemTime.addObserver { show ->
            mxSystemTimeTxv.visibility = if (show) View.VISIBLE else View.GONE
        }
        // 电池控件显示设置
        config.canShowBatteryImg.addObserver { show ->
            mxBatteryImg.visibility = if (show) View.VISIBLE else View.GONE
        }

        // 视频宽高变化时分发事件
        config.videoSize.addObserver { size ->
            config.videoListeners.toList().forEach { listener ->
                listener.onVideoSizeChange(size.width, size.height)
            }
        }

        // 播放控件宽高变化时，控制相应按钮的大小、滑动距离标尺等
        config.playerViewSize.addObserver { size ->
            if (size.width <= 0 || size.height <= 0) return@addObserver
            touchHelp.setSize(size.width, size.height)

            val fullScreen = config.screen.get() == MXScreen.FULL
            val playWidth = if (fullScreen) {
                (min(size.width, size.height) / 4)
            } else {
                mxVideo.resources.getDimensionPixelOffset(R.dimen.mx_player_size_icon_width)
            }
            setViewSize(mxPlayPauseImg, playWidth, 0)
            setViewSize(mxReplayImg, playWidth, playWidth / 5)

            val loadingWidth = if (fullScreen) {
                ((min(size.width, size.height) * 48) / (4 * 50))
            } else {
                mxVideo.resources.getDimensionPixelOffset(R.dimen.mx_player_size_loading_width)
            }
            setViewSize(mxLoading, loadingWidth, 0)
        }

        // 控制是否可以被用户快进快退
        config.canSeekByUser.addObserver {
            mxSeekProgress.isEnabled = config.sourceCanSeek()
        }
        // 播放源变化时，控制视频是否可以被用户快进快退
        config.source.addObserver {
            mxSeekProgress.isEnabled = config.sourceCanSeek()
        }

        // 状态更新
        config.state.addObserver { state ->
            processState(state)
        }

        // 播放进度更新时，设置页面状态+事件分发
        position.addObserver { pair ->
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

        // 加载状态变化时，设置页面状态+事件分发
        config.loading.addObserver { loading ->
            mxLoading.visibility = if (loading) View.VISIBLE else View.GONE
            mxLoadingTxv.visibility = if (loading) View.VISIBLE else View.GONE
            config.videoListeners.toList().forEach { listener ->
                listener.onBuffering(loading)
            }
        }

        // 播放按钮点击事件处理
        mxPlayBtn.setOnClickListener {
            val source = config.source.get()
            val state = config.state.get()
            if (source == null) { // 播放源=null时，处理回调事件
                config.videoListeners.toList().forEach { listener ->
                    listener.onEmptyPlay()
                }
                return@setOnClickListener
            }
            val player = mxVideo.getPlayer()
            // 符合条件 1：播放中  2：可以被用户暂停  3：非直播源时，暂停播放
            if (state == MXState.PLAYING && config.canPauseByUser.get() && !source.isLiveSource) {
                mxVideo.pausePlay()
                return@setOnClickListener
            }

            // 暂停状态时点击，恢复播放
            if (state == MXState.PAUSE) {
                mxVideo.continuePlay()
                return@setOnClickListener
            }

            // 预加载完成时点击，启动播放
            if (state == MXState.PREPARED) {
                if (player != null) {
                    player.start()
                    mxVideo.seekToWhenPlay()
                    config.state.set(MXState.PLAYING)
                }
                return@setOnClickListener
            }

            // 预加载时点击，去除预加载状态并刷新当前页面的显示状态
            if (config.isPreloading.get() && state == MXState.PREPARING) { // 预加载完成
                config.isPreloading.set(false)
                config.state.notifyChange()
                return@setOnClickListener
            }

            // 还未开播时点击，开始播放
            if (state == MXState.NORMAL) {
                mxVideo.startPlay()
                return@setOnClickListener
            }
        }

        // 点击屏幕响应
        mxPlayerRootLay.setOnClickListener(object : MXDoubleClickListener() {
            override fun onClick() {
                if (config.state.get() == MXState.PLAYING) {
                    // 播放中单击：控制暂停按钮显示与隐藏
                    playingControlShow.set(!playingControlShow.get())
                }
            }

            override fun onDoubleClick() {
                val state = config.state.get()
                val source = config.source.get() ?: return
                if (state == MXState.PLAYING && config.canPauseByUser.get() && !source.isLiveSource) {
                    mxVideo.pausePlay()
                } else if (state == MXState.PAUSE) {
                    mxVideo.continuePlay()
                }
            }
        })

        // 播放时，控件显示3秒后隐藏回调
        timeDelay.setDelayRun(4000) {
            if (config.state.get() == MXState.PLAYING) {
                playingControlShow.set(false)
            }
        }

        // 网速刷新
        speedHelp.setOnSpeedUpdate { speed ->
            if (config.canShowNetSpeed.get()) {
                mxLoadingTxv.text = speed
            } else {
                mxLoadingTxv.text = null
            }
        }

        // 进度条循环刷新
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

        // 左右、左边上下、右边上下滑动事件处理
        mxPlayerRootLay.setOnTouchListener { _, motionEvent ->
            val mScreen = config.screen.get()
            val mState = config.state.get()

            if (mScreen == MXScreen.FULL && mState == MXState.PLAYING) {
                // 全屏且正在播放才会触发触摸滑动
                return@setOnTouchListener touchHelp.onTouch(motionEvent)
            }
            return@setOnTouchListener false
        }
        // 左右滑动 快进快退事件
        touchHelp.horizontalTouch = MXBaseTouchListener(this, config, timeDelay)
        // 右侧上下滑动 声音大小调节
        touchHelp.verticalRightTouch = MXVolumeTouchListener(this)
        // 左侧上下滑动 亮度大小调节
        touchHelp.verticalLeftTouch = MXBrightnessTouchListener(this)

        // 重试播放
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

        // 重新播放
        mxReplayLay.setOnClickListener {
            mxVideo.startPlay()
        }

        // 全屏按钮响应
        mxFullscreenBtn.setOnClickListener {
            if (!config.canFullScreen.get()) return@setOnClickListener
            if (config.screen.get() == MXScreen.NORMAL) {
                mxVideo.gotoFullScreen()
            } else {
                mxVideo.gotoNormalScreen()
            }
        }

        // 全屏返回按钮响应
        mxReturnBtn.setOnClickListener {
            if (config.screen.get() == MXScreen.FULL) {
                mxVideo.gotoNormalScreen()
            }
        }
    }

    /**
     * 状态处理
     */
    private fun processState(state: MXState) {
        val isLiveSource = (config.source.get()?.isLiveSource == true)
        val isFullScreen = (config.screen.get() == MXScreen.FULL)
        val isPreloading = config.isPreloading.get()
        var mxBottomSeekProgressShow: Boolean? = false // 底部导航栏
        var mxPlaceImgShow: Boolean? = false // 背景图
        var mxPlayBtnShow: Boolean? = false // 播放按钮
        var mxLoadingShow: Boolean? = false // 加载中图
        var mxTopLayShow: Boolean? = false // 顶部标题、返回按钮层容器
        var mxBottomLayShow: Boolean? = false // 底部进度条、文字进度层容器
        var mxRetryLayShow: Boolean? = false // 错误后重新播放按钮
        var mxReplayLayShow: Boolean? = false // 播放完后重新播放按钮
        timeTicket.stop()

        when (state) {
            MXState.IDLE, MXState.NORMAL -> {
                mxPlaceImgShow = true
                mxPlayBtnShow = true
                if (isFullScreen) {
                    mxTopLayShow = true
                }
                if (isPreloading) {
                    mxLoadingShow = false
                }
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                position.set(0 to 0)
            }
            MXState.PREPARING -> {
                speedHelp.start()

                mxPlaceImgShow = true
                if (isFullScreen) {
                    mxTopLayShow = true
                }
                if (isPreloading) {
                    mxPlayBtnShow = true
                } else {
                    mxLoadingShow = true
                }
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            }
            MXState.PREPARED -> {
                mxPlaceImgShow = true
                if (isPreloading) {
                    mxPlayBtnShow = true
                } else {
                    mxLoadingShow = true
                }

                MXUtils.findWindows(mxVideo.context)
                    ?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                timeTicket.start()
            }
            MXState.PLAYING -> {
                mxBottomSeekProgressShow = config.canShowBottomSeekBar.get()
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_pause)
                mxSeekProgress.setOnSeekBarChangeListener(onSeekBarListener)
                if (isLiveSource) { // 直播流，需要隐藏一些按钮
                    mxSeekProgress.visibility = View.INVISIBLE
                    mxCurrentTimeTxv.visibility = View.GONE
                    mxTotalTimeTxv.visibility = View.GONE
                    mxPlayBtnShow = false
                } else {
                    mxSeekProgress.visibility = View.VISIBLE
                    mxCurrentTimeTxv.visibility = View.VISIBLE
                    mxTotalTimeTxv.visibility = View.VISIBLE
                }

                playingControlShow.set(false)
                config.isPreloading.set(false)
                timeTicket.start()
            }
            MXState.PAUSE -> {
                mxTopLayShow = true
                mxPlayBtnShow = true
                mxBottomLayShow = true
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                timeTicket.start()
            }
            MXState.ERROR -> {
                speedHelp.stop()

                mxPlaceImgShow = true
                mxRetryLayShow = true
                if (isFullScreen) {
                    mxTopLayShow = true
                }

                MXUtils.findWindows(mxVideo.context)
                    ?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            MXState.COMPLETE -> {
                speedHelp.stop()

                mxPlaceImgShow = true
                mxReplayLayShow = true
                if (isFullScreen) {
                    mxTopLayShow = true
                }


                MXUtils.findWindows(mxVideo.context)
                    ?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        setViewShow(mxBottomSeekProgress, mxBottomSeekProgressShow)
        setViewShow(mxPlaceImg, mxPlaceImgShow)
        setViewShow(mxPlayBtn, mxPlayBtnShow)

        setViewShow(mxLoading, mxLoadingShow)
        setViewShow(mxLoadingTxv, mxLoadingShow)

        setViewShow(mxTopLay, mxTopLayShow)
        setViewShow(mxBottomLay, mxBottomLayShow)
        setViewShow(mxRetryLay, mxRetryLayShow)
        setViewShow(mxReplayLay, mxReplayLayShow)

        config.videoListeners.toList().forEach { listener ->
            listener.onStateChange(state, this)
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

    fun setViewShow(view: View, show: Boolean?) {
        show ?: return
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setViewSize(view: View, size: Int, padding: Int) {
        val lp = view.layoutParams ?: return
        lp.width = size
        lp.height = size
        view.setPadding(padding, padding, padding, padding)
        view.layoutParams = lp
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

        for (field in this::class.java.declaredFields) {
            val any = field.get(this)
            if (any is MXValueObservable<*>) {
//                MXUtils.log("${field.name} -> deleteObservers")
                any.deleteObservers()
            }
        }

        mxSeekProgress.setOnSeekBarChangeListener(null)
        speedHelp.release()
        timeDelay.release()
        timeTicket.release()
        touchHelp.release()
    }
}