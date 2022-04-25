package com.mx.video.views

import android.view.View
import android.view.WindowManager
import com.mx.video.R
import com.mx.video.base.IMXVideo
import com.mx.video.beans.MXConfig
import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXState
import com.mx.video.listener.*
import com.mx.video.utils.*
import com.mx.video.utils.touch.MXTouchHelp
import kotlin.math.min
import kotlin.math.roundToInt

internal class MXViewProvider(val viewSet: MXViewSet, val mxVideo: IMXVideo, val config: MXConfig) {
    /**
     * 播放进度
     * first = 当前播放进度 秒
     * second = 视频总长度 秒
     */
    private val position = MXValueObservable(Pair(-1, -1))

    // 播放状态，相关View是否显示
    internal val showWhenPlaying = MXValueObservable(false)

    private val timeTicket = MXTicket()
    private val touchHelp = MXTouchHelp(viewSet.context)
    private val delayDismiss = MXDismissDelay()
    private val speedHelp = MXNetSpeedHelp()

    fun initView() {
        // 全屏切换时，显示设置
        config.screen.addObserver { screen ->
            viewSet.processTopLay(showWhenPlaying.get())

            if (screen == MXScreen.FULL) {
                viewSet.setViewShow(viewSet.mxReturnBtn, true)
                viewSet.mxFullscreenBtn.setImageResource(R.drawable.mx_icon_small_screen)
            } else {
                viewSet.setViewShow(viewSet.mxReturnBtn, false)
                viewSet.mxFullscreenBtn.setImageResource(R.drawable.mx_icon_full_screen)
            }

            config.playerViewSize.notifyChange()
            config.videoListeners.toList().forEach { listener ->
                listener.onScreenChange(screen, viewSet)
            }
        }

        showWhenPlaying.addObserver { show ->
            viewSet.processPlayBtn(show)
            viewSet.processTopLay(show)
            viewSet.processBottomLay(show)
            viewSet.processBottomSeekView(show)

            if (config.state.get() == MXState.PLAYING && show) {
                delayDismiss.start()
            } else {
                delayDismiss.stop()
            }
        }

        config.canShowBottomSeekBar.addObserver {
            viewSet.processBottomSeekView(showWhenPlaying.get())
        }

        // 全屏按钮显示控制
        val fullScreenObserver = { _: Boolean ->
            viewSet.setViewShow(
                viewSet.mxFullscreenBtn,
                config.canFullScreen.get() && config.showFullScreenButton.get()
            )
        }
        config.canFullScreen.addObserver(fullScreenObserver)
        config.showFullScreenButton.addObserver(fullScreenObserver)

        // 时间控件显示设置
        config.canShowSystemTime.addObserver { show ->
            viewSet.setViewShow(viewSet.mxSystemTimeTxv, show)
        }
        // 电池控件显示设置
        config.canShowBatteryImg.addObserver { show ->
            viewSet.setViewShow(viewSet.mxBatteryImg, show)
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
                viewSet.context.resources.getDimensionPixelOffset(R.dimen.mx_player_size_icon_width)
            }
            viewSet.setViewSize(viewSet.mxPlayPauseBtn, playWidth)
            viewSet.setViewSize(viewSet.mxReplayImg, playWidth)
            viewSet.setViewSize(viewSet.mxLoading, (playWidth * (60f / 56f)).roundToInt())
        }

        // 控制是否可以被用户快进快退
        config.canSeekByUser.addObserver {
            viewSet.mxSeekProgress.isEnabled = config.sourceCanSeek()
        }
        // 播放源变化时，控制视频是否可以被用户快进快退
        config.source.addObserver { source ->
            viewSet.mxSeekProgress.isEnabled = config.sourceCanSeek()

            if (source?.isLiveSource == true) {
                viewSet.setViewVisible(viewSet.mxSeekProgress, View.INVISIBLE)
                viewSet.setViewVisible(viewSet.mxCurrentTimeTxv, View.INVISIBLE)
                viewSet.setViewVisible(viewSet.mxTotalTimeTxv, View.INVISIBLE)
            } else {
                viewSet.setViewVisible(viewSet.mxSeekProgress, View.VISIBLE)
                viewSet.setViewVisible(viewSet.mxCurrentTimeTxv, View.VISIBLE)
                viewSet.setViewVisible(viewSet.mxTotalTimeTxv, View.VISIBLE)
            }
        }

