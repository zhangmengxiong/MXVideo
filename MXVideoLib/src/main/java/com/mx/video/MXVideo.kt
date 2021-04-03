package com.mx.video

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import com.mx.video.utils.MXUtils
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
            playingVideo?.stopPlay()
        }
    }

    init {
        mContext = context.applicationContext
    }

    abstract fun getLayoutId(): Int

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
    private val viewProvider by lazy { MXViewProvider(this, mxConfig) }

    init {
        View.inflate(context, getLayoutId(), this)

        viewProvider.initView()
        viewProvider.setState(MXState.IDLE)
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
        clazz: Class<IMXPlayer>? = null,
        start: Boolean = true
    ) {
        stopPlay()
        currentSource = source
        mxPlayerClass = clazz ?: MXSystemPlayer::class.java

        viewProvider.mxTitleTxv.text = source.title
        viewProvider.setState(MXState.IDLE)
        if (start) {
            startVideo()
        }
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
        MXUtils.log("startVideo ${currentSource?.playUrl}")
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

        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER

        viewProvider.mxSurfaceContainer.addView(textureView, layoutParams)
        textureView.surfaceTextureListener = player
        this.textureView = textureView
        return textureView
    }

    fun onPrepared() {
        MXUtils.log("onPrepared")
        val player = mxPlayer ?: return
        viewProvider.setState(MXState.PREPARED)
        player.start()
        viewProvider.setState(MXState.PLAYING)
        if (seekWhenPlay > 0) {
            player.seekTo(seekWhenPlay)
            seekWhenPlay = 0
        }
    }

    fun onCompletion() {
        MXUtils.log("onCompletion")
        viewProvider.setState(MXState.COMPLETE)
        if (mxConfig.gotoNormalScreenWhenComplete && viewProvider.mScreen == MXScreen.FULL) {
            gotoNormalScreen()
        }
        mxPlayer?.release()
        MXUtils.findWindows(context)?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Runtime.getRuntime().gc()
    }

    /**
     * @param 0-100
     */
    fun setBufferProgress(percent: Int) {
//        MXUtils.log("setBufferProgress:$percent")
    }

    fun onSeekComplete() {
        MXUtils.log("onSeekComplete")
    }

    /**
     * 错误信息
     */
    fun onError(error: String?) {
        MXUtils.log("onError  $error")
        viewProvider.setState(MXState.ERROR)
        if (mxConfig.gotoNormalScreenWhenError && viewProvider.mScreen == MXScreen.FULL) {
            gotoNormalScreen()
        }
    }

    /**
     * 缓冲状态变更
     * @param start
     *  true = 开始缓冲
     *  false = 结束缓冲
     */
    fun onBuffering(start: Boolean) {
        MXUtils.log("onBuffering:$start")
        viewProvider.mxLoading.visibility = if (start) View.VISIBLE else View.GONE
    }

    /**
     * 获得视频宽高
     */
    fun onVideoSizeChanged(width: Int, height: Int) {
        MXUtils.log("onVideoSizeChanged $width x $height")
        mVideoWidth = width
        mVideoHeight = height
        textureView?.setVideoSize(width, height)
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
        viewProvider.setState(MXState.IDLE)
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
        if (dimensionRatio > 0.0 && viewProvider.mScreen == MXScreen.NORMAL
            && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY
        ) {
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val measureSpec = MeasureSpec.makeMeasureSpec(
                (widthSize / dimensionRatio).toInt(),
                MeasureSpec.EXACTLY
            )
            super.onMeasure(widthMeasureSpec, measureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewProvider.touchHelp.setSize(w, h)
    }

    /**
     * 切换全屏、小屏显示
     */
    private fun switchToScreen(screen: MXScreen) {
        val windows = MXUtils.findWindowsDecorView(context) ?: return
        if (viewProvider.mScreen == screen) return
        when (screen) {
            MXScreen.FULL -> {
                viewProvider.mxFullscreenBtn.setImageResource(R.drawable.mx_icon_small_screen)
                if (parentMap.containsKey(viewIndexId)) {
                    return
                }
                val parent = (parent as ViewGroup?) ?: return
                val index = parent.indexOfChild(this)
                val layoutParams = layoutParams
                parent.removeView(this)
                parentMap[viewIndexId] = MXParentView(index, parent, layoutParams, width, height)

                val fullLayout = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                windows.addView(this, fullLayout)
                viewProvider.mScreen = MXScreen.FULL
                viewProvider.mxReturnBtn.visibility = View.VISIBLE
                MXUtils.setFullScreen(context)
            }
            MXScreen.NORMAL -> {
                viewProvider.mxFullscreenBtn.setImageResource(R.drawable.mx_icon_full_screen)
                val parentItem = parentMap.remove(viewIndexId) ?: return
                windows.removeView(this)
                parentItem.parentViewGroup.addView(this, parentItem.index, parentItem.layoutParams)
                requestLayout()

                viewProvider.mScreen = MXScreen.NORMAL
                viewProvider.mxReturnBtn.visibility = View.GONE
                MXUtils.recoverFullScreen(context)
            }
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

}