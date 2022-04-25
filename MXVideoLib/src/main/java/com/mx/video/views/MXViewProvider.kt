package com.mx.video.views

import android.view.View
import android.view.WindowManager
import android.widget.*
import com.mx.video.MXVideo
import com.mx.video.R
import com.mx.video.beans.MXConfig
import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXState
import com.mx.video.listener.*
import com.mx.video.utils.*
import com.mx.video.utils.touch.MXTouchHelp
import kotlin.math.min
import kotlin.math.roundToInt

class MXViewProvider(val mxVideo: MXVideo, val config: MXConfig) {
    /**
     * 播放进度
     * first = 当前播放进度 秒
     * second = 视频总长度 秒
     */
    private val position = MXValueObservable(Pair(-1, -1))

    // 播放状态，相关View是否显示
    internal val showWhenPlaying = MXValueObservable(false)

    private val timeTicket = MXTicket()
    private val touchHelp = MXTouchHelp(mxVideo.context)
    private val delayDismiss = MXDismissDelay()
    private val speedHelp = MXNetSpeedHelp()

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

    private fun processPlaceImg() {
        val state = config.state.get()
        setViewShow(
            mxPlaceImg,
            state !in arrayOf(MXState.PLAYING, MXState.PAUSE)
        )
    }

    private fun processLoading() {
        val state = config.state.get()
        if (config.loading.get() && state in arrayOf(MXState.PLAYING, MXState.PAUSE)) {
            setViewShow(mxLoading, true)
            setViewShow(mxLoadingTxv, true)
            return
        }
        if (!config.isPreloading.get() &&
            state in arrayOf(MXState.PREPARING, MXState.PREPARED)
        ) {
            setViewShow(mxLoading, true)
            setViewShow(mxLoadingTxv, true)
            return
        }

        setViewShow(mxLoading, false)
        setViewShow(mxLoadingTxv, false)
    }

    private fun processPlayBtn() {
        val state = config.state.get()

        // 预加载
        if (config.isPreloading.get() &&
            state in arrayOf(MXState.PREPARING, MXState.PREPARED)
        ) {
            setViewShow(mxPlayBtn, true)
            mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            return
        }

        if (state in arrayOf(MXState.IDLE, MXState.NORMAL, MXState.PREPARED, MXState.PAUSE)) {
            mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
        } else {
            mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_pause)
        }

        // 下面几种情况下，显示播放按钮
        if (state in arrayOf(MXState.IDLE, MXState.NORMAL, MXState.PREPARED, MXState.PAUSE)) {
            setViewShow(mxPlayBtn, true)
            return
        }
        // 下面几种情况下，隐藏播放按钮
        if (state in arrayOf(MXState.PREPARING, MXState.ERROR, MXState.COMPLETE)) {
            setViewShow(mxPlayBtn, false)
            return
        }

        // 直播流隐藏播放按钮
        if (config.source.get()?.isLiveSource == true) {
            setViewShow(mxPlayBtn, false)
            return
        }
        // 播放控制需要显示
        if (!showWhenPlaying.get()) {
            setViewShow(mxPlayBtn, false)
            return
        }
        setViewShow(mxPlayBtn, true)
    }

    private fun processTopLay() {
        val state = config.state.get()
        val screen = config.screen.get()

        // 全屏的时候，现实顶部导航栏
        if (screen == MXScreen.FULL &&
            state in arrayOf(
                MXState.IDLE,
                MXState.NORMAL,
                MXState.PREPARING,
                MXState.ERROR,
                MXState.COMPLETE
            )
        ) {
            setViewShow(mxTopLay, true)
            return
        }
        if (showWhenPlaying.get() && state == MXState.PLAYING) {
            setViewShow(mxTopLay, true)
            return
        }
        if (state == MXState.PAUSE) {
            setViewShow(mxTopLay, true)
            return
        }
        setViewShow(mxTopLay, false)
    }

    private fun processBottomLay() {
        val state = config.state.get()
        if (showWhenPlaying.get() && state == MXState.PLAYING) {
            setViewShow(mxBottomLay, true)
            return
        }
        if (state == MXState.PAUSE) {
            setViewShow(mxBottomLay, true)
            return
        }
        setViewShow(mxBottomLay, false)
    }

    private fun processBottomSeekView() {
        // 设置不可见
        if (!config.canShowBottomSeekBar.get()) {
            setViewShow(mxBottomSeekProgress, false)
            return
        }
        // 直播源时隐藏
        if (config.source.get()?.isLiveSource == true) {
            setViewShow(mxBottomSeekProgress, false)
            return
        }

        val state = config.state.get()
        if (state == MXState.PLAYING && !showWhenPlaying.get()) {
            setViewShow(mxBottomSeekProgress, true)
            return
        }
        setViewShow(mxBottomSeekProgress, false)
    }

    fun initView() {
        // 全屏切换时，显示设置
        config.screen.addObserver { screen ->
            processTopLay()

            if (screen == MXScreen.FULL) {
                mxReturnBtn.visibility = View.VISIBLE
                mxFullscreenBtn.setImageResource(R.drawable.mx_icon_small_screen)
            } else {
                mxReturnBtn.visibility = View.GONE
                mxFullscreenBtn.setImageResource(R.drawable.mx_icon_full_screen)
            }

            config.playerViewSize.notifyChange()
            config.videoListeners.toList().forEach { listener ->
                listener.onScreenChange(screen, this)
            }
        }

        showWhenPlaying.addObserver { show ->
            processPlayBtn()
            processTopLay()
            processBottomLay()
            processBottomSeekView()

            if (config.state.get() == MXState.PLAYING && show) {
                delayDismiss.start()
            } else {
                delayDismiss.stop()
            }
        }

        config.canShowBottomSeekBar.addObserver {
            processBottomSeekView()
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

            val fullScreen = (config.screen.get() == MXScreen.FULL)
            val playWidth = if (fullScreen) {
                (min(size.width, size.height) / 5f).roundToInt()
            } else {
                mxVideo.resources.getDimensionPixelOffset(R.dimen.mx_player_size_icon_width)
            }
            setViewSize(mxPlayPauseImg, playWidth)
            setViewSize(mxReplayImg, playWidth)
            setViewSize(mxLoading, (playWidth * (60f / 56f)).roundToInt())
        }

        // 控制是否可以被用户快进快退
        config.canSeekByUser.addObserver {
            mxSeekProgress.isEnabled = config.sourceCanSeek()
        }
        // 播放源变化时，控制视频是否可以被用户快进快退
        config.source.addObserver { source ->
            mxSeekProgress.isEnabled = config.sourceCanSeek()

            if (source?.isLiveSource == true) {
                mxSeekProgress.visibility = View.INVISIBLE
                mxCurrentTimeTxv.visibility = View.INVISIBLE
                mxTotalTimeTxv.visibility = View.INVISIBLE
            } else {
                mxSeekProgress.visibility = View.VISIBLE
                mxCurrentTimeTxv.visibility = View.VISIBLE
                mxTotalTimeTxv.visibility = View.VISIBLE
            }
        }

        // 状态更新
        config.state.addObserver { state ->
            processPlayBtn()
            processPlaceImg()
            processLoading()
            processTopLay()
            processBottomLay()
            processBottomSeekView()

            processState(state)
        }

        // 播放进度更新时，设置页面状态+事件分发
        position.addObserver { pair ->
            val source = config.source.get() ?: return@addObserver
            val position = pair.first
            val duration = pair.second
            mxSeekProgress.max = duration
            mxSeekProgress.progress = position
            mxBottomSeekProgress.max = duration
            mxBottomSeekProgress.progress = position
            mxCurrentTimeTxv.text = MXUtils.stringForTime(position)
            mxTotalTimeTxv.text = MXUtils.stringForTime(duration)

            if (duration > 0 && position > 0 && source.enableSaveProgress) {
                MXUtils.saveProgress(source.playUri, position)
            }

            config.videoListeners.toList().forEach { listener ->
                listener.onPlayTicket(position, duration)
            }
        }

        // 加载状态变化时，设置页面状态+事件分发
        config.loading.addObserver { loading ->
            processLoading()

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
                    showWhenPlaying.set(!showWhenPlaying.get())
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
        delayDismiss.setDelayRun(4000) {
            if (config.state.get() == MXState.PLAYING) {
                showWhenPlaying.set(false)
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
        timeTicket.setTicketRun {
            val state = config.state.get()
            if (state in arrayOf(
                    MXState.PREPARED,
                    MXState.PLAYING,
                    MXState.PAUSE
                )
            ) {
                val duration = mxVideo.getDuration()
                val position = mxVideo.getCurrentPosition()
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
        touchHelp.horizontalTouch = positionSeekTouchListener
        // 右侧上下滑动 声音大小调节
        touchHelp.verticalRightTouch = volumeSeekTouchListener
        // 左侧上下滑动 亮度大小调节
        touchHelp.verticalLeftTouch = brightnessSeekTouchListener

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
        if (state == MXState.PLAYING) {
            mxSeekProgress.setOnSeekBarChangeListener(onSeekBarListener)
            showWhenPlaying.set(false)
            config.isPreloading.set(false)
        }

        // 开始播放+暂停状态时，刷新一次加载中状态
        if (state in arrayOf(MXState.PLAYING, MXState.PAUSE)) {
            config.loading.notifyChange()
        }

        if (state == MXState.PREPARING) {// 屏幕常亮
            position.set(0 to 0)
            timeTicket.start()
            speedHelp.start()
            MXUtils.findWindows(mxVideo.context)
                ?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else if (state == MXState.ERROR || state == MXState.COMPLETE) { //取消屏幕常亮
            timeTicket.stop()
            speedHelp.stop()
            MXUtils.findWindows(mxVideo.context)
                ?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setViewShow(mxReplayLay, state == MXState.COMPLETE)
        setViewShow(mxRetryLay, state == MXState.ERROR)
        config.videoListeners.toList().forEach { listener ->
            listener.onStateChange(state, this)
        }
    }

    private val onSeekBarListener = object : MXProgressSeekListener(config) {
        override fun onSeekStart() {
            delayDismiss.stop()
            timeTicket.stop()
        }

        override fun onSeekChange(seekTo: Int) {
            mxCurrentTimeTxv.text = MXUtils.stringForTime(touchProgress)
        }

        override fun onSeekStop(seekTo: Int) {
            mxVideo.seekTo(seekTo)
            delayDismiss.start()
            timeTicket.start()
        }
    }

    private val positionSeekTouchListener = object : MXProgressSeekTouchListener(config, position) {
        override fun onSeekStart() {
            showWhenPlaying.set(false)
            timeTicket.stop()
            mxQuickSeekLay.visibility = View.VISIBLE
        }

        override fun onSeekChange(position: Int, duration: Int) {
            mxQuickSeekCurrentTxv.text = MXUtils.stringForTime(position)
            mxQuickSeekMaxTxv.text = MXUtils.stringForTime(duration)
            mxBottomSeekProgress.progress = position
        }

        override fun onSeekStop(position: Int, duration: Int) {
            mxQuickSeekLay.visibility = View.GONE
            mxVideo.seekTo(position)
            config.loading.notifyChange()
            delayDismiss.start()
            timeTicket.start()
        }
    }

    private val volumeSeekTouchListener = object : MXVolumeTouchListener(mxVideo.context) {
        override fun onSeekStart() {
            mxVolumeLightLay.visibility = View.VISIBLE
            mxVolumeLightTypeTxv.setText(R.string.mx_play_volume)
        }

        override fun onSeekChange(volume: Int, maxVolume: Int) {
            mxVolumeLightTxv.text = "$volume / $maxVolume"
        }

        override fun onSeekStop(volume: Int, maxVolume: Int) {
            mxVolumeLightLay.visibility = View.GONE
        }
    }

    private val brightnessSeekTouchListener = object : MXBrightnessTouchListener(mxVideo.context) {
        override fun onSeekStart() {
            mxVolumeLightLay.visibility = View.VISIBLE
            mxVolumeLightTypeTxv.setText(R.string.mx_play_brightness)
        }

        override fun onSeekChange(brightness: Int, maxBrightness: Int) {
            val percent = (brightness * 100.0 / maxBrightness).roundToInt()
            mxVolumeLightTxv.text = "${percent}%"
        }

        override fun onSeekStop(brightness: Int, maxBrightness: Int) {
            mxVolumeLightLay.visibility = View.GONE
        }
    }

    private val viewMap = HashMap<View, Boolean?>()
    private fun setViewShow(view: View, show: Boolean?) {
        show ?: return
        if (viewMap[view] == show) return
        view.visibility = if (show) View.VISIBLE else View.GONE
        viewMap[view] = show
    }

    private fun setViewSize(view: View, size: Int) {
        val lp = view.layoutParams ?: return
        lp.width = size
        lp.height = size
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
        viewMap.clear()

        mxSeekProgress.setOnSeekBarChangeListener(null)
        speedHelp.release()
        delayDismiss.release()
        timeTicket.release()
        touchHelp.release()
    }
}