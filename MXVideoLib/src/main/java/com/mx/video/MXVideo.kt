package com.mx.video

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.mx.video.beans.*
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import com.mx.video.utils.MXSensorHelp
import com.mx.video.utils.MXUtils
import com.mx.video.utils.MXVideoListener
import com.mx.video.views.MXTextureView
import com.mx.video.views.MXViewProvider

abstract class MXVideo @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private var hasWifiDialogShow = false
        private val parentMap = HashMap<Int, MXParentView>()

        private var playingVideo: MXVideo? = null
        fun isFullScreen(): Boolean {
            return playingVideo?.isFullScreen() == true
        }

        fun gotoNormalScreen() {
            playingVideo?.gotoNormalScreen()
        }

        /**
         * 当前播放设为全屏
         */
        fun gotoFullScreen() {
            playingVideo?.gotoFullScreen()
        }

        /**
         * 停止当前播放
         */
        fun stopAll() {
            playingVideo?.stopPlay()
        }

        /**
         * 释放当前播放器
         */
        fun releaseAll() {
            playingVideo?.release()
        }

        /**
         * 清理进度条
         */
        fun clearProgress() {
            MXUtils.clearProgress()
        }

        fun setDebug(debug: Boolean) {
            MXUtils.setDebug(debug)
        }
    }

    /**
     * 父容器封装
     */
    private data class MXParentView(
        val index: Int,
        val parentViewGroup: ViewGroup,
        val layoutParams: ViewGroup.LayoutParams,
        val width: Int,
        val height: Int
    )

    /**
     * 布局资源文件
     */
    abstract fun getLayoutId(): Int

    /**
     * 播放器
     */
    private var mxPlayerClass: Class<*>? = null

    /**
     * 播放器实例
     */
    private var mxPlayer: IMXPlayer? = null

    /**
     * 当前TextureView
     */
    private var mxTextureView: MXTextureView? = null

    /**
     * 共享配置
     */
    private val config = MXConfig()

    /**
     * 视图处理器
     */
    private val provider by lazy { MXViewProvider(this, config) }

    /**
     * 设备旋转感应器
     */
    private val sensorHelp by lazy { MXSensorHelp.instance }

    /**
     * 播放器 暂停状态
     */
    private var isStopState: Boolean = false

    init {
        MXUtils.init(context)
        View.inflate(context, getLayoutId(), this)
        isFocusable = false
        isFocusableInTouchMode = false
        provider.initView()
        config.state.set(MXState.IDLE)
        config.canFullScreen.addObserver { can ->
            if (!can && config.screen.get() == MXScreen.FULL) {
                gotoNormalScreen()
            }
        }
        config.screen.addObserver { screen ->
            val windows = MXUtils.findWindowsDecorView(context) ?: return@addObserver
            when (screen) {
                MXScreen.FULL -> {
                    if (parentMap.containsKey(config.viewIndexId)) {
                        return@addObserver
                    }
                    val parent = (parent as ViewGroup?) ?: return@addObserver

                    val item = MXParentView(
                        parent.indexOfChild(this),
                        parent, layoutParams, width, height
                    )
                    parent.removeView(this)
                    cloneMeToLayout(item)
                    parentMap[config.viewIndexId] = item

                    val fullLayout = LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    windows.addView(this, fullLayout)

                    MXUtils.setFullScreen(context)
                    if (config.willChangeOrientationWhenFullScreen()) {
                        var orientation = sensorHelp.getOrientation()
                        if (orientation.isVertical()) {
                            orientation = MXOrientation.DEGREE_270
                        }
                        MXUtils.setScreenOrientation(context, orientation)
                    }

                    postInvalidate()
                }
                MXScreen.NORMAL -> {
                    val parentItem = parentMap.remove(config.viewIndexId) ?: return@addObserver
                    windows.removeView(this)
                    parentItem.parentViewGroup.removeViewAt(parentItem.index)
                    parentItem.parentViewGroup.addView(
                        this,
                        parentItem.index,
                        parentItem.layoutParams
                    )

                    MXUtils.recoverScreenOrientation(context)
                    MXUtils.recoverFullScreen(context)
                    postInvalidate()
                }
            }
        }
    }

    fun addOnVideoListener(listener: MXVideoListener) {
        MXUtils.log("MXVideo: addOnVideoListener()")
        if (!config.videoListeners.contains(listener)) {
            config.videoListeners.add(listener)
        }
    }

    fun clearListener() {
        MXUtils.log("MXVideo: clearListener()")
        config.videoListeners.clear()
    }

    fun removeOnVideoListener(listener: MXVideoListener) {
        MXUtils.log("MXVideo: removeOnVideoListener()")
        config.videoListeners.remove(listener)
    }

    /**
     * 获取占位图ImageView
     */
    fun getPosterImageView() = provider.mxPlaceImg

    /**
     * 获取Config
     */
    fun getConfig() = config

    /**
     * 获取状态值
     */
    fun getState() = config.state.get()

    /**
     * 获取播放器
     */
    fun getPlayer() = mxPlayer


    /**
     * 设置播放数据源
     * @param source 播放源
     * @param player 播放器类
     * @param seekTo 跳转 >=0 时播放后会跳转到对应时间，单位：秒
     */
    open fun setSource(
        source: MXPlaySource?,
        player: Class<out IMXPlayer>? = null,
        seekTo: Int = -1
    ) {
        stopPlay()

        if (source == config.source.get()) {
            return
        }
        MXUtils.log("MXVideo: setSource()")
        isStopState = false

        config.source.set(source)
        mxPlayerClass = player

        config.seekWhenPlay.set(seekTo)
        provider.mxTitleTxv.text = source?.title
        if (source != null) {
            config.state.set(MXState.NORMAL)
        } else {
            config.state.set(MXState.IDLE)
        }
    }

    /**
     * @hide
     * 设置方向
     */
    open fun setTextureOrientation(orientation: MXOrientation) {
        MXUtils.log("MXVideo: setTextureOrientation()")
        config.orientation.set(orientation)
        mxTextureView?.setOrientation(orientation)
    }

    /**
     * 跳转
     */
    open fun seekTo(seek: Int) {
        MXUtils.log("MXVideo: seekTo() ${MXUtils.stringForTime(seek)}")
        val player = mxPlayer
        if (player != null && config.state.get() in arrayOf(MXState.PLAYING, MXState.PAUSE)) {
            player.seekTo(seek)
        } else {
            config.seekWhenPlay.set(seek)
        }
    }

    /**
     * 设置缩放方式
     * MXScale.FILL_PARENT  当父容器宽高一定时，填满宽高
     * MXScale.CENTER_CROP  根据视频宽高自适应
     */
    open fun setScaleType(type: MXScale) {
        MXUtils.log("MXVideo: setScaleType() ${type.name}")
        config.scale.set(type)
        mxTextureView?.setDisplayType(type)
    }

    /**
     * 开始构建播放流程，预加载完成后立即播放
     */
    open fun startPlay() {
        MXUtils.log("MXVideo: startPlay()")
        stopPlay()
        config.isPreloading.set(false)
        startVideo()
    }

    /**
     * 暂停播放
     */
    open fun pausePlay() {
        MXUtils.log("MXVideo: pausePlay()")
        if (config.state.get() != MXState.PLAYING) return
        if (!config.canPauseByUser.get()) return
        val source = config.source.get() ?: return
        if (source.isLiveSource) return

        mxPlayer?.pause()
        config.state.set(MXState.PAUSE)
    }

    /**
     * 暂停播放后，继续播放
     */
    open fun continuePlay() {
        MXUtils.log("MXVideo: continuePlay()")
        if (config.state.get() != MXState.PAUSE) return
        mxPlayer?.start()
        config.state.set(MXState.PLAYING)
    }

    /**
     * Activity/Fragment 生命周期onStart() 需要调用暂停
     */
    open fun onStart() {
        MXUtils.log("MXVideo: onStart()")
        if (!isStopState) return
        val source = config.source.get() ?: return
        if (source.isLiveSource) {
            startPlay()
        } else {
            if (config.state.get() == MXState.PAUSE) {
                continuePlay()
            }
        }
        isStopState = false
    }

    /**
     * Activity/Fragment 生命周期onStop() 需要调用暂停
     */
    open fun onStop() {
        MXUtils.log("MXVideo: onStop()")
        if (config.state.get() != MXState.PLAYING) return
        val source = config.source.get() ?: return
        if (source.isLiveSource) {// 直播无法暂停，这里直接停止播放
            stopPlay()
        } else {
            pausePlay()
        }
        isStopState = true
    }

    /**
     * 开始构建播放流程，在预加载完成后不立即播放
     */
    open fun startPreload() {
        MXUtils.log("MXVideo: startPreload()")
        stopPlay()
        config.isPreloading.set(false)
        val source = config.source.get() ?: return
        val isLiveSource = source.isLiveSource
        if (isLiveSource) {
            // 直播源不支持预加载
            return
        }
        val seekTo = getSeekPosition()
        if (seekTo > 0) {
            // 这里暂时只支持seek=0的预加载
            return
        }

        config.isPreloading.set(true)
        startVideo()
    }

    /**
     * 开始构建播放流程
     * 1：释放播放资源
     * 2：判断播放地址、播放器
     * 3：判断WiFi状态
     * 4：创建播放器，开始播放
     */
    private fun startVideo() {
        MXUtils.log("MXVideo: startVideo()")
        playingVideo?.stopPlay()
        val clazz = mxPlayerClass ?: MXSystemPlayer::class.java
        val source = config.source.get() ?: return
        MXUtils.log("startVideo ${source.playUri} player=${clazz.name}")

        val startRun = {
            val constructor = clazz.getConstructor()
            val player = (constructor.newInstance() as IMXPlayer)
            player.setSource(source)
            val textureView = addTextureView(player)
            player.setMXVideo(this, textureView)
            mxPlayer = player
            playingVideo = this
            config.state.set(MXState.PREPARING)
            sensorHelp.addListener(sensorListener)
        }
        if (!MXUtils.isWifiConnected(context) && config.showTipIfNotWifi.get() && !hasWifiDialogShow) {
            AlertDialog.Builder(context).apply {
                setMessage(R.string.mx_play_wifi_notify)
                setPositiveButton(context.getString(R.string.mx_play_wifi_dialog_continue)) { _, _ ->
                    hasWifiDialogShow = true
                    startRun.invoke()
                }
                setNegativeButton(context.getString(R.string.mx_play_wifi_dialog_cancel), null)
            }.create().show()
            return
        } else {
            startRun.invoke()
        }
    }

    private fun addTextureView(player: IMXPlayer): MXTextureView {
        provider.mxSurfaceContainer.removeAllViews()
        val textureView = MXTextureView(context.applicationContext)
        val size = config.videoSize.get()
        textureView.setVideoSize(size.width, size.height)
        textureView.setDisplayType(config.scale.get())
        textureView.setOrientation(config.orientation.get())

        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER

        provider.mxSurfaceContainer.addView(textureView, layoutParams)
        textureView.surfaceTextureListener = player
        this.mxTextureView = textureView
        return textureView
    }

    /**
     * 视频已经准备好,但不是已经开始播放!
     */
    open fun onPlayerPrepared() {
        val player = mxPlayer ?: return

        config.state.set(MXState.PREPARED)
        if (config.isPreloading.get()) {
            MXUtils.log("MXVideo: onPlayerPrepared -> need click start button to play")
        } else {
            MXUtils.log("MXVideo: onPlayerPrepared -> start play")
            player.start()
            seekToWhenPlay()
        }
    }

    /**
     * 播放前跳转
     * 必须在player.start()调用之后再使用
     */
    open fun seekToWhenPlay() {
        val player = mxPlayer ?: return
        val seekTo = getSeekPosition()
        if (seekTo > 0) {
            MXUtils.log("MXVideo: seekToWhenPlay(${seekTo})")
            player.seekTo(seekTo)
        }
        config.seekWhenPlay.set(-1)
    }

    /**
     * 获取需要跳转的位置
     */
    private fun getSeekPosition(): Int {
        val source = config.source.get() ?: return -1
        if (config.seekWhenPlay.get() >= 0) { // 注意：这里seekWhenPlay=0时需要默认从0开始播放
            return config.seekWhenPlay.get()
        }
        if (source.enableSaveProgress) {
            // 默认seek提前5秒
            val seekTo = (MXUtils.getProgress(source.playUri) - 5)
            if (seekTo > 0) return seekTo
        }
        return -1
    }

    /**
     * 视频正式开始播放
     */
    open fun onPlayerStartPlay() {
        MXUtils.log("MXVideo: onPlayerStartPlay()")
        if (config.state.get() in arrayOf(MXState.PREPARED, MXState.PREPARING)) {
            config.state.set(MXState.PLAYING)
        }
    }

    /**
     * 视频播放完成
     */
    open fun onPlayerCompletion() {
        MXUtils.log("MXVideo: onPlayerCompletion()")
        config.source.get()?.playUri?.let { MXUtils.saveProgress(it, 0) }
        mxPlayer?.release()
        config.state.set(MXState.COMPLETE)

        if (config.gotoNormalScreenWhenComplete.get() && config.screen.get() == MXScreen.FULL) {
            switchToScreen(MXScreen.NORMAL)
        }
    }

    /**
     * 视频缓冲进度
     * @param 0-100
     */
    open fun onPlayerBufferProgress(percent: Int) {
//        MXUtils.log("onPlayerBufferProgress:$percent")
    }

    /**
     * 视频快进完成
     */
    open fun onPlayerSeekComplete() {
    }

    /**
     * 视频播放错误信息
     */
    open fun onPlayerError(error: String?) {
        MXUtils.log("MXVideo: onPlayerError() $error")
        if (config.source.get()?.isLiveSource == true
            && config.replayLiveSourceWhenError.get()
            && (config.state.get() in arrayOf(
                MXState.PLAYING,
                MXState.PAUSE,
                MXState.PREPARING,
                MXState.PREPARED
            ))
        ) {
            // 直播重试
            startPlay()
            return
        }

        if (config.isPreloading.get() && config.state.get() == MXState.PREPARING) {
            // 预加载失败，状态重置成NORMAL
            config.isPreloading.set(false)
            stopPlay()
            return
        }
        config.state.set(MXState.ERROR)
        if (config.gotoNormalScreenWhenError.get() && config.screen.get() == MXScreen.FULL) {
            switchToScreen(MXScreen.NORMAL)
        }
    }

    /**
     * 视频缓冲状态变更
     * @param start
     *  true = 开始缓冲
     *  false = 结束缓冲
     */
    open fun onPlayerBuffering(start: Boolean) {
        MXUtils.log("MXVideo: onPlayerBuffering() $start")
        config.loading.set(start)
    }

    /**
     * 视频获得宽高
     */
    open fun onPlayerVideoSizeChanged(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val size = config.videoSize.get()
        if (width == size.width && height == size.height) return

        MXUtils.log("MXVideo: onPlayerVideoSizeChanged() $width x $height")
        config.videoSize.set(MXSize(width, height))
        mxTextureView?.setVideoSize(width, height)
        postInvalidate()
    }

    /**
     * 结束播放
     */
    open fun stopPlay() {
        val player = mxPlayer
        mxTextureView = null
        mxPlayer = null

        if (player != null) {
            MXUtils.log("MXVideo: stopPlay()")
            player.release()
        }

        provider.mxSurfaceContainer.removeAllViews()

        if (playingVideo == this) {
            playingVideo = null
        }

        sensorHelp.deleteListener(sensorListener)

        if (config.source.get() == null) {
            config.state.set(MXState.IDLE)
        } else {
            config.state.set(MXState.NORMAL)
        }
    }

    private var dimensionRatio: Double = 0.0

    /**
     * 设置MXVideo  ViewGroup的宽高比，设置之后会自动计算播放器的高度
     */
    open fun setDimensionRatio(ratio: Double) {
        MXUtils.log("MXVideo: setDimensionRatio()")
        if (ratio != dimensionRatio) {
            this.dimensionRatio = ratio
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (dimensionRatio > 0.0
            && config.screen.get() == MXScreen.NORMAL
            && widthMode == MeasureSpec.EXACTLY
        ) {
            var height = (widthSize / dimensionRatio).toInt()
            if (height > heightSize && heightMode == MeasureSpec.AT_MOST) {
                height = heightSize
            }

            // 当外部设置固定宽高比，且非全屏时，调整测量高度
            val measureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, measureSpec)
            return
        }

        val size = config.videoSize.get()
        if (size.width > 0 && size.height > 0
            && config.screen.get() == MXScreen.NORMAL
            && widthMode == MeasureSpec.EXACTLY
            && heightMode != MeasureSpec.EXACTLY
        ) {
            var height = (widthSize * size.height.toFloat() / size.width).toInt()
            if (height > heightSize && heightMode == MeasureSpec.AT_MOST) {
                height = heightSize
            }

            //  当视频宽高有数据，，且非全屏时，按照视频宽高比调整整个View的高度，默认视频宽高比= 1280 x 720
            val measureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, measureSpec)
            return
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        config.playerViewSize.set(MXSize(w, h))
    }

    /**
     * 切换全屏、小屏显示
     */
    private fun switchToScreen(screen: MXScreen) {
        MXUtils.log("MXVideo: switchToScreen()  ${config.screen.get().name} -> ${screen.name}")
        MXUtils.findWindowsDecorView(context) ?: return
        if (!config.canFullScreen.get() && screen == MXScreen.FULL) {
            return
        }
        config.screen.set(screen)
    }

    private fun cloneMeToLayout(target: MXParentView) {
        try {
            val constructor = this::class.java.getConstructor(Context::class.java)
            val selfClone = constructor.newInstance(context)
            selfClone.id = this.id
            selfClone.mxPlayerClass = mxPlayerClass
            selfClone.dimensionRatio = dimensionRatio
            selfClone.config.cloneBy(config)

            selfClone.minimumWidth = target.width
            selfClone.minimumHeight = target.height
            target.parentViewGroup.addView(selfClone, target.index, target.layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 是否正在播放
     */
    open fun isPlaying(): Boolean {
        mxPlayer ?: return false
        return (config.state.get() in arrayOf(
            MXState.PLAYING, MXState.PAUSE,
            MXState.PREPARING, MXState.PREPARED
        ))
    }

    /**
     * 判断是否全屏
     */
    open fun isFullScreen(): Boolean {
        return config.screen.get() == MXScreen.FULL
    }

    /**
     * 切换小屏播放
     */
    open fun gotoNormalScreen() {
        MXUtils.log("MXVideo: gotoNormalScreen()")
        switchToScreen(MXScreen.NORMAL)
    }

    /**
     * 切换全屏播放
     */
    open fun gotoFullScreen() {
        MXUtils.log("MXVideo: gotoFullScreen()")
        switchToScreen(MXScreen.FULL)
    }

    /**
     * 获取总时长
     */
    open fun getDuration(): Int {
        return mxPlayer?.getDuration() ?: 0
    }

    /**
     * 获取当前播放时长
     */
    open fun getCurrentPosition(): Int {
        return mxPlayer?.getCurrentPosition() ?: 0
    }

    /**
     * 重置播放器为 @link{MXState.IDLE} 状态
     */
    open fun reset() {
        MXUtils.log("MXVideo: reset()")
        stopPlay()
        mxPlayerClass = null
        mxPlayer = null
        config.reset()
        postInvalidate()
    }

    private val sensorListener = object : MXSensorListener {
        override fun onChange(orientation: MXOrientation) {
            MXUtils.log("设备方向变更：$orientation")
            if (!isPlaying() || !config.willChangeOrientationWhenFullScreen()) {
                // 当不在播放，或者不需要变更方向时，不处理
                return
            }
            val screen = config.screen.get()

            if (config.autoRotateBySensorWhenFullScreen.get() && screen == MXScreen.FULL) {
                // 全屏时，方向切换，变更一下
                MXUtils.setScreenOrientation(context, orientation)
            }

            if (config.autoFullScreenBySensor.get()) {
                if (orientation.isHorizontal() && screen == MXScreen.NORMAL) {
                    switchToScreen(MXScreen.FULL)
                }
            }
        }
    }

    /**
     * 播放器未知信息回调
     */
    fun onPlayerInfo(what: Int, extra: Int) {
        MXUtils.log("MXVideo: onPlayerInfo $what -> $extra")
    }

    /**
     * 销毁Activity或Fragment时调用
     * 销毁后，不能再次进行播放操作
     */
    open fun release() {
        MXUtils.log("MXVideo: release()")
        config.release()
        provider.release()
        sensorHelp.deleteListener(sensorListener)
        parentMap.remove(config.viewIndexId)
        stopPlay()
    }
}