package com.mx.video.utils

import android.view.View
import android.widget.*
import com.mx.video.MXState
import com.mx.video.MXVideo
import com.mx.video.R

class MXViewProvider(private val mxVideo: MXVideo) {
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
    val mxRetryLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxRetryLay) ?: LinearLayout(mxVideo.context)
    }

    val mxPlayPauseImg: ImageView by lazy {
        mxVideo.findViewById(R.id.mxPlayPauseImg) ?: ImageView(mxVideo.context)
    }
    val mxReturnBtn: ImageView by lazy {
        mxVideo.findViewById(R.id.mxReturnBtn) ?: ImageView(mxVideo.context)
    }
    val mxCurrentTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxCurrentTimeTxv) ?: TextView(mxVideo.context)
    }
    val mxTotalTimeTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxTotalTimeTxv) ?: TextView(mxVideo.context)
    }
    val mxTitleTxv: TextView by lazy {
        mxVideo.findViewById(R.id.mxTitleTxv) ?: TextView(mxVideo.context)
    }
    val mxSeekProgress: SeekBar by lazy {
        mxVideo.findViewById(R.id.mxSeekProgress) ?: SeekBar(mxVideo.context)
    }
    val mxBottomLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxBottomLay) ?: LinearLayout(mxVideo.context)
    }
    val mxTopLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxTopLay) ?: LinearLayout(mxVideo.context)
    }
    val mxReplayLay: LinearLayout by lazy {
        mxVideo.findViewById(R.id.mxReplayLay) ?: LinearLayout(mxVideo.context)
    }
    val mxQuickSeekLay: LinearLayout by lazy {
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

    fun setState(state: MXState) {
        when (state) {
            MXState.IDLE -> {
                mxPlaceImg.visibility = View.VISIBLE
                mxLoading.visibility = View.GONE
                mxPlayBtn.visibility = View.VISIBLE
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                mxRetryLay.visibility = View.GONE
                mxBottomLay.visibility = View.GONE
                mxTopLay.visibility = View.GONE
                mxReplayLay.visibility = View.GONE
                mxQuickSeekLay.visibility = View.GONE
            }
            MXState.NORMAL -> {
                mxPlaceImg.visibility = View.VISIBLE
                mxLoading.visibility = View.GONE
                mxRetryLay.visibility = View.GONE
                mxBottomLay.visibility = View.GONE
                mxTopLay.visibility = View.GONE
                mxReplayLay.visibility = View.GONE
                mxPlayBtn.visibility = View.VISIBLE
                mxQuickSeekLay.visibility = View.GONE
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
            }
            MXState.PREPARING -> {
                mxPlaceImg.visibility = View.VISIBLE
                mxRetryLay.visibility = View.GONE
                mxPlayBtn.visibility = View.GONE
                mxLoading.visibility = View.VISIBLE
                mxBottomLay.visibility = View.GONE
                mxTopLay.visibility = View.GONE
                mxReplayLay.visibility = View.GONE
                mxQuickSeekLay.visibility = View.GONE
            }
            MXState.PREPARED -> {
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                mxPlaceImg.visibility = View.GONE
                mxLoading.visibility = View.GONE
                mxPlayBtn.visibility = View.GONE
                mxTopLay.visibility = View.GONE
                mxBottomLay.visibility = View.GONE
                mxRetryLay.visibility = View.GONE
                mxReplayLay.visibility = View.GONE
                mxQuickSeekLay.visibility = View.GONE
            }
            MXState.PLAYING -> {
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_pause)
                mxPlaceImg.visibility = View.GONE
                mxLoading.visibility = View.GONE
//                mxPlayBtn.visibility = View.GONE
//                mxTopLay.visibility = View.GONE
//                mxBottomLay.visibility = View.GONE
                mxRetryLay.visibility = View.GONE
                mxReplayLay.visibility = View.GONE
                mxQuickSeekLay.visibility = View.GONE
            }
            MXState.PAUSE -> {
                mxPlayPauseImg.setImageResource(R.drawable.mx_icon_player_play)
                mxPlaceImg.visibility = View.GONE
                mxLoading.visibility = View.GONE
                mxPlayBtn.visibility = View.VISIBLE
                mxTopLay.visibility = View.VISIBLE
                mxBottomLay.visibility = View.VISIBLE
                mxRetryLay.visibility = View.GONE
                mxReplayLay.visibility = View.GONE
                mxQuickSeekLay.visibility = View.GONE
            }
            MXState.ERROR -> {
                mxPlaceImg.visibility = View.VISIBLE
                mxLoading.visibility = View.GONE
                mxPlayBtn.visibility = View.GONE
                mxTopLay.visibility = View.GONE
                mxBottomLay.visibility = View.GONE
                mxRetryLay.visibility = View.VISIBLE
                mxReplayLay.visibility = View.GONE
                mxQuickSeekLay.visibility = View.GONE
            }
            MXState.COMPLETE -> {
                mxPlaceImg.visibility = View.VISIBLE
                mxLoading.visibility = View.GONE
                mxPlayBtn.visibility = View.GONE
                mxTopLay.visibility = View.GONE
                mxBottomLay.visibility = View.GONE
                mxRetryLay.visibility = View.GONE
                mxReplayLay.visibility = View.VISIBLE
                mxQuickSeekLay.visibility = View.GONE
            }
        }
    }
}