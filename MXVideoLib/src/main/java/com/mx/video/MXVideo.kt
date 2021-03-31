package com.mx.video

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.mx.video.player.IMXPlayer
import com.mx.video.player.MXSystemPlayer
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

    private val playPauseImg: ImageView by lazy {
        findViewById(R.id.mxPlayPauseImg) ?: ImageView(context)
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

    init {
        View.inflate(context, getLayoutId(), this)
        initView()
    }

    private fun initView() {
        playBtn.setOnClickListener {
            if (mState == MXPlayState.IDLE) return@setOnClickListener

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
            }
            MXPlayState.PREPARING -> {
                playBtn.visibility = View.GONE
                mxLoading.visibility = View.VISIBLE
            }
            MXPlayState.PREPARED -> {
                mxLoading.visibility = View.GONE
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
        if (displayType != type) {
            this.displayType = type
            textureView?.setDisplayType(type)
        }
    }

    abstract fun getLayoutId(): Int


    fun startVideo() {
        MXUtils.log("startVideo")
        val clazz = mxPlayerClass ?: return
        val source = currentSource ?: return
        val constructor = clazz.getConstructor()
        val player = (constructor.newInstance() as IMXPlayer)
        mxPlayer = player
        player.setMXVideo(this)
        addTextureView()
        MXUtils.findWindows(context)?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        player.prepare(source)
        setState(MXPlayState.PREPARING)
    }

    fun addTextureView() {
        MXUtils.log("addTextureView")
        surfaceContainer.removeAllViews()
        val textureView = MXTextureView(context.applicationContext)
        surfaceContainer.addView(
            textureView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        textureView.surfaceTextureListener = mxPlayer
        textureView.setDisplayType(displayType)
        this.textureView = textureView
    }

    fun onPrepared() {
        MXUtils.log("onPrepared")
        val player = mxPlayer ?: return
        setState(MXPlayState.PREPARED)
        player.start()
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
        MXUtils.log("setBufferProgress:$percent")
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
}