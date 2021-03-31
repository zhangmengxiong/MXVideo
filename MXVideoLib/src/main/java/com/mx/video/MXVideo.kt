package com.mx.video

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
import com.mx.video.utils.MXTicket
import com.mx.video.utils.MXUtils

abstract class MXVideo @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        var mContext: Context? = null
        fun getAppContext() = mContext!!
    }

    init {
        mContext = context.applicationContext
    }

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

    /**
     * 播放状态
     */
    var mState = MXPlayState.IDLE
        private set

    var currentSource: MXPlaySource? = null
    private var mxPlayerClass: Class<*>? = null
    private var mxPlayer: IMXPlayer? = null
    private var textureView: MXTextureView? = null // 当前TextureView
    private var displayType: MXVideoDisplay = MXVideoDisplay.CENTER_CROP
    private var seekWhenPlay: Int = 0

    private val timeTicket = MXTicket()

    init {
        View.inflate(context, getLayoutId(), this)
        initView()
        setState(MXPlayState.IDLE)
    }

    private fun initView() {
        playBtn.setOnClickListener {
            val player = mxPlayer
            when (mState) {
                MXPlayState.PLAYING -> {
                    if (player != null) {
                        player.pause()
                        setState(MXPlayState.PAUSE)
                    }
                }
                MXPlayState.PAUSE -> {
                    if (player != null) {
                        player.start()
                        setState(MXPlayState.PLAYING)
                    }
                }
                MXPlayState.COMPLETE -> {

                }
            }

        }
        surfaceContainer.setOnClickListener {
            if (mState !in arrayOf(
                    MXPlayState.PLAYING,
                    MXPlayState.PAUSE
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

        mxSeekProgress.setOnSeekBarChangeListener(onSeekBarListener)
        mxRetryLay.setOnClickListener {
            startVideo()
        }
        timeTicket.setTicketRun(300) {
            val player = mxPlayer ?: return@setTicketRun
            if (mState in arrayOf(
                    MXPlayState.PREPARED,
                    MXPlayState.PREPARING,
                    MXPlayState.PLAYING,
                    MXPlayState.PAUSE
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
            setState(MXPlayState.NORMAL)
        }
    }

    private fun setState(state: MXPlayState) {
        MXUtils.log("setState  ${state.name}")
        this.mState = state
        when (state) {
            MXPlayState.IDLE -> {
                playBtn.visibility = View.GONE
                mxRetryLay.visibility = View.GONE
            }
            MXPlayState.NORMAL -> {
                mxRetryLay.visibility = View.GONE
                playBtn.visibility = View.VISIBLE
                playPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            }
            MXPlayState.PREPARING -> {
                mxRetryLay.visibility = View.GONE
                playBtn.visibility = View.GONE
                mxLoading.visibility = View.VISIBLE
            }
            MXPlayState.PREPARED -> {
                mxRetryLay.visibility = View.GONE
                mxLoading.visibility = View.GONE
                playPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                startTimerTicket()
            }
            MXPlayState.PLAYING -> {
                playPauseImg.setImageResource(R.drawable.mx_icon_player_pause)
            }
            MXPlayState.PAUSE -> {
                playPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            }
            MXPlayState.ERROR -> {
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

    fun setDisplayType(type: MXVideoDisplay) {
        this.displayType = type
        textureView?.setDisplayType(type)
    }

    abstract fun getLayoutId(): Int


    fun startVideo() {
        MXUtils.log("startVideo")
        val clazz = mxPlayerClass ?: return
        val source = currentSource ?: return
        val constructor = clazz.getConstructor()
        val player = (constructor.newInstance() as IMXPlayer)
        mxPlayer = player
        player.setSource(source)
        player.setMXVideo(this)
        addTextureView(player)
        MXUtils.findWindows(context)?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setState(MXPlayState.PREPARING)
    }

    private fun addTextureView(player: IMXPlayer) {
        MXUtils.log("addTextureView")
        surfaceContainer.removeAllViews()
        val textureView = MXTextureView(context.applicationContext)
        surfaceContainer.addView(
            textureView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        textureView.surfaceTextureListener = player
        textureView.setDisplayType(displayType)
        this.textureView = textureView
    }

    fun onPrepared() {
        MXUtils.log("onPrepared")
        val player = mxPlayer ?: return
        setState(MXPlayState.PREPARED)
        player.start()
        setState(MXPlayState.PLAYING)
        if (seekWhenPlay > 0) {
            player.seekTo(seekWhenPlay)
            seekWhenPlay = 0
        }
    }

    fun onCompletion() {
        MXUtils.log("onCompletion")
        setState(MXPlayState.COMPLETE)
    }

    fun setBufferProgress(percent: Int) {
//        MXUtils.log("setBufferProgress:$percent")
    }

    fun onSeekComplete() {
        MXUtils.log("onSeekComplete")
    }

    fun onError() {
        MXUtils.log("onError")
        setState(MXPlayState.ERROR)
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
        textureView?.setVideoSize(width, height)
    }

    fun stopPlay() {
        MXUtils.log("stopPlay")
        mxPlayer?.release()
        surfaceContainer.removeAllViews()
    }

    private fun startTimerTicket() {
        timeTicket.start()
    }

    private fun stopTimerTicket() {
        timeTicket.stop()
    }

}