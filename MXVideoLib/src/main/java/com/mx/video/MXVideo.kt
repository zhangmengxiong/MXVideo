package com.mx.video

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import com.mx.video.utils.MXUtils
import com.mx.video.utils.MXVideoListener
import com.mx.video.views.MXTextureView
import com.mx.video.views.MXViewProvider
import java.util.concurrent.atomic.AtomicInteger

abstract class MXVideo @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private var hasWifiDialogShow = false
        private val videoViewIndex = AtomicInteger(1)
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

    private var mRotation: Int = 0
    private var mVideoWidth: Int = 1280
    private var mVideoHeight: Int = 720
    private val viewIndexId = videoViewIndex.incrementAndGet()

    private var currentSource: MXPlaySource? = null
    private var mxPlayerClass: Class<*>? = null
    private var mxPlayer: IMXPlayer? = null
    private var textureView: MXTextureView? = null // 当前TextureView
    private var displayType: MXScale = MXScale.CENTER_CROP
    private var seekWhenPlay: Int = 0

    private val mxConfig = MXConfig()
    private val videoListeners = ArrayList<MXVideoListener>()
    private val viewProvider by lazy { MXViewProvider(this, videoListeners, mxConfig) }

    init {
        View.inflate(context, getLayoutId(), this)

        reset()
        viewProvider.initView()
        viewProvider.setState(MXState.IDLE)
    }

    fun addOnVideoListener(listener: MXVideoListener) {
        if (!videoListeners.contains(listener)) {
            videoListeners.add(listener)
        }
    }

    fun removeOnVideoListener(listener: MXVideoListener) {
        videoListeners.remove(listener)
    }

    /**
     * 获取占位图ImageView
     */
    fun getPosterImageView() = viewProvider.mxPlaceImg

    /**
     * 获取Config
     */
    fun getConfig() = mxConfig

    /**
     * 获取当前播放源
     */
    fun getSource() = currentSource

    /**
     * 获取播放器
     */
    fun getPlayer() = mxPlayer


    /**
     * 设置播放数据源
     * @param source 播放源
     * @param clazz 播放器
     * @param start 是否立即播放
     */
    fun setSource(
        source: MXPlaySource,
        clazz: Class<out IMXPlayer>? = null,
        seekTo: Int = 0,
        start: Boolean = true
    ) {
        stopPlay()
        currentSource = source
        mxPlayerClass = clazz ?: MXSystemPlayer::class.java

        seekWhenPlay = seekTo
        viewProvider.mxTitleTxv.text = source.title
        viewProvider.setState(MXState.NORMAL)
        if (start) {
            startVideo()
        }
    }

    fun setTextureViewRotation(rotation: Int) {
        mRotation = rotation
        textureView?.rotation = rotation.toFloat()
    }

    /**
     * 跳转
     */
    fun seekTo(seek: Int) {
        MXUtils.log("seekTo ${MXUtils.stringForTime(seek)}")
        val player = mxPlayer
        if (player != null && player.isPlaying()) {
            player.seekTo(seek)
        } else {
            seekWhenPlay = seek
        }
    }

    /**
     * 设置缩放方式
     * MXScale.FILL_PARENT  当父容器宽高一定时，填满宽高
     * MXScale.CENTER_CROP  根据视频宽高自适应
     */
    fun setDisplayType(type: MXScale) {
        this.displayType = type
        viewProvider.mxPlaceImg.scaleType = when (type) {
            MXScale.CENTER_CROP -> ImageView.ScaleType.FIT_CENTER
            MXScale.FILL_PARENT -> ImageView.ScaleType.FIT_XY
        }
        textureView?.setDisplayType(type)
    }

    fun startPlay() {
        startVideo()
    }

    /**
     * 开始播放
     * 1：释放播放资源
     * 2：判断播放地址、播放器
     * 3：判断WiFi状态
     * 4：创建播放器，开始播放
     */
    private fun startVideo() {
        playingVideo?.stopPlay()
        stopPlay()
        MXUtils.log("startVideo ${currentSource?.playUri}")
        val clazz = mxPlayerClass ?: return
        val source = currentSource ?: return

        val startRun = {
            val constructor = clazz.getConstructor()
            val player = (constructor.newInstance() as IMXPlayer)
            player.setSource(source)
            val textureView = addTextureView(player)
            player.setMXVideo(this, textureView)
            mxPlayer = player
            playingVideo = this
            MXUtils.findWindows(context)?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            viewProvider.setState(MXState.PREPARING)
        }
        if (!MXUtils.isWifiConnected(context) && mxConfig.showTipIfNotWifi && !hasWifiDialogShow) {
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
        viewProvider.mxSurfaceContainer.removeAllViews()
        val textureView = MXTextureView(context.applicationContext)
        textureView.setVideoSize(mVideoWidth, mVideoHeight)
        textureView.setDisplayType(displayType)
        textureView.rotation = mRotation.toFloat()

        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER

        viewProvider.mxSurfaceContainer.addView(textureView, layoutParams)
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
        viewProvider.setState(MXState.PREPARED)
        player.start()

        if (seekWhenPlay > 0) {
            player.seekTo(seekWhenPlay)
            seekWhenPlay = 0
        }
    }

    /**
     * 视频正式开始播放
     */
    fun onPlayerStartPlay() {
        MXUtils.log("onPlayerStartPlay")
        viewProvider.setState(MXState.PLAYING)
    }

    /**
     * 视频播放完成
     */
    fun onPlayerCompletion() {
        MXUtils.log("onPlayerCompletion")
        viewProvider.setState(MXState.COMPLETE)
        if (mxConfig.gotoNormalScreenWhenComplete && viewProvider.mScreen == MXScreen.FULL) {
            gotoNormalScreen()
        }
        mxPlayer?.release()
        MXUtils.findWindows(context)?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Runtime.getRuntime().gc()
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
        MXUtils.log("onPlayerSeekComplete")
    }

    /**
     * 视频播放错误信息
     */
    fun onPlayerError(error: String?) {
        MXUtils.log("onPlayerError  $error")
        viewProvider.setState(MXState.ERROR)
        if (mxConfig.gotoNormalScreenWhenError && viewProvider.mScreen == MXScreen.FULL) {
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
        viewProvider.mxLoading.visibility = if (start) View.VISIBLE else View.GONE
    }

    /**
     * 视频获得宽高
     */
    fun onPlayerVideoSizeChanged(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        MXUtils.log("onPlayerVideoSizeChanged $width x $height")
        mVideoWidth = width
        mVideoHeight = height
        textureView?.setVideoSize(width, height)
        postInvalidate()
    }

    /**
     * 结束播放
     */
    fun stopPlay() {
        MXUtils.log("stopPlay")
//        viewProvider.mxSurfaceContainer.removeAllViews()
        val player = mxPlayer
        textureView = null
        mxPlayer = null
        player?.release()
        if (playingVideo == this) {
            playingVideo = null
        }
        if (currentSource == null) {
            viewProvider.setState(MXState.IDLE)
        } else {
            viewProvider.setState(MXState.NORMAL)
        }
    }

    /**
     * 销毁Activity或Fragment时调用
     */
    fun release() {
        MXUtils.log("release")
        stopPlay()
        videoListeners.clear()
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
            && viewProvider.mScreen == MXScreen.NORMAL
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

        if (mVideoWidth > 0 && mVideoHeight > 0
            && viewProvider.mScreen == MXScreen.NORMAL
            && widthMode == MeasureSpec.EXACTLY
            && heightMode != MeasureSpec.EXACTLY
        ) {
            //  当视频宽高有数据，，且非全屏时，按照视频宽高比调整整个View的高度，默认视频宽高比= 1280 x 720
            val measureSpec = MeasureSpec.makeMeasureSpec(
                (widthSize * mVideoHeight.toFloat() / mVideoWidth).toInt(),
                MeasureSpec.EXACTLY
            )
            super.onMeasure(widthMeasureSpec, measureSpec)
            return
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewProvider.touchHelp.setSize(w, h)
    }

    /**
     * 切换全屏、小屏显示
     */
    private fun switchToScreen(screen: MXScreen) {
        if (!mxConfig.canFullScreen) return
        val windows = MXUtils.findWindowsDecorView(context) ?: return
        if (viewProvider.mScreen == screen) return
        val willChangeOrientation = (currentSource?.canChangeOrientationIfFullScreen == true
                || mVideoWidth > mVideoHeight)
        when (screen) {
            MXScreen.FULL -> {
                viewProvider.mxFullscreenBtn.setImageResource(R.drawable.mx_icon_small_screen)
                if (parentMap.containsKey(viewIndexId)) {
                    return
                }
                val parent = (parent as ViewGroup?) ?: return
                val item = MXParentView(
                    parent.indexOfChild(this),
                    parent, layoutParams, width, height
                )
                parent.removeView(this)
                cloneMeToLayout(item)
                parentMap[viewIndexId] = item

                val fullLayout = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                windows.addView(this, fullLayout)
                viewProvider.mScreen = MXScreen.FULL
                viewProvider.mxReturnBtn.visibility = View.VISIBLE
                MXUtils.setFullScreen(context, willChangeOrientation)
            }
            MXScreen.NORMAL -> {
                viewProvider.mxFullscreenBtn.setImageResource(R.drawable.mx_icon_full_screen)
                val parentItem = parentMap.remove(viewIndexId) ?: return
                windows.removeView(this)
                parentItem.parentViewGroup.removeViewAt(parentItem.index)
                parentItem.parentViewGroup.addView(this, parentItem.index, parentItem.layoutParams)

                viewProvider.mScreen = MXScreen.NORMAL
                viewProvider.mxReturnBtn.visibility = View.GONE
                MXUtils.recoverFullScreen(context)
            }
        }
    }

    private fun cloneMeToLayout(target: MXParentView) {
        try {
            val constructor = this::class.java.getConstructor(Context::class.java)
            val selfClone = constructor.newInstance(context)
            selfClone.id = this.id
            selfClone.mVideoWidth = mVideoWidth
            selfClone.mVideoHeight = mVideoHeight
            selfClone.currentSource = currentSource?.clone()
            selfClone.displayType = displayType
            selfClone.mxPlayerClass = mxPlayerClass
            selfClone.mRotation = mRotation
            selfClone.dimensionRatio = dimensionRatio
            selfClone.mxConfig.cloneBy(mxConfig)

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
        return (viewProvider.mState in arrayOf(
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
        return viewProvider.mScreen == MXScreen.FULL
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
        mRotation = 0
        mVideoWidth = 1280
        mVideoHeight = 720
        currentSource = null
        mxPlayerClass = null
        mxPlayer = null
        seekWhenPlay = 0
        viewProvider.setState(MXState.IDLE)
        postInvalidate()
    }
}