package com.mx.video.views

import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.mx.video.MXScreen
import com.mx.video.MXState
import com.mx.video.MXVideo
import com.mx.video.R
import com.mx.video.utils.*
import kotlin.math.abs

class MXViewProvider(
    private val mxVideo: MXVideo,
    private val mxConfig: MXConfig
) {

    val timeTicket = MXTicket()
    val timeDelay = MXDelay()
    val touchHelp by lazy { MXTouchHelp(mxVideo.context, mxConfig) }
    var mState: MXState = MXState.IDLE
    var mScreen: MXScreen = MXScreen.SMALL

    val mxSurfaceContainer: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxSurfaceContainer) ?: LinearLayout(mxVideo.context)
    }

    val mxPlaceImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxPlaceImg) ?: ImageView(mxVideo.context)
    }
    val mxLoading: ProgressBar by lazy {
        mxVideo.findViewById(R.id.mxLoading) ?: ProgressBar(mxVideo.context)
    }

    val mxPlayBtn: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxPlayBtn) ?: LinearLayout(mxVideo.context)
    }
    private val mxRetryLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxRetryLay) ?: LinearLayout(mxVideo.context)
    }

    private val mxPlayPauseImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxPlayPauseImg) ?: ImageView(mxVideo.context)
    }
    val mxReturnBtn: ImageView by lazy {
        mxVideo.findViewById(R.id.mxReturnBtn) ?: ImageView(mxVideo.context)
    }
    private val mxBatteryImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxBatteryImg) ?: ImageView(mxVideo.context)
    }
    val mxCurrentTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxCurrentTimeTxv) ?: TextView(mxVideo.context)
    }
    private val mxSystemTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxSystemTimeTxv) ?: TextView(mxVideo.context)
    }
    private val mxTotalTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxTotalTimeTxv) ?: TextView(mxVideo.context)
    }
    val mxTitleTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxTitleTxv) ?: TextView(mxVideo.context)
    }
    private val mxSeekProgress: SeekBar by lazy {
        mxVideo.findViewById(R.id.mxSeekProgress) ?: SeekBar(mxVideo.context)
    }
    private val mxBottomLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxBottomLay) ?: LinearLayout(mxVideo.context)
    }
    private val mxTopLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxTopLay) ?: LinearLayout(mxVideo.context)
    }
    private val mxReplayLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxReplayLay) ?: LinearLayout(mxVideo.context)
    }
    private val mxQuickSeekLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxQuickSeekLay) ?: LinearLayout(mxVideo.context)
    }
    val mxQuickSeekImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxQuickSeekImg) ?: ImageView(mxVideo.context)
    }
    val mxQuickSeekTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxQuickSeekTxv) ?: TextView(mxVideo.context)
    }
    val mxFullscreenBtn: ImageView by lazy {
        mxVideo.findViewById(R.id.mxFullscreenBtn) ?: ImageView(mxVideo.context)
    }

    /**
     * 根节点View列表
     */
    private val allContentView = arrayOf(
        mxPlaceImg,
        mxLoading,
        mxPlayBtn,
        mxTopLay,
        mxBottomLay,
        mxRetryLay,
        mxReplayLay,
        mxQuickSeekLay
    )

    /**
     * 播放时点击需要显示的状态
     */
    private val playingVisible = arrayOf(mxPlayBtn, mxTopLay, mxBottomLay)

    fun initView() {
        mxSurfaceContainer.setOnClickListener {
            if (mState in arrayOf(MXState.PLAYING, MXState.PAUSE)) {
                if (mxPlayBtn.isShown) {
                    mxPlayBtn.visibility = View.GONE
                    mxBottomLay.visibility = View.GONE
                    mxTopLay.visibility = View.GONE
                    timeDelay.stop()
                } else {
                    mxPlayBtn.visibility = View.VISIBLE
                    mxBottomLay.visibility = View.VISIBLE
                    mxTopLay.visibility = View.VISIBLE
                    timeDelay.start()
                }
            } else if (mScreen == MXScreen.FULL) {
                mxTopLay.visibility = View.VISIBLE
                timeDelay.start()
            }
        }

        timeDelay.setDelayRun(5000) {
            if (!mxVideo.isShown || mState != MXState.PLAYING) {
                return@setDelayRun
            }
            mxPlayBtn.visibility = View.GONE
            mxBottomLay.visibility = View.GONE
            mxTopLay.visibility = View.GONE
        }
        timeTicket.setTicketRun(300) {
            if (!mxVideo.isShown) return@setTicketRun
            if (mState in arrayOf(
                    MXState.PREPARED,
                    MXState.PREPARING,
                    MXState.PLAYING,
                    MXState.PAUSE
                ) && mxVideo.isPlaying()
            ) {
                val duration = mxVideo.getDuration()
                val position = mxVideo.getCurrentPosition()
                mxSeekProgress.max = duration
                mxSeekProgress.progress = position
                mxCurrentTimeTxv.text = MXUtils.stringForTime(position)
                mxTotalTimeTxv.text = MXUtils.stringForTime(duration)
            }
        }

        mxSurfaceContainer.setOnTouchListener { _, motionEvent ->
            if (mScreen == MXScreen.FULL && mState == MXState.PLAYING) {
                // 全屏且正在播放才会触发触摸滑动
                touchHelp.onTouch(motionEvent)
            }
            return@setOnTouchListener false
        }
        touchHelp.setHorizontalTouchCall(object : MXTouchHelp.OnMXTouchListener() {
            override fun onStart(touchDownPercent: Float) {
                super.onStart(touchDownPercent)
                if (!mxConfig.canSeekByUser) return
                allContentView.forEach {
                    if (it == mxQuickSeekLay) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
            }

            override fun onTouchMove(percent: Float) {
                if (!mxConfig.canSeekByUser) return
                val duration = mxVideo.getDuration()
                val position = abs(duration * percent).toInt()
                mxQuickSeekImg.setImageResource(if (touchDownPercent > percent) R.drawable.mx_icon_seek_left else R.drawable.mx_icon_seek_right)
                mxQuickSeekTxv.text =
                    MXUtils.stringForTime(position) + "/" + MXUtils.stringForTime(duration)
            }

            override fun onEnd(percent: Float) {
                if (!mxConfig.canSeekByUser || percent < 0 || percent > 1) return
                mxQuickSeekLay.visibility = View.GONE
                val duration = mxVideo.getDuration()
                val position = abs(duration * percent).toInt()
                mxVideo.seekTo(position)
                timeDelay.start()
            }
        })

        mxRetryLay.setOnClickListener {
            mxVideo.startPlay()
        }
        mxReplayLay.setOnClickListener {
            mxVideo.startPlay()
        }
        mxFullscreenBtn.setOnClickListener {
            if (mScreen == MXScreen.SMALL) {
                mxVideo.gotoFullScreen()
            } else {
                mxVideo.gotoSmallScreen()
            }
        }
        mxReturnBtn.setOnClickListener {
            if (mScreen == MXScreen.FULL) {
                mxVideo.gotoSmallScreen()
            }
        }
    }

    private val onSeekBarListener = object : SeekBar.OnSeekBarChangeListener {
        var progress = 0
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser && mxConfig.canSeekByUser) {
                this.progress = progress
                mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            if (!mxConfig.canSeekByUser) return
            MXUtils.log("onStartTrackingTouch")
            this.progress = seekBar?.progress ?: return
            timeTicket.stop()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if (!mxConfig.canSeekByUser) return
            MXUtils.log("onStopTrackingTouch")
            mxCurrentTimeTxv.text = MXUtils.stringForTime(progress)
            mxVideo.seekTo(progress)
            timeTicket.start()
        }
    }

    fun setState(state: MXState) {
        mState = state
        if (!mxConfig.canFullScreen) {
            mxFullscreenBtn.visibility = View.GONE
        } else {
            mxFullscreenBtn.visibility = View.VISIBLE
        }
        mxSeekProgress.isEnabled = mxConfig.canSeekByUser
        if (!mxConfig.canShowSystemTime) {
            mxSystemTimeTxv.visibility = View.GONE
        } else {
            mxSystemTimeTxv.visibility = View.VISIBLE
        }
        if (!mxConfig.canShowBatteryImg) {
            mxBatteryImg.visibility = View.GONE
        } else {
            mxBatteryImg.visibility = View.VISIBLE
        }
        when (state) {
            MXState.IDLE, MXState.NORMAL -> {
                allContentView.forEach {
                    if (it in arrayOf(mxPlaceImg, mxPlayBtn)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            }
            MXState.PREPARING -> {
                allContentView.forEach {
                    if (it in arrayOf(mxPlaceImg, mxLoading)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
            }
            MXState.PREPARED -> {
                allContentView.forEach {
                    if (it in arrayOf(mxLoading)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                mxSeekProgress.setOnSeekBarChangeListener(onSeekBarListener)
                timeTicket.start()
            }
            MXState.PLAYING -> {
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_pause)
                allContentView.forEach {
                    if (it !in playingVisible) {
                        it.visibility = View.GONE
                    }
                }
                if (mxConfig.canSeekByUser) {
                    mxSeekProgress.isEnabled = true
                }
                timeDelay.start()
            }
            MXState.PAUSE -> {
                allContentView.forEach {
                    if (it in playingVisible) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                mxSeekProgress.isEnabled = false
                timeDelay.stop()
            }
            MXState.ERROR -> {
                allContentView.forEach {
                    if (it in arrayOf(mxPlaceImg, mxRetryLay)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
            }
            MXState.COMPLETE -> {
                allContentView.forEach {
                    if (it in arrayOf(mxPlaceImg, mxReplayLay)) {
                        it.visibility = View.VISIBLE
                    } else {
                        it.visibility = View.GONE
                    }
                }
            }
        }
        mxReturnBtn.visibility = if (mScreen == MXScreen.FULL) View.VISIBLE else View.GONE
    }
}