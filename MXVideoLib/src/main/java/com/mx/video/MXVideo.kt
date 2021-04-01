package com.mx.video

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import com.mx.video.utils.MXTicket
import com.mx.video.utils.MXUtils
import java.util.concurrent.atomic.AtomicInteger

abstract class MXVideo @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private val videoViewIndex = AtomicInteger(1)
        private val parentMap = HashMap<Int, MXParentView>()
        var mContext: Context? = null
        fun getAppContext() = mContext!!
    }

    init {
        mContext = context.applicationContext
    }

    private var mVideoWidth: Int = 1280
    private var mVideoHeight: Int = 720
    private val viewIndexId = videoViewIndex.incrementAndGet()
    private val surfaceContainer: LinearLayout by lazy {
        findViewById(R.id.mxSurfaceContainer) ?: LinearLayout(context)
    }

    private val placeImg: ImageView by lazy {
        findViewById(R.id.mxPlaceImg) ?: ImageView(context)
    }
    private val mxLoading: ProgressBar by lazy {
        findViewById(R.id.mxLoading) ?: ProgressBar(context)
    }

    private val playBtn: LinearLayout by lazy {
        findViewById(R.id.mxPlayBtn) ?: LinearLayout(context)
    }
    private val mxRetryLay: LinearLayout by lazy {
        findViewById(R.id.mxRetryLay) ?: LinearLayout(context)
    }

    private val playPauseImg: ImageView by lazy {
        findViewById(R.id.mxPlayPauseImg) ?: ImageView(context)
    }
    private val mxCurrentTimeTxv: TextView by lazy {
        findViewById(R.id.mxCurrentTimeTxv) ?: TextView(context)
    }
    private val mxTotalTimeTxv: TextView by lazy {
        findViewById(R.id.mxTotalTimeTxv) ?: TextView(context)
    }
    private val mxSeekProgress: SeekBar by lazy {
        findViewById(R.id.mxSeekProgress) ?: SeekBar(context)
    }
    private val mxBottomLay: LinearLayout by lazy {
        findViewById(R.id.mxBottomLay) ?: LinearLayout(context)
    }
    private val mxFullscreenBtn: ImageView by lazy {
        findViewById(R.id.mxFullscreenBtn) ?: ImageView(context)
    }

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

    private val timeTicket = MXTicket()

    init {
        View.inflate(context, getLayoutId(), this)
        initView()
        setState(MXState.IDLE)
        setBackgroundColor(Color.GRAY)
    }

    private fun initView() {
        playBtn.setOnClickListener {
            if (currentSource == null) {
                Toast.makeText(context, "请设置播放地址！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val player = mxPlayer
            when (mState) {
                MXState.PLAYING -> {
                    if (player != null) {
                        player.pause()
                        setState(MXState.PAUSE)
                    }
                }
                MXState.PAUSE -> {
                    if (player != null) {
                        player.start()
                        setState(MXState.PLAYING)
                    }
                }
                MXState.COMPLETE -> {

                }
            }

        }
        surfaceContainer.setOnClickListener {
            if (mState !in arrayOf(
                    MXState.PLAYING,
                    MXState.PAUSE
                )
            ) return@setOnClickListener
            if (playBtn.isShown) {
                playBtn.visibility = View.GONE
                mxBottomLay.visibility = View.GONE
            } else {
                playBtn.visibility = View.VISIBLE
                mxBottomLay.visibility = View.VISIBLE
            }
        }

        mxRetryLay.setOnClickListener {
            startVideo()
        }
        mxFullscreenBtn.setOnClickListener {
            if (mScreen == MXScreen.SMALL) {
                switchToScreen(MXScreen.FULL)
            } else {
                switchToScreen(MXScreen.SMALL)
            }
        }
        timeTicket.setTicketRun(300) {
            val player = mxPlayer ?: return@setTicketRun
            if (mState in arrayOf(
                    MXState.PREPARED,
                    MXState.PREPARING,
                    MXState.PLAYING,
                    MXState.PAUSE
                ) && player.isPlaying() && !onSeekBarListener.isUserInSeek
            ) {
                val duration = player.getDuration()
                val position = player.getCurrentPosition()
                mxSeekProgress.max = duration
                mxSeekProgress.progress = position
                mxCurrentTimeTxv.text = MXUtils.stringForTime(position)
                mxTotalTimeTxv.text = MXUtils.stringForTime(duration)
            }
        }
    }

    private val onSeekBarListener = object : SeekBar.OnSeekBarChangeListener {
        var progress = 0
        var isUserInSeek = false
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                this.progress = progress
                mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            MXUtils.log("onStartTrackingTouch")
            this.progress = seekBar?.progress ?: return
            isUserInSeek = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            MXUtils.log("onStopTrackingTouch")
            mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            isUserInSeek = false
            seekTo(progress)
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
        if (start) {
            startVideo()
        } else {
            setState(MXState.NORMAL)
        }
    }

    private fun setState(state: MXState) {
        MXUtils.log("setState  ${state.name}")
        this.mState = state
        when (state) {
            MXState.IDLE -> {
                playBtn.visibility = View.VISIBLE
                playPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                mxRetryLay.visibility = View.GONE
            }
            MXState.NORMAL -> {
                mxRetryLay.visibility = View.GONE
                playBtn.visibility = View.VISIBLE
                playPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            }
            MXState.PREPARING -> {
                mxRetryLay.visibility = View.GONE
                playBtn.visibility = View.GONE
                mxLoading.visibility = View.VISIBLE
            }
            MXState.PREPARED -> {
                mxRetryLay.visibility = View.GONE
                mxLoading.visibility = View.GONE
                playPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                startTimerTicket()
                mxSeekProgress.setOnSeekBarChangeListener(onSeekBarListener)
            }
            MXState.PLAYING -> {
                playPauseImg.setImageResource(R.drawable.mx_icon_player_pause)
            }
            MXState.PAUSE -> {
                playPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            }
            MXState.ERROR -> {
                mxRetryLay.visibility = View.VISIBLE
                playBtn.visibility = View.GONE
                mxLoading.visibility = View.GONE
                mxBottomLay.visibility = View.GONE
            }
        }
    }

    fun seekTo(seek: Int) {
        MXUtils.log("seekTo")
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


    private fun startVideo() {
        MXUtils.log("startVideo ${currentSource?.playUrl}")
        val clazz = mxPlayerClass ?: return
        val source = currentSource ?: return
        val constructor = clazz.getConstructor()
        val player = (constructor.newInstance() as IMXPlayer)
        mxPlayer = player
        player.setSource(source)
        val textureView = addTextureView(player)
        player.setMXVideo(this, textureView)
        MXUtils.findWindows(context)?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setState(MXState.PREPARING)
    }

    private fun addTextureView(player: IMXPlayer): MXTextureView {
        MXUtils.log("addTextureView")
        surfaceContainer.removeAllViews()
        val textureView = MXTextureView(context.applicationContext)
        textureView.setVideoSize(mVideoWidth, mVideoHeight)
        textureView.setDisplayType(displayType)

        surfaceContainer.addView(
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
        mxLoading.visibility = if (start) View.VISIBLE else View.GONE
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
        surfaceContainer.removeAllViews()
        val player = mxPlayer
        textureView = null
        mxPlayer = null
        player?.release()
    }

    private fun startTimerTicket() {
        timeTicket.start()
    }

    private fun stopTimerTicket() {
        timeTicket.stop()
    }

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

    private fun switchToScreen(screen: MXScreen) {
        val windows = MXUtils.findWindowsDecorView(context) ?: return
        if (mScreen == screen) return
        when (screen) {
            MXScreen.FULL -> {
                mxFullscreenBtn.setImageResource(R.drawable.mx_icon_small_screen)
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
                MXUtils.setFullScreen(context)
            }
            MXScreen.SMALL -> {
                mxFullscreenBtn.setImageResource(R.drawable.mx_icon_full_screen)
                val parentItem = parentMap.remove(viewIndexId) ?: return
                windows.removeView(this)
                parentItem.parentViewGroup.addView(this, parentItem.index, parentItem.layoutParams)
                requestLayout()

                mScreen = MXScreen.SMALL
                MXUtils.recoverFullScreen(context)
            }
        }
    }
}