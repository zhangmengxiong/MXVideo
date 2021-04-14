package com.mx.video

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
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
        var mContext: Context? = null
        fun getAppContext() = mContext!!

        private var playingVideo: MXVideo? = null
        fun isFullScreen(): Boolean {
            return playingVideo?.isFullScreen() == true
        }

        fun gotoNormalScreen() {
            playingVideo?.gotoNormalScreen()
        }

        fun gotoFullScreen() {
            playingVideo?.gotoFullScreen()
        }

        fun releaseAll() {
            playingVideo?.release()
        }
    }

    init {
        mContext = context.applicationContext
    }

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

    init {
        View.inflate(context, getLayoutId(), this)

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
     * 获取播放器
     */
    fun getPlayer() = mxPlayer


    /**
     * 设置播放数据源
     * @param source 播放源
     * @param clazz 播放器
     * @param seekTo 跳转
     */
    fun setSource(source: MXPlaySource, clazz: Class<out IMXPlayer>? = null, seekTo: Int = -1) {
        stopPlay()
        config.source = source
        mxPlayerClass = clazz

        config.seekWhenPlay = seekTo
        provider.mxTitleTxv.text = source.title
        provider.setPlayState(MXState.NORMAL)
    }

    fun setTextureViewRotation(rotation: Int) {
        config.rotation = rotation
        textureView?.rotation = rotation.toFloat()
    }

    /**
     * 跳转
     */
    fun seekTo(seek: Int) {
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
    fun setScaleType(type: MXScale) {
        config.scale = type
        textureView?.setDisplayType(type)
    }

    /**
     * 开始构建播放流程，预加载完成后立即播放
     */
    fun startPlay() {
        stopPlay()
        config.isPreloading = false
        startVideo()
    }

    /**
     * 开始构建播放流程，在预加载完成后不立即播放
     */
    fun startPreload() {
        stopPlay()
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
        }
        if (!MXUtils.isWifiConnected(context) && config.showTipIfNotWifi && !hasWifiDialogShow) {
            AlertDialog.Builder(context).apply {
                setMessage(R.string.mx_play_wifi_notify)
                setPositiveButton(context.getString(R.string.mx_play_wifi_dialog_continue)) { _, _ ->
                    hasWifiDialogShow = true
                    startRun.invoke()
                }
                setNegativeButton(context.getString(R.string.mx_play_wifi_dialog_cancel)) { _, _ ->
                    hasWifiDialogShow = true
                }
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
        textureView.rotation = config.rotation.toFloat()

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
    fun onPlayerPrepared() {
        MXUtils.log("onPlayerPrepared")
        val player = mxPlayer ?: return

        if (config.isPreloading) {
            config.isPreloading = false
            provider.setPlayState(MXState.PREPARED)
        } else {
            player.start()
            seekBeforePlay()
        }
    }

    /**
     * 播放前跳转
     * 必须在player.start()调用之后再使用
     */
    fun seekBeforePlay() {
        val player = mxPlayer ?: return
        val source = config.source ?: return
        if (config.seekWhenPlay >= 0) {
            if (config.seekWhenPlay > 0) {
                // 0 = 是默认重头开始
                // > 0 需要seekTo
                player.seekTo(config.seekWhenPlay)
            }
            config.seekWhenPlay = -1
        } else if (source.enableSaveProgress) {
            val seekTo = MXUtils.getProgress(context, source.playUri)
            if (seekTo > 0) {
                player.seekTo(seekTo)
            }
        }
    }

    /**
     * 视频正式开始播放
     */
    fun onPlayerStartPlay() {
        MXUtils.log("onPlayerStartPlay")
        provider.setPlayState(MXState.PLAYING)
    }

    /**
     * 视频播放完成
     */
    fun onPlayerCompletion() {
        MXUtils.log("onPlayerCompletion")
        config.source?.playUri?.let { MXUtils.saveProgress(context, it, 0) }
        if (config.gotoNormalScreenWhenComplete && provider.mScreen == MXScreen.FULL) {
            gotoNormalScreen()
        }
        mxPlayer?.release()
        MXUtils.findWindows(context)?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        provider.setPlayState(MXState.COMPLETE)
    }

    /**
     * 视频缓冲进度
     * @param 0-100
     */
    fun onPlayerBufferProgress(percent: Int) {
//        MXUtils.log("onPlayerBufferProgress:$percent")
    }

    /**
     * 视频快进完成
     */
    fun onPlayerSeekComplete() {
    }

    /**
     * 视频播放错误信息
     */
    fun onPlayerError(error: String?) {
        MXUtils.log("onPlayerError  $error")
        if (config.isPreloading && provider.mState == MXState.PREPARING) {
            // 预加载失败，状态重置成NORMAL
            config.isPreloading = false
            stopPlay()
            return
        }
        provider.setPlayState(MXState.ERROR)
        if (config.gotoNormalScreenWhenError && provider.mScreen == MXScreen.FULL) {
            gotoNormalScreen()
        }
    }

    /**
     * 视频缓冲状态变更
     * @param start
     *  true = 开始缓冲
     *  false = 结束缓冲
     */
    fun onPlayerBuffering(start: Boolean) {
        MXUtils.log("onPlayerBuffering:$start")
        provider.setOnBuffering(start)

        config.videoListeners.toList().forEach { listener ->
            listener.onBuffering(start)
        }
    }

    /**
     * 视频获得宽高
     */
    fun onPlayerVideoSizeChanged(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (width == config.videoWidth && height == config.videoHeight) return

        MXUtils.log("onPlayerVideoSizeChanged $width x $height")
        config.videoWidth = width
        config.videoHeight = height
        textureView?.setVideoSize(width, height)
        postInvalidate()

        config.videoListeners.toList().forEach { listener ->
            listener.onVideoSizeChange(width, height)
        }
    }

    /**
     * 结束播放
     */
    fun stopPlay() {
        MXUtils.log("stopPlay")
        val player = mxPlayer
        textureView = null
        mxPlayer = null
        player?.release()
        if (playingVideo == this) {
            playingVideo = null
        }
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
    fun setDimensionRatio(ratio: Double) {
        if (ratio != dimensionRatio) {
            this.dimensionRatio = ratio
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        if (dimensionRatio > 0.0
            && provider.mScreen == MXScreen.NORMAL
            && widthMode == MeasureSpec.EXACTLY
        ) {
            // 当外部设置固定宽高比，且非全屏时，调整测量高度
            val measureSpec = MeasureSpec.makeMeasureSpec(
                (widthSize / dimensionRatio).toInt(),
                MeasureSpec.EXACTLY
            )
            super.onMeasure(widthMeasureSpec, measureSpec)
            return
        }

        if (config.videoWidth > 0 && config.videoHeight > 0
            && provider.mScreen == MXScreen.NORMAL
            && widthMode == MeasureSpec.EXACTLY
            && heightMode != MeasureSpec.EXACTLY
        ) {
            //  当视频宽高有数据，，且非全屏时，按照视频宽高比调整整个View的高度，默认视频宽高比= 1280 x 720
            val measureSpec = MeasureSpec.makeMeasureSpec(
                (widthSize * config.videoHeight.toFloat() / config.videoWidth).toInt(),
                MeasureSpec.EXACTLY
            )
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
        if (!config.canFullScreen) return
        val windows = MXUtils.findWindowsDecorView(context) ?: return
        if (provider.mScreen == screen) return
        val willChangeOrientation = (config.source?.canChangeOrientationIfFullScreen == true
                || config.videoWidth > config.videoHeight)
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

                MXUtils.setFullScreen(context, willChangeOrientation)
                provider.setScreenState(MXScreen.FULL)
            }
            MXScreen.NORMAL -> {
                val parentItem = parentMap.remove(config.viewIndexId) ?: return
                windows.removeView(this)
                parentItem.parentViewGroup.removeViewAt(parentItem.index)
                parentItem.parentViewGroup.addView(this, parentItem.index, parentItem.layoutParams)

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
    fun isPlaying(): Boolean {
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
    fun isFullScreen(): Boolean {
        return provider.mScreen == MXScreen.FULL
    }

    /**
     * 切换小屏播放
     */
    fun gotoNormalScreen() {
        switchToScreen(MXScreen.NORMAL)
    }

    /**
     * 切换全屏播放
     */
    fun gotoFullScreen() {
        switchToScreen(MXScreen.FULL)
    }

    /**
     * 获取总时长
     */
    fun getDuration(): Int {
        return mxPlayer?.getDuration() ?: 0
    }

    /**
     * 获取当前播放时长
     */
    fun getCurrentPosition(): Int {
        return mxPlayer?.getCurrentPosition() ?: 0
    }

    /**
     * 重置播放器为 @link{MXState.IDLE} 状态
     */
    fun reset() {
        stopPlay()
        mxPlayerClass = null
        mxPlayer = null
        config.reset()
        provider.setPlayState(MXState.IDLE)
        postInvalidate()
    }


    /**
     * 销毁Activity或Fragment时调用
     * 销毁后，不能再次进行播放操作
     */
    fun release() {
        config.release()
        provider.release()
        stopPlay()
    }
}