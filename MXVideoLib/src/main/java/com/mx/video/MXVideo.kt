package com.mx.video

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
    private var textureView: MXTextureView? = null

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

    init {
        View.inflate(context, getLayoutId(), this)

        MXSensorHelp.init(context.applicationContext as Application)
        provider.initView()
        provider.setPlayState(MXState.IDLE)
    }

    fun addOnVideoListener(listener: MXVideoListener) {
        if (!config.videoListeners.contains(listener)) {
            config.videoListeners.add(listener)
        }
    }

    fun clearListener() {
        config.videoListeners.clear()
    }

    fun removeOnVideoListener(listener: MXVideoListener) {
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
    fun getState() = provider.mState

    /**
     * 获取视图Provider
     */
    fun getViewProvider() = provider

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
        source: MXPlaySource,
        player: Class<out IMXPlayer>? = null,
        seekTo: Int = -1
    ) {
        stopPlay()
        config.source = source
        mxPlayerClass = player

        config.seekWhenPlay = seekTo
        provider.mxTitleTxv.text = source.title
        provider.setPlayState(MXState.NORMAL)
    }

    /**
     * @hide
     * 设置方向
     */
    open fun setTextureOrientation(orientation: MXOrientation) {
        config.orientation = orientation
        textureView?.setOrientation(orientation)
    }

    /**
     * 跳转
     */
    open fun seekTo(seek: Int) {
        MXUtils.log("seekTo ${MXUtils.stringForTime(seek)}")
        val player = mxPlayer
        if (player != null && provider.mState in arrayOf(MXState.PLAYING, MXState.PAUSE)) {
            player.seekTo(seek)
        } else {
            config.seekWhenPlay = seek
        }
    }

    /**
     * 设置缩放方式
     * MXScale.FILL_PARENT  当父容器宽高一定时，填满宽高
     * MXScale.CENTER_CROP  根据视频宽高自适应
     */
    open fun setScaleType(type: MXScale) {
        config.scale = type
        textureView?.setDisplayType(type)
    }

    /**
     * 开始构建播放流程，预加载完成后立即播放
     */
    open fun startPlay() {
        stopPlay()
        config.isPreloading = false
        startVideo()
    }

    /**
     * 暂停播放
     */
    open fun pausePlay() {
        if (provider.mState != MXState.PLAYING) return
        if (!config.canPauseByUser) return
        val source = config.source ?: return
        if (source.isLiveSource) return

        mxPlayer?.pause()
        provider.setPlayState(MXState.PAUSE)
    }

    /**
     * 暂停播放后，继续播放
     */
    open fun continuePlay() {
        if (provider.mState != MXState.PAUSE) return
        mxPlayer?.start()
        provider.setPlayState(MXState.PLAYING)
    }

    /**
     * 开始构建播放流程，在预加载完成后不立即播放
     */
    open fun startPreload() {
        stopPlay()
        config.isPreloading = false
        val source = config.source ?: return
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

        config.isPreloading = true
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
        playingVideo?.stopPlay()
        val clazz = mxPlayerClass ?: MXSystemPlayer::class.java
        val source = config.source ?: return
        MXUtils.log("startVideo ${source.playUri} player=${clazz.name}")

        val startRun = {
            val constructor = clazz.getConstructor()
            val player = (constructor.newInstance() as IMXPlayer)
            player.setSource(source)
            val textureView = addTextureView(player)
            player.setMXVideo(this, textureView)
            mxPlayer = player
            playingVideo = this
            MXUtils.findWindows(context)?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            provider.setPlayState(MXState.PREPARING)
            sensorHelp.addListener(sensorListener)
        }
        if (!MXUtils.isWifiConnected(context) && config.showTipIfNotWifi && !hasWifiDialogShow) {
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
        MXUtils.log("addTextureView")
        provider.mxSurfaceContainer.removeAllViews()
        val textureView = MXTextureView(context.applicationContext)
        textureView.setVideoSize(config.videoWidth, config.videoHeight)
        textureView.setDisplayType(config.scale)
        textureView.setOrientation(config.orientation)

        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER

        provider.mxSurfaceContainer.addView(textureView, layoutParams)
        textureView.surfaceTextureListener = player
        this.textureView = textureView
        return textureView
    }

    /**
     * 视频已经准备好,但不是已经开始播放!
     */
    open fun onPlayerPrepared() {
        MXUtils.log("onPlayerPrepared")
        val player = mxPlayer ?: return

        provider.setPlayState(MXState.PREPARED)
        if (config.isPreloading) {
            config.isPreloading = false
        } else {
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
            player.seekTo(seekTo)
        }
        config.seekWhenPlay = -1
    }

    /**
     * 获取需要跳转的位置
     */
    private fun getSeekPosition(): Int {
        val source = config.source ?: return -1
        if (config.seekWhenPlay >= 0) { // 注意：这里seekWhenPlay=0时需要默认从0开始播放
            return config.seekWhenPlay
        }
        if (source.enableSaveProgress) {
            val seekTo = MXUtils.getProgress(context, source.playUri)
            if (seekTo > 0) return seekTo
        }
        return -1
    }

    /**
     * 视频正式开始播放
     */
    open fun onPlayerStartPlay() {
        MXUtils.log("onPlayerStartPlay")
        provider.setPlayState(MXState.PLAYING)
    }

    /**
     * 视频播放完成
     */
    open fun onPlayerCompletion() {
        MXUtils.log("onPlayerCompletion")
        config.source?.playUri?.let { MXUtils.saveProgress(context, it, 0) }
        mxPlayer?.release()
        MXUtils.findWindows(context)?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        provider.setPlayState(MXState.COMPLETE)

        if (config.gotoNormalScreenWhenComplete && provider.mScreen == MXScreen.FULL) {
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
        MXUtils.log("onPlayerError  $error")
        if (config.source?.isLiveSource == true
            && config.replayLiveSourceWhenError
            && provider.mState in arrayOf(MXState.PLAYING, MXState.PAUSE, MXState.PREPARING)
        ) {
            // 直播重试
            startPlay()
            return
        }

        if (config.isPreloading && provider.mState == MXState.PREPARING) {
            // 预加载失败，状态重置成NORMAL
            config.isPreloading = false
            stopPlay()
            return
        }
        provider.setPlayState(MXState.ERROR)
        if (config.gotoNormalScreenWhenError && provider.mScreen == MXScreen.FULL) {
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
        MXUtils.log("onPlayerBuffering:$start")
        provider.setOnBuffering(start)

        post {
            config.videoListeners.toList().forEach { listener ->
                listener.onBuffering(start)
            }
        }
    }

    /**
     * 视频获得宽高
     */
    open fun onPlayerVideoSizeChanged(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (width == config.videoWidth && height == config.videoHeight) return

        MXUtils.log("onPlayerVideoSizeChanged $width x $height")
        config.videoWidth = width
        config.videoHeight = height
        textureView?.setVideoSize(width, height)
        postInvalidate()

        post {
            config.videoListeners.toList().forEach { listener ->
                listener.onVideoSizeChange(width, height)
            }
        }
    }

    /**
     * 结束播放
     */
    open fun stopPlay() {
        MXUtils.log("stopPlay")
        val player = mxPlayer
        textureView = null
        mxPlayer = null
        player?.release()
        if (playingVideo == this) {
            playingVideo = null
        }

        sensorHelp.deleteListener(sensorListener)

        if (config.source == null) {
            provider.setPlayState(MXState.IDLE)
        } else {
            provider.setPlayState(MXState.NORMAL)
        }
    }

    private var dimensionRatio: Double = 0.0

    /**
     * 设置MXVideo  ViewGroup的宽高比，设置之后会自动计算播放器的高度
     */
    open fun setDimensionRatio(ratio: Double) {
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
            && provider.mScreen == MXScreen.NORMAL
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

        if (config.videoWidth > 0 && config.videoHeight > 0
            && provider.mScreen == MXScreen.NORMAL
            && widthMode == MeasureSpec.EXACTLY
            && heightMode != MeasureSpec.EXACTLY
        ) {
            var height = (widthSize * config.videoHeight.toFloat() / config.videoWidth).toInt()
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
        provider.setViewSize(w, h)
    }

    /**
     * 切换全屏、小屏显示
     */
    private fun switchToScreen(screen: MXScreen) {
        val windows = MXUtils.findWindowsDecorView(context) ?: return
        if (provider.mScreen == screen) return
        when (screen) {
            MXScreen.FULL -> {
                if (parentMap.containsKey(config.viewIndexId)) {
                    return
                }
                val parent = (parent as ViewGroup?) ?: return
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
                provider.setScreenState(MXScreen.FULL)
            }
            MXScreen.NORMAL -> {
                val parentItem = parentMap.remove(config.viewIndexId) ?: return
                windows.removeView(this)
                parentItem.parentViewGroup.removeViewAt(parentItem.index)
                parentItem.parentViewGroup.addView(this, parentItem.index, parentItem.layoutParams)

                MXUtils.recoverScreenOrientation(context)
                MXUtils.recoverFullScreen(context)
                provider.setScreenState(MXScreen.NORMAL)
            }
        }
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
        val player = mxPlayer ?: return false
        return (provider.mState in arrayOf(
            MXState.PLAYING,
            MXState.PAUSE,
            MXState.PREPARING,
            MXState.PREPARED
        )) && player.isPlaying()
    }

    /**
     * 判断是否全屏
     */
    open fun isFullScreen(): Boolean {
        return provider.mScreen == MXScreen.FULL
    }

    /**
     * 切换小屏播放
     */
    open fun gotoNormalScreen() {
        switchToScreen(MXScreen.NORMAL)
    }

    /**
     * 切换全屏播放
     */
    open fun gotoFullScreen() {
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
        stopPlay()
        mxPlayerClass = null
        mxPlayer = null
        config.reset()
        provider.setPlayState(MXState.IDLE)
        postInvalidate()
    }

    private val sensorListener = object : MXSensorListener {
        override fun onChange(orientation: MXOrientation) {
            if (!isPlaying() || !config.willChangeOrientationWhenFullScreen()) {
                // 当不在播放，或者不需要变更方向时，不处理
                return
            }
            if (!config.autoRotateBySensor) {
                if (provider.mScreen == MXScreen.FULL && orientation.isHorizontal()) {
                    // 全屏时，方向切换，变更一下
                    MXUtils.setScreenOrientation(context, orientation)
                }
                return
            }
            if (orientation.isHorizontal()) {
                // 竖屏切换到横屏
                if (provider.mScreen == MXScreen.FULL) {
                    MXUtils.setScreenOrientation(context, orientation)
                } else {
                    switchToScreen(MXScreen.FULL)
                }
                return
            }
            if (orientation.isVertical()) {
                // 横屏切换到竖屏
                if (provider.mScreen == MXScreen.NORMAL) {
                    MXUtils.setScreenOrientation(context, orientation)
                } else {
                    switchToScreen(MXScreen.NORMAL)
                }
                return
            }
        }

    }

    /**
     * 销毁Activity或Fragment时调用
     * 销毁后，不能再次进行播放操作
     */
    open fun release() {
        config.release()
        provider.release()
        sensorHelp.deleteListener(sensorListener)
        stopPlay()
    }
}