        // 预加载状态更新
        config.isPreloading.addObserver {
            viewSet.processLoading()
            viewSet.processPlayBtn(showWhenPlaying.get())
        }

        // 状态更新
        config.state.addObserver { state ->
            viewSet.processPlayBtn(showWhenPlaying.get())
            viewSet.processPlaceImg()
            viewSet.processLoading()
            viewSet.processTopLay(showWhenPlaying.get())
            viewSet.processBottomLay(showWhenPlaying.get())
            viewSet.processBottomSeekView(showWhenPlaying.get())
            viewSet.processOthers()

            processState(state)
        }

        // 播放进度更新时，设置页面状态+事件分发
        position.addObserver { pair ->
            val source = config.source.get() ?: return@addObserver
            val position = pair.first
            val duration = pair.second
            viewSet.mxSeekProgress.max = duration
            viewSet.mxSeekProgress.progress = position
            viewSet.mxBottomSeekProgress.max = duration
            viewSet.mxBottomSeekProgress.progress = position
            viewSet.mxCurrentTimeTxv.text = MXUtils.stringForTime(position)
            viewSet.mxTotalTimeTxv.text = MXUtils.stringForTime(duration)

            if (duration > 0 && position > 0 && source.enableSaveProgress) {
                MXUtils.saveProgress(source.playUri, position)
            }

            config.videoListeners.toList().forEach { listener ->
                listener.onPlayTicket(position, duration)
            }
        }

        // 加载状态变化时，设置页面状态+事件分发
        config.loading.addObserver { loading ->
            viewSet.processLoading()

            config.videoListeners.toList().forEach { listener ->
                listener.onBuffering(loading)
            }
        }

        // 播放按钮点击事件处理
        viewSet.mxPlayPauseBtn.setOnClickListener {
            val source = config.source.get()
            val state = config.state.get()
            if (source == null) { // 播放源=null时，处理回调事件
                config.videoListeners.toList().forEach { listener ->
                    listener.onEmptyPlay()
                }
                return@setOnClickListener
            }
            val player = mxVideo
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
                player.continuePlay()
                return@setOnClickListener
            }

            // 预加载时点击，去除预加载状态并刷新当前页面的显示状态
            if (config.isPreloading.get() && state == MXState.PREPARING) { // 预加载完成
                config.isPreloading.set(false)
                return@setOnClickListener
            }

            // 还未开播时点击，开始播放
            if (state == MXState.NORMAL) {
                mxVideo.startPlay()
                return@setOnClickListener
            }
        }

        // 点击屏幕响应
        viewSet.mxPlayerRootLay.setOnClickListener(object : MXDoubleClickListener() {
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
                viewSet.mxLoadingTxv.text = speed
            } else {
                viewSet.mxLoadingTxv.text = null
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
                val position = mxVideo.getPosition()
                this.position.set(position to duration)
            }
        }

        // 左右、左边上下、右边上下滑动事件处理
        viewSet.mxPlayerRootLay.setOnTouchListener { _, motionEvent ->
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
        viewSet.mxRetryLay.setOnClickListener {
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
        viewSet.mxReplayLay.setOnClickListener {
            mxVideo.startPlay()
        }

        // 全屏按钮响应
        viewSet.mxFullscreenBtn.setOnClickListener {
            if (!config.canFullScreen.get()) return@setOnClickListener
            if (config.screen.get() == MXScreen.NORMAL) {
                mxVideo.switchToScreen(MXScreen.FULL)
            } else {
                mxVideo.switchToScreen(MXScreen.NORMAL)
            }
        }

        // 全屏返回按钮响应
        viewSet.mxReturnBtn.setOnClickListener {
            if (config.screen.get() == MXScreen.FULL) {
                mxVideo.switchToScreen(MXScreen.NORMAL)
            }
        }
    }


    /**
     * 状态处理
     */
    private fun processState(state: MXState) {
        if (state == MXState.PLAYING) {
            viewSet.mxSeekProgress.setOnSeekBarChangeListener(onSeekBarListener)
            showWhenPlaying.set(false)
            config.isPreloading.set(false)
        }

        if (state == MXState.PREPARING) {// 屏幕常亮
            position.set(0 to 0)
            timeTicket.start()
            speedHelp.start()
            MXUtils.findWindows(viewSet.context)
                ?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else if (state == MXState.ERROR || state == MXState.COMPLETE) { //取消屏幕常亮
            timeTicket.stop()
            speedHelp.stop()
            MXUtils.findWindows(viewSet.context)
                ?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        config.videoListeners.toList().forEach { listener ->
            listener.onStateChange(state, viewSet)
        }
    }

    private val onSeekBarListener = object : MXProgressSeekListener(config) {
        override fun onSeekStart() {
            delayDismiss.stop()
            timeTicket.stop()
        }

        override fun onSeekChange(seekTo: Int) {
            viewSet.mxCurrentTimeTxv.text = MXUtils.stringForTime(touchProgress)
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

            viewSet.setViewShow(viewSet.mxQuickSeekLay, true)
        }

        override fun onSeekChange(position: Int, duration: Int) {
            viewSet.mxQuickSeekCurrentTxv.text = MXUtils.stringForTime(position)
            viewSet.mxQuickSeekMaxTxv.text = MXUtils.stringForTime(duration)
            viewSet.mxBottomSeekProgress.progress = position
        }

        override fun onSeekStop(position: Int, duration: Int) {
            viewSet.setViewShow(viewSet.mxQuickSeekLay, false)
            mxVideo.seekTo(position)
            config.loading.notifyChange()
            delayDismiss.start()
            timeTicket.start()
        }
    }

    private val volumeSeekTouchListener = object : MXVolumeTouchListener(viewSet.context) {
        override fun onSeekStart() {
            viewSet.setViewShow(viewSet.mxVolumeLightLay, true)
            viewSet.mxVolumeLightTypeTxv.setText(R.string.mx_play_volume)
        }

        override fun onSeekChange(volume: Int, maxVolume: Int) {
            viewSet.mxVolumeLightTxv.text = "$volume / $maxVolume"
        }

        override fun onSeekStop(volume: Int, maxVolume: Int) {
            viewSet.setViewShow(viewSet.mxVolumeLightLay, false)
        }
    }

    private val brightnessSeekTouchListener = object : MXBrightnessTouchListener(viewSet.context) {
        override fun onSeekStart() {
            viewSet.setViewShow(viewSet.mxVolumeLightLay, true)
            viewSet.mxVolumeLightTypeTxv.setText(R.string.mx_play_brightness)
        }

        override fun onSeekChange(brightness: Int, maxBrightness: Int) {
            val percent = (brightness * 100.0 / maxBrightness).roundToInt()
            viewSet.mxVolumeLightTxv.text = "${percent}%"
        }

        override fun onSeekStop(brightness: Int, maxBrightness: Int) {
            viewSet.setViewShow(viewSet.mxVolumeLightLay, false)
        }
    }

    /**
     * 释放资源，释放后无法再次播放
     */
    fun release() {
        val batteryImg = viewSet.mxBatteryImg
        if (batteryImg is MXBatteryImageView) {
            batteryImg.release()
        }

        val timeTxv = viewSet.mxSystemTimeTxv
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

        viewSet.mxSeekProgress.setOnSeekBarChangeListener(null)
        speedHelp.release()
        delayDismiss.release()
        timeTicket.release()
        touchHelp.release()
    }
}