package com.mx.video.views

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.mx.video.R
import com.mx.video.beans.MXConfig
import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXState
import com.mx.video.beans.MXViewAnimator
import com.mx.video.utils.MXAnimatorHelp

class MXViewSet(val rootView: View, val config: MXConfig) {
    val context = rootView.context

    val mxPlayerRootLay: View by lazy {
        rootView.findViewById(R.id.mxPlayerRootLay) ?: View(context)
    }
    val mxSurfaceContainer: FrameLayout by lazy {
        rootView.findViewById(R.id.mxSurfaceContainer) ?: FrameLayout(context)
    }

    val mxPlaceImg: ImageView by lazy {
        rootView.findViewById(R.id.mxPlaceImg) ?: ImageView(context)
    }
    val mxLoading: View by lazy {
        rootView.findViewById(R.id.mxLoading) ?: View(context)
    }
    val mxNetSpeedTxv: TextView by lazy {
        rootView.findViewById(R.id.mxNetSpeedTxv) ?: TextView(context)
    }
    val mxBottomSeekProgress: ProgressBar by lazy {
        rootView.findViewById(R.id.mxBottomSeekProgress) ?: ProgressBar(context)
    }
    val mxRetryLay: View by lazy {
        rootView.findViewById(R.id.mxRetryLay) ?: View(context)
    }

    val mxPlayPauseBtn: ImageView by lazy {
        rootView.findViewById(R.id.mxPlayPauseBtn) ?: ImageView(context)
    }
    val mxReturnBtn: View by lazy {
        rootView.findViewById(R.id.mxReturnBtn) ?: View(context)
    }
    val mxBatteryImg: View by lazy {
        rootView.findViewById(R.id.mxBatteryImg) ?: View(context)
    }
    val mxCurrentTimeTxv: TextView by lazy {
        rootView.findViewById(R.id.mxCurrentTimeTxv) ?: TextView(context)
    }
    val mxSystemTimeTxv: View by lazy {
        rootView.findViewById(R.id.mxSystemTimeTxv) ?: View(context)
    }
    val mxTotalTimeTxv: TextView by lazy {
        rootView.findViewById(R.id.mxTotalTimeTxv) ?: TextView(context)
    }
    val mxTitleTxv: TextView by lazy {
        rootView.findViewById(R.id.mxTitleTxv) ?: TextView(context)
    }
    val mxSeekProgress: SeekBar by lazy {
        rootView.findViewById(R.id.mxSeekProgress) ?: SeekBar(context)
    }
    val mxBottomLay: View by lazy {
        rootView.findViewById(R.id.mxBottomLay) ?: View(context)
    }
    val mxTopLay: View by lazy {
        rootView.findViewById(R.id.mxTopLay) ?: View(context)
    }
    val mxReplayLay: View by lazy {
        rootView.findViewById(R.id.mxReplayLay) ?: View(context)
    }
    val mxReplayImg: ImageView by lazy {
        rootView.findViewById(R.id.mxReplayImg) ?: ImageView(context)
    }
    val mxQuickSeekLay: View by lazy {
        rootView.findViewById(R.id.mxQuickSeekLay) ?: View(context)
    }
    val mxVolumeLightLay: View by lazy {
        rootView.findViewById(R.id.mxVolumeLightLay) ?: View(context)
    }
    val mxVolumeLightTypeTxv: TextView by lazy {
        rootView.findViewById(R.id.mxVolumeLightTypeTxv) ?: TextView(context)
    }
    val mxVolumeLightTxv: TextView by lazy {
        rootView.findViewById(R.id.mxVolumeLightTxv) ?: TextView(context)
    }
    val mxQuickSeekCurrentTxv: TextView by lazy {
        rootView.findViewById(R.id.mxQuickSeekCurrentTxv) ?: TextView(context)
    }
    val mxQuickSeekMaxTxv: TextView by lazy {
        rootView.findViewById(R.id.mxQuickSeekMaxTxv) ?: TextView(context)
    }
    val mxFullscreenBtn: ImageView by lazy {
        rootView.findViewById(R.id.mxFullscreenBtn) ?: ImageView(context)
    }

    /**
     * 控制相关View是否启用动画效果，以及动画属性
     */
    private val animatorPropSet = HashMap<Int, MXViewAnimator>().apply {
//        put(R.id.mxPlayPauseBtn, MXViewAnimator.CENTER)
        put(R.id.mxTopLay, MXViewAnimator.TOP)
        put(R.id.mxBottomLay, MXViewAnimator.BOTTOM)
    }

    fun attachTextureView(): MXTextureView {
        if (mxSurfaceContainer.childCount > 0) {
            detachTextureView()
        }
        setViewShow(mxSurfaceContainer, true)
        val textureView = MXTextureView(context)
        textureView.setConfig(config)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )
        params.gravity = Gravity.CENTER
        mxSurfaceContainer.addView(textureView, params)
        return textureView
    }

    fun detachTextureView() {
        (0 until mxSurfaceContainer.childCount).mapNotNull {
            mxSurfaceContainer.getChildAt(it)
        }.mapNotNull { if (it is MXTextureView) it else null }.forEach { view ->
            view.release()
        }
        mxSurfaceContainer.removeAllViews()
    }

    fun getTextureView(): MXTextureView? {
        return (0 until mxSurfaceContainer.childCount).mapNotNull {
            mxSurfaceContainer.getChildAt(it)
        }.firstNotNullOfOrNull { if (it is MXTextureView) it else null }
    }

    fun setViewShow(view: View, show: Boolean?) {
        show ?: return
        if (view.parent == null) return
        if (!view.isAttachedToWindow) return

        val duration = config.animatorDuration.get()
        val prop = animatorPropSet[view.id]
        val visibility = if (show) View.VISIBLE else View.GONE
        if (prop != null && duration > 0L) {
            if (visibility == View.VISIBLE) {
                MXAnimatorHelp.show(view, duration)
            } else {
                MXAnimatorHelp.hide(view, duration, prop)
            }
        } else {
            view.visibility = visibility
        }
    }

    fun setViewVisible(view: View, visibility: Int) {
        if (view.visibility == visibility) return
        view.visibility = visibility
    }

    fun setViewSize(view: View, size: Int) {
        val lp = view.layoutParams ?: return
        lp.width = size
        lp.height = size
        view.layoutParams = lp
    }


    fun processLoading() {
        val state = config.state.get()
        if (config.loading.get() && state in arrayOf(MXState.PLAYING, MXState.PAUSE)) {
            setViewShow(mxLoading, true)
            setViewShow(mxNetSpeedTxv, true)
            return
        }
        if (!config.isPreloading.get() && state in arrayOf(MXState.PREPARING, MXState.PREPARED)) {
            setViewShow(mxLoading, true)
            setViewShow(mxNetSpeedTxv, true)
            return
        }

        setViewShow(mxLoading, false)
        setViewShow(mxNetSpeedTxv, false)
    }

    fun processPlaceImg() {
        val state = config.state.get()
        setViewShow(
            mxPlaceImg, state !in arrayOf(MXState.PLAYING, MXState.PAUSE)
        )
    }

    fun processPlayBtn(showWhenPlaying: Boolean) {
        val state = config.state.get()

        // 预加载
        if (config.isPreloading.get() && state in arrayOf(MXState.PREPARING, MXState.PREPARED)) {
            setViewShow(mxPlayPauseBtn, true)
            mxPlayPauseBtn.setImageResource(R.drawable.mx_video_icon_player_play)
            return
        }

        if (state in arrayOf(MXState.IDLE, MXState.NORMAL, MXState.PREPARED, MXState.PAUSE)) {
            mxPlayPauseBtn.setImageResource(R.drawable.mx_video_icon_player_play)
        } else {
            mxPlayPauseBtn.setImageResource(R.drawable.mx_video_icon_player_pause)
        }

        // 下面几种情况下，显示播放按钮
        if (state in arrayOf(MXState.IDLE, MXState.NORMAL, MXState.PREPARED, MXState.PAUSE)) {
            if (config.source.get() == null && config.hidePlayBtnWhenNoSource.get()) {
                setViewShow(mxPlayPauseBtn, false)
            } else {
                setViewShow(mxPlayPauseBtn, true)
            }
            return
        }
        // 下面几种情况下，隐藏播放按钮
        if (state in arrayOf(MXState.PREPARING, MXState.ERROR, MXState.COMPLETE)) {
            setViewShow(mxPlayPauseBtn, false)
            return
        }

        // 直播流隐藏播放按钮
        if (config.source.get()?.isLiveSource == true) {
            setViewShow(mxPlayPauseBtn, false)
            return
        }
        // 播放控制需要显示
        if (!showWhenPlaying) {
            setViewShow(mxPlayPauseBtn, false)
            return
        }
        setViewShow(mxPlayPauseBtn, true)
    }

    fun processTopLay(showWhenPlaying: Boolean) {
        val state = config.state.get()
        val screen = config.screen.get()

        // 全屏的时候，现实顶部导航栏
        if (screen == MXScreen.FULL && state in arrayOf(
                MXState.IDLE, MXState.NORMAL, MXState.PREPARING, MXState.ERROR, MXState.COMPLETE
            )
        ) {
            setViewShow(mxTopLay, true)
            return
        }
        if (showWhenPlaying && state == MXState.PLAYING) {
            setViewShow(mxTopLay, true)
            return
        }
        if (state == MXState.PAUSE) {
            setViewShow(mxTopLay, true)
            return
        }
        setViewShow(mxTopLay, false)
    }

    fun processBottomLay(showWhenPlaying: Boolean) {
        val state = config.state.get()
        if (showWhenPlaying && state == MXState.PLAYING) {
            setViewShow(mxBottomLay, true)
            return
        }
        if (state == MXState.PAUSE) {
            setViewShow(mxBottomLay, true)
            return
        }
        setViewShow(mxBottomLay, false)
    }

    fun processBottomSeekView(showWhenPlaying: Boolean) {
        // 设置不可见
        if (!config.canShowBottomSeekBar.get()) {
            setViewShow(mxBottomSeekProgress, false)
            return
        }
        // 直播源时隐藏
        if (config.source.get()?.isLiveSource == true) {
            setViewShow(mxBottomSeekProgress, false)
            return
        }

        val state = config.state.get()
        if (state == MXState.PLAYING && !showWhenPlaying) {
            setViewShow(mxBottomSeekProgress, true)
            return
        }
        setViewShow(mxBottomSeekProgress, false)
    }

    fun processOthers() {
        val state = config.state.get()

        setViewShow(mxReplayLay, state == MXState.COMPLETE)
        setViewShow(mxRetryLay, state == MXState.ERROR)
    }

    fun release() {
        val batteryImg = mxBatteryImg
        if (batteryImg is MXBatteryImageView) {
            batteryImg.release()
        }

        val timeTxv = mxSystemTimeTxv
        if (timeTxv is MXTimeTextView) {
            timeTxv.release()
        }
        mxSeekProgress.setOnSeekBarChangeListener(null)
    }
}