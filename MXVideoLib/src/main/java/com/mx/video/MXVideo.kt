package com.mx.video

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.mx.video.base.IMXVideo
import com.mx.video.beans.*
import com.mx.video.listener.MXSensorListener
import com.mx.video.listener.MXVideoListener
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import com.mx.video.utils.MXSensorHelp
import com.mx.video.utils.MXUtils
import com.mx.video.views.MXTextureView
import com.mx.video.views.MXViewProvider
import com.mx.video.views.MXViewSet

abstract class MXVideo @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IMXVideo {
    companion object {
        fun init(context: Context) {
            MXUtils.init(context)
        }

        private var hasWifiDialogShow = false
        private val parentMap = HashMap<Int, MXParentView>()

        private var PLAYING_VIDEO: IMXVideo? = null
        fun isFullScreen(): Boolean {
            return PLAYING_VIDEO?.currentScreen() == MXScreen.FULL
        }

        fun gotoNormalScreen() {
            PLAYING_VIDEO?.switchToScreen(MXScreen.NORMAL)
        }

        /**
         * 当前播放设为全屏
         */
        fun gotoFullScreen() {
            PLAYING_VIDEO?.switchToScreen(MXScreen.FULL)
        }

        /**
         * 停止当前播放
         */
        fun stopAll() {
            PLAYING_VIDEO?.stopPlay()
        }

        /**
         * 释放当前播放器
         */
        fun releaseAll() {
            PLAYING_VIDEO?.release()
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
    private var mxPlayerClass: Class<out IMXPlayer> = MXSystemPlayer::class.java

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
     * 所有View的集合
     */
    private val viewSet by lazy { MXViewSet(this, config) }

    /**
     * 视图处理器
     */
    private val provider by lazy { MXViewProvider(viewSet, this, config) }

    /**
     * 设备旋转感应器
     */
    private val sensorHelp by lazy { MXSensorHelp.instance }

    /**
     * 播放器 暂停状态
     */
    private var isStopState: Boolean? = null

    init {
        MXUtils.init(context)
        View.inflate(context, getLayoutId(), this)
        setBackgroundColor(Color.BLACK)
        isFocusable = false
        isFocusableInTouchMode = false
        provider.initView()
        config.state.set(MXState.IDLE)
        config.canFullScreen.addObserver { can ->
            if (!can && config.screen.get() == MXScreen.FULL) {
                switchToScreen(MXScreen.NORMAL)
            }
        }
        config.audioMute.addObserver { mute ->
            val player = mxPlayer ?: return@addObserver
            if (mute) {
                player.setVolume(0f, 0f)
            } else {
                player.setVolume(1f, 1f)
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
    fun getPosterImageView() = viewSet.mxPlaceImg

    /**
     * 获取Config
     */
    fun getConfig() = config

    /**
     * 获取状态值
     */
    fun getState() = config.state.get()

    /**
     * player 播放器类
     */
    override fun setPlayer(player: Class<out IMXPlayer>?) {
        mxPlayerClass = player ?: MXSystemPlayer::class.java
    }

    /**
     * 设置播放数据源
     * @param source 播放源
     * @param seekTo 跳转 >=0 时播放后会跳转到对应时间，单位：秒
     */
    override fun setSource(source: MXPlaySource?, seekTo: Int) {
        stopPlay()

        MXUtils.log("MXVideo: setSource()")
        isStopState = null
        config.source.set(source)
        config.seekWhenPlay.set(seekTo)
        viewSet.mxTitleTxv.text = source?.title
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
    override fun setTextureOrientation(orientation: MXOrientation) {
        MXUtils.log("MXVideo: setTextureOrientation()")
        config.orientation.set(orientation)
        requestLayout()
    }

    /**
     * 跳转
     */
    override fun seekTo(seek: Int) {
        MXUtils.log("MXVideo: seekTo() ${MXUtils.stringForTime(seek)}")
        val player = mxPlayer
        if (player != null && config.state.get() in arrayOf(MXState.PLAYING, MXState.PAUSE)) {
            player.seekTo(seek)
            viewSet.processLoading()
        } else {
            config.seekWhenPlay.set(seek)
        }
    }

    override fun setScaleType(type: MXScale) {
        MXUtils.log("MXVideo: setScaleType(${type.name})")
        config.scale.set(type)
    }

    override fun setDimensionRatio(ratio: Double) {
        MXUtils.log("MXVideo: setDimensionRatio($ratio)")
        config.dimensionRatio.set(ratio)
        requestLayout()
    }

    override fun setAudioMute(mute: Boolean) {
        MXUtils.log("MXVideo: setAudioMute($mute)")
        config.audioMute.set(mute)
    }

    override fun startPlay() {
        MXUtils.log("MXVideo: startPlay()")
        stopPlay()
        config.isPreloading.set(false)
        startVideo()
    }

    override fun startPreload() {
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

        val player = createPlayer()
        if (!player.enablePreload()) {
            // 不支持预加载
            return
        }

        config.isPreloading.set(true)
        startVideo()
    }

    override fun stopPlay() {
        val player = mxPlayer

        mxTextureView?.release()
        mxTextureView = null

        mxPlayer = null
        isStopState = null

        if (player != null) {
            MXUtils.log("MXVideo: stopPlay()")
            player.release()
        }

        viewSet.mxSurfaceContainer.removeAllViews()

        if (PLAYING_VIDEO == this) {
            PLAYING_VIDEO = null
        }

        sensorHelp.deleteListener(sensorListener)

        if (config.source.get() == null) {
            config.state.set(MXState.IDLE)
        } else {
            config.state.set(MXState.NORMAL)
        }
        config.loading.set(false)
    }

    override fun pausePlay() {
        if (config.state.get() != MXState.PLAYING) return
        if (!config.canPauseByUser.get()) return
        val source = config.source.get() ?: return
        if (source.isLiveSource) return

        MXUtils.log("MXVideo: pausePlay()")
        mxPlayer?.pause()
        config.state.set(MXState.PAUSE)
    }

    override fun continuePlay() {
        if (config.state.get() !in arrayOf(MXState.PAUSE, MXState.PREPARED)) return
        MXUtils.log("MXVideo: continuePlay()")
        mxPlayer?.start()
        config.state.set(MXState.PLAYING)
    }

    override fun isPlaying(): Boolean {
        mxPlayer ?: return false
        return (config.state.get() in arrayOf(
            MXState.PLAYING, MXState.PAUSE,
            MXState.PREPARING, MXState.PREPARED
        ))
    }

    override fun getDuration(): Int {
        return mxPlayer?.getDuration() ?: 0
    }

    override fun getPosition(): Int {
        return mxPlayer?.getPosition() ?: 0
    }

    override fun switchToScreen(screen: MXScreen): Boolean {
        MXUtils.findWindowsDecorView(context) ?: return false
        if (!config.canFullScreen.get() && screen == MXScreen.FULL) {
            return false
        }
        MXUtils.log("MXVideo: switchToScreen()  ${config.screen.get().name} -> ${screen.name}")
        config.screen.set(screen)
        return true
    }

    override fun currentScreen(): MXScreen {
        return config.screen.get()
    }

    override fun onPlayerPrepared() {
        val player = mxPlayer ?: return

        config.audioMute.notifyChange()
        config.state.set(MXState.PREPARED)
        if (config.isPreloading.get()) {
            MXUtils.log("MXVideo: onPlayerPrepared -> need click start button to play")
        } else {
            MXUtils.log("MXVideo: onPlayerPrepared -> start play")
            player.start()
            seekToWhenPlay()
        }
    }

    override fun onPlayerStartPlay() {
        MXUtils.log("MXVideo: onPlayerStartPlay()")
        if (config.state.get() in arrayOf(MXState.PREPARED, MXState.PREPARING)) {
            config.state.set(MXState.PLAYING)
        }
    }

    override fun onPlayerCompletion() {
        val source = config.source.get() ?: return
        MXUtils.log("MXVideo: onPlayerCompletion()")
        source.playUri.let { MXUtils.saveProgress(it, 0) }
        mxPlayer?.release()
        mxPlayer = null
        if (source.isLooping) {
            startVideo()
        } else {
            config.state.set(MXState.COMPLETE)

            if (config.gotoNormalScreenWhenComplete.get() && config.screen.get() == MXScreen.FULL) {
                switchToScreen(MXScreen.NORMAL)
            }
        }
    }

    override fun onPlayerBufferProgress(percent: Int) {
    }

    override fun onPlayerSeekComplete() {
    }

    override fun onPlayerError(error: String?) {
        if (config.source.get()?.isLiveSource == true
            && config.replayLiveSourceWhenError.get()
            && (config.state.get() in arrayOf(
                MXState.PLAYING,
                MXState.PAUSE,
                MXState.PREPARING,
                MXState.PREPARED
            ))
        ) {
            MXUtils.log("MXVideo: onPlayerError() ---> 直播重试")
            // 直播重试
            startPlay()
            return
        }

        if (config.isPreloading.get() && config.state.get() == MXState.PREPARING) {
            // 预加载失败，状态重置成NORMAL
            config.isPreloading.set(false)
            stopPlay()

            MXUtils.log("MXVideo: onPlayerError() ---> 预加载失败，状态重置成NORMAL")
            return
        }
        MXUtils.log("MXVideo: onPlayerError($error)")

        config.state.set(MXState.ERROR)
        if (config.gotoNormalScreenWhenError.get() && config.screen.get() == MXScreen.FULL) {
            switchToScreen(MXScreen.NORMAL)
        }
    }

    override fun onPlayerBuffering(start: Boolean) {
        MXUtils.log("MXVideo: onPlayerBuffering() $start")
        config.loading.set(start)
    }

    override fun onPlayerVideoSizeChanged(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val size = config.videoSize.get()
        if (width == size.width && height == size.height) return

        MXUtils.log("MXVideo: onPlayerVideoSizeChanged() $width x $height")
        config.videoSize.set(MXSize(width, height))
        postInvalidate()
    }

    override fun onPlayerInfo(message: String?) {
        MXUtils.log("MXVideo: onPlayerInfo($message)")
    }

    override fun onStart() {
        if (isStopState != true) return
        val source = config.source.get() ?: return
        MXUtils.log("MXVideo: onStart()")
        if (source.isLiveSource) {
            startPlay()
        } else {
            if (config.state.get() == MXState.PAUSE) {
                continuePlay()
            }
        }
        isStopState = null
    }

    override fun onStop() {
        if (config.state.get() != MXState.PLAYING) return
        val source = config.source.get() ?: return
        MXUtils.log("MXVideo: onStop()")
        if (source.isLiveSource) {// 直播无法暂停，这里直接停止播放
            stopPlay()
        } else {
            pausePlay()
        }
        isStopState = true
    }

    private fun createPlayer(): IMXPlayer {
        val constructor = mxPlayerClass.getConstructor()
        return (constructor.newInstance() as IMXPlayer)
    }

    /**
     * 开始构建播放流程
     * 1：释放播放资源
     * 2：判断播放地址、播放器
     * 3：判断WiFi状态
     * 4：创建播放器，开始播放
     */
    private fun startVideo() {
        PLAYING_VIDEO?.stopPlay()
        val source = config.source.get() ?: return
        MXUtils.log("startVideo $source")
        MXUtils.log("startVideo player=${mxPlayerClass.name}")

        val startRun = {
            val player = createPlayer()
            val textureView = viewSet.attachTextureView()
            player.startPlay(context, this, source, textureView)

            mxPlayer = player
            PLAYING_VIDEO = this
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        config.playerViewSize.set(MXSize(w, h))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var ratio = config.dimensionRatio.get()
        if (ratio <= 0.0) {
            val size = config.videoSize.get()
            if (size.width > 0 && size.height > 0) {
                ratio = if (config.orientation.get().isVertical()) {
                    size.width.toDouble() / size.height.toDouble()
                } else {
                    size.height.toDouble() / size.width.toDouble()
                }
            }
        }

        if (ratio <= 0.0 && widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST) {
            // 高度自适应、且没有宽高比，设置宽高比为 16：9
            ratio = 16.0 / 9.0
        }

//        MXUtils.log("MXVideo: onMeasure($widthMode,$heightMode,$widthSize,$heightSize) $ratio")

        if (config.screen.get() == MXScreen.NORMAL) {
            var height = (widthSize / ratio).toInt()
            if (height > heightSize) {
                height = heightSize
            }

            // 当外部设置固定宽高比，且非全屏时，调整测量高度
            val measureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, measureSpec)
            return
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun cloneMeToLayout(target: MXParentView) {
        try {
            val constructor = this::class.java.getConstructor(Context::class.java)
            val selfClone = constructor.newInstance(context)
            selfClone.id = this.id
            selfClone.mxPlayerClass = mxPlayerClass
            selfClone.config.cloneBy(config)

            selfClone.minimumWidth = target.width
            selfClone.minimumHeight = target.height
            target.parentViewGroup.addView(selfClone, target.index, target.layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 重置播放器为 @link{MXState.IDLE} 状态
     */
    open fun reset() {
        MXUtils.log("MXVideo: reset()")
        stopPlay()
        mxPlayerClass = MXSystemPlayer::class.java
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
     * 销毁Activity或Fragment时调用
     * 销毁后，不能再次进行播放操作
     */
    override fun release() {
        MXUtils.log("MXVideo: release()")
        viewSet.release()
        config.release()
        provider.release()
        sensorHelp.deleteListener(sensorListener)
        parentMap.remove(config.viewIndexId)
        stopPlay()
    }
}