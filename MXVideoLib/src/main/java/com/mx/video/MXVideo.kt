package com.mx.video

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import com.mx.video.utils.*
import com.mx.video.views.MXTextureView
import com.mx.video.views.MXViewProvider
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

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
            return playingVideo?.mScreen == MXScreen.FULL
        }

        fun gotoSmallScreen() {
            playingVideo?.gotoSmallScreen()
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

    private var mVideoWidth: Int = 1280
    private var mVideoHeight: Int = 720
    private val viewIndexId = videoViewIndex.incrementAndGet()

    /**
     * 播放状态
     */
    private var mState = MXState.IDLE
    private var mScreen = MXScreen.SMALL

    var currentSource: MXPlaySource? = null
    private var mxPlayerClass: Class<*>? = null
    private var mxPlayer: IMXPlayer? = null
    private var textureView: MXTextureView? = null // 当前TextureView
    private var displayType: MXScale = MXScale.CENTER_CROP
    private var seekWhenPlay: Int = 0


    private val mxConfig = MXConfig()
    private val timeTicket = MXTicket()
    private val timeDelay = MXDelay()
    private val touchHelp by lazy { MXTouchHelp(context, mxConfig) }
    private val viewProvider by lazy { MXViewProvider(this, mxConfig) }

    init {
        View.inflate(context, getLayoutId(), this)
        initView()
        setState(MXState.IDLE)
        setBackgroundColor(Color.GRAY)
    }

    fun getConfig() = mxConfig

    private fun initView() {
        viewProvider.mxPlayBtn.setOnClickListener {
            if (currentSource == null) {
                Toast.makeText(context, R.string.mx_play_source_not_set, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val player = mxPlayer
            if (mState == MXState.PLAYING) {
                if (player != null) {
                    player.pause()
                    setState(MXState.PAUSE)
                }
            } else if (mState == MXState.PAUSE) {
                if (player != null) {
                    player.start()
                    setState(MXState.PLAYING)
                }
            } else if (mState == MXState.IDLE) {
                startPlay()
            }
        }
        viewProvider.mxSurfaceContainer.setOnClickListener {
            if (mState in arrayOf(MXState.PLAYING, MXState.PAUSE)) {
                if (viewProvider.mxPlayBtn.isShown) {
                    viewProvider.mxPlayBtn.visibility = View.GONE
                    viewProvider.mxBottomLay.visibility = View.GONE
                    viewProvider.mxTopLay.visibility = View.GONE
                    timeDelay.stop()
                } else {
                    viewProvider.mxPlayBtn.visibility = View.VISIBLE
                    viewProvider.mxBottomLay.visibility = View.VISIBLE
                    viewProvider.mxTopLay.visibility = View.VISIBLE
                    timeDelay.start()
                }
            } else if (mScreen == MXScreen.FULL) {
                viewProvider.mxTopLay.visibility = View.VISIBLE
                timeDelay.start()
            }
        }
        viewProvider.mxSurfaceContainer.setOnTouchListener { view, motionEvent ->
            if (mScreen == MXScreen.FULL && mState == MXState.PLAYING) {
                // 全屏且正在播放才会触发触摸滑动
                touchHelp.onTouch(motionEvent)
            }
            return@setOnTouchListener false
        }

        touchHelp.setOnTouchAction {
            when (it) {
                MotionEvent.ACTION_DOWN -> {
                    viewProvider.mxPlaceImg.visibility = View.GONE
                    viewProvider.mxRetryLay.visibility = View.GONE
                    viewProvider.mxPlayBtn.visibility = View.GONE
                    viewProvider.mxLoading.visibility = View.GONE
                    viewProvider.mxBottomLay.visibility = View.GONE
                    viewProvider.mxTopLay.visibility = View.GONE
                    viewProvider.mxReplayLay.visibility = View.GONE
                    viewProvider.mxQuickSeekLay.visibility = View.VISIBLE
                }
                MotionEvent.ACTION_UP -> {
                    viewProvider.mxQuickSeekLay.visibility = View.GONE
                    timeDelay.start()
                }
            }
        }
        touchHelp.setHorizontalTouchCall { touchDownPercent, percent ->
            val duration = mxPlayer?.getDuration() ?: return@setHorizontalTouchCall
            val position = abs(duration * percent).toInt()
            viewProvider.mxQuickSeekImg.setImageResource(if (touchDownPercent > percent) R.drawable.mx_icon_seek_left else R.drawable.mx_icon_seek_right)
            viewProvider.mxQuickSeekTxv.text =
                MXUtils.stringForTime(position) + "/" + MXUtils.stringForTime(duration)
        }

        viewProvider.mxRetryLay.setOnClickListener {
            startVideo()
        }
        viewProvider.mxReplayLay.setOnClickListener {
            startVideo()
        }
        viewProvider.mxFullscreenBtn.setOnClickListener {
            if (mScreen == MXScreen.SMALL) {
                switchToScreen(MXScreen.FULL)
            } else {
                switchToScreen(MXScreen.SMALL)
            }
        }
        viewProvider.mxReturnBtn.setOnClickListener {
            if (mScreen == MXScreen.FULL) {
                switchToScreen(MXScreen.SMALL)
            }
        }
        timeDelay.setDelayRun(8000) {
            if (!isShown || mState != MXState.PLAYING) {
                return@setDelayRun
            }
            viewProvider.mxPlayBtn.visibility = View.GONE
            viewProvider.mxBottomLay.visibility = View.GONE
            viewProvider.mxTopLay.visibility = View.GONE
        }
        timeTicket.setTicketRun(300) {
            if (!isShown) return@setTicketRun
            val player = mxPlayer ?: return@setTicketRun
            if (mState in arrayOf(
                    MXState.PREPARED,
                    MXState.PREPARING,
                    MXState.PLAYING,
                    MXState.PAUSE
                ) && player.isPlaying()
            ) {
                val duration = player.getDuration()
                val position = player.getCurrentPosition()
                viewProvider.mxSeekProgress.max = duration
                viewProvider.mxSeekProgress.progress = position
                viewProvider.mxCurrentTimeTxv.text = MXUtils.stringForTime(position)
                viewProvider.mxTotalTimeTxv.text = MXUtils.stringForTime(duration)
            }
        }
    }

    private val onSeekBarListener = object : SeekBar.OnSeekBarChangeListener {
        var progress = 0
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                this.progress = progress
                viewProvider.mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            MXUtils.log("onStartTrackingTouch")
            this.progress = seekBar?.progress ?: return
            timeTicket.stop()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            MXUtils.log("onStopTrackingTouch")
            viewProvider.mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            seekTo(progress)
            timeTicket.start()
        }
    }

    fun setSource(
        source: MXPlaySource,
        clazz: Class<IMXPlayer>? = null,
        start: Boolean = true
    ) {
        stopPlay()
        currentSource = source
        mxPlayerClass = clazz ?: MXSystemPlayer::class.java

        viewProvider.mxTitleTxv.text = source.title
        setState(MXState.IDLE)
        if (start) {
            startVideo()
        }
    }

    private fun setState(state: MXState) {
        MXUtils.log("setState  ${state.name}")
        this.mState = state
        viewProvider.setState(state)
        if (state == MXState.PREPARED) {
            viewProvider.mxSeekProgress.setOnSeekBarChangeListener(onSeekBarListener)
            timeTicket.start()
        }
        if (state == MXState.PLAYING) {
            timeDelay.start()
        }
        if (state == MXState.PAUSE) {
            timeDelay.stop()
        }
        viewProvider.mxReturnBtn.visibility =
            if (mScreen == MXScreen.FULL) View.VISIBLE else View.GONE
    }

    fun seekTo(seek: Int) {
        MXUtils.log("seekTo ${MXUtils.stringForTime(seek)}")
        if (mxPlayer?.isPlaying() == true) {
            mxPlayer?.seekTo(seek)
        } else {
            seekWhenPlay = seek
        }
    }

    fun setDisplayType(type: MXScale) {
        this.displayType = type
        textureView?.setDisplayType(type)
    }

    abstract fun getLayoutId(): Int

    fun startPlay() {
        startVideo()
    }

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
            setState(MXState.PREPARING)
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

        viewProvider.mxSurfaceContainer.addView(
            textureView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        textureView.surfaceTextureListener = player
        this.textureView = textureView
        return textureView
    }

    fun onPrepared() {
        MXUtils.log("onPrepared")
        val player = mxPlayer ?: return
        setState(MXState.PREPARED)
        player.start()
        setState(MXState.PLAYING)
        if (seekWhenPlay > 0) {
            player.seekTo(seekWhenPlay)
            seekWhenPlay = 0
        }
    }

    fun onCompletion() {
        MXUtils.log("onCompletion")
        setState(MXState.COMPLETE)
    }

    fun setBufferProgress(percent: Int) {
//        MXUtils.log("setBufferProgress:$percent")
    }

    fun onSeekComplete() {
        MXUtils.log("onSeekComplete")
    }

    fun onError(error: String?) {
        MXUtils.log("onError  $error")
        setState(MXState.ERROR)
    }

    fun onBuffering(start: Boolean) {
        MXUtils.log("onBuffering:$start")
        viewProvider.mxLoading.visibility = if (start) View.VISIBLE else View.GONE
    }

    fun onRenderFirstFrame() {
        MXUtils.log("onRenderFirstFrame")
    }

    fun onVideoSizeChanged(width: Int, height: Int) {
        MXUtils.log("onVideoSizeChanged $width x $height")
        mVideoWidth = width
        mVideoHeight = height
        textureView?.setVideoSize(width, height)
    }

    fun stopPlay() {
        MXUtils.log("stopPlay")
        viewProvider.mxSurfaceContainer.removeAllViews()
        val player = mxPlayer
        textureView = null
        mxPlayer = null
        player?.release()
        if (playingVideo == this) {
            playingVideo = null
        }
        setState(MXState.IDLE)
    }

    fun getPosterImageView() = viewProvider.mxPlaceImg

    private var dimensionRatio: Double = 0.0

    /**
     * 设置View的宽高比
     */
    fun setDimensionRatio(ratio: Double) {
        if (ratio != dimensionRatio) {
            this.dimensionRatio = ratio
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (dimensionRatio > 0.0 && mScreen == MXScreen.SMALL
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
        touchHelp.setSize(w, h)
    }

    private fun switchToScreen(screen: MXScreen) {
        val windows = MXUtils.findWindowsDecorView(context) ?: return
        if (mScreen == screen) return
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
                mScreen = MXScreen.FULL
                viewProvider.mxReturnBtn.visibility = View.VISIBLE
                MXUtils.setFullScreen(context)
            }
            MXScreen.SMALL -> {
                viewProvider.mxFullscreenBtn.setImageResource(R.drawable.mx_icon_full_screen)
                val parentItem = parentMap.remove(viewIndexId) ?: return
                windows.removeView(this)
                parentItem.parentViewGroup.addView(this, parentItem.index, parentItem.layoutParams)
                requestLayout()

                mScreen = MXScreen.SMALL
                viewProvider.mxReturnBtn.visibility = View.GONE
                MXUtils.recoverFullScreen(context)
            }
        }
    }

    fun isPlaying(): Boolean {
        return (mState in arrayOf(
            MXState.PLAYING,
            MXState.PAUSE,
            MXState.PREPARING,
            MXState.PREPARED
        ))
    }

    fun isFullScreen(): Boolean {
        return mScreen == MXScreen.FULL
    }

    fun gotoSmallScreen() {
        switchToScreen(MXScreen.SMALL)
    }

    fun gotoFullScreen() {
        switchToScreen(MXScreen.FULL)
    }
}