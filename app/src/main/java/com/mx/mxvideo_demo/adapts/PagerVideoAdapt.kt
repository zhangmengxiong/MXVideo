package com.mx.mxvideo_demo.adapts

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.mx.adapt.MXBaseSimpleAdapt
import com.mx.adapt.MXBaseViewHolder
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.SourceItem
import com.mx.mxvideo_demo.databinding.AdaptFullVideoItemBinding
import com.mx.video.MXVideoStd
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScale
import com.mx.video.beans.MXSensorMode

class PagerVideoAdapt : MXBaseSimpleAdapt<SourceItem>() {
    override fun createItem(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBinding {
        return AdaptFullVideoItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(position: Int, binding: ViewBinding, record: SourceItem) {
        binding as AdaptFullVideoItemBinding
        val mxVideoStd = binding.mxVideoStd
        Glide.with(mxVideoStd.context).load(record.img)
            .into(mxVideoStd.getPosterImageView())
//        mxVideoStd.setDimensionRatio(16.0 / 9.0)
        mxVideoStd.reset()
        mxVideoStd.setScaleType(MXScale.FILL_PARENT)
        mxVideoStd.getConfig().autoFullScreenBySensor.set(false)
        mxVideoStd.getConfig().fullScreenSensorMode.set(MXSensorMode.SENSOR_AUTO)
        mxVideoStd.setSource(
            MXPlaySource(Uri.parse(record.url), record.name, isLiveSource = record.live())
        )
        mxVideoStd.setOnPlayTicketListener { position, duration ->
            println("播放进度：$position / $duration")
        }
    }

    override fun onViewDetachedFromWindow(holder: MXBaseViewHolder) {
        val mxVideoStd = holder.itemView.findViewById<MXVideoStd>(R.id.mxVideoStd)
        mxVideoStd?.stopPlay()
    }
}