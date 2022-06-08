package com.mx.mxvideo_demo.adapts

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.mx.adapt.MXBaseSimpleAdapt
import com.mx.adapt.MXBaseViewHolder
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.thumbnails
import com.mx.video.MXVideoStd
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXSensorMode

class SimpleVideoAdapt : MXBaseSimpleAdapt<String>() {
    override fun createItem(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
        return inflater.inflate(
            R.layout.adapt_video_item, parent, false
        )
    }

    override fun bindView(position: Int, itemView: View, record: String) {
        val mxVideoStd = itemView.findViewById<MXVideoStd>(R.id.mxVideoStd)
        Glide.with(mxVideoStd.context).load(thumbnails.random())
            .into(mxVideoStd.getPosterImageView())
        mxVideoStd.setDimensionRatio(16.0 / 9.0)
        mxVideoStd.reset()
        mxVideoStd.getConfig().autoFullScreenBySensor.set(false)
        mxVideoStd.getConfig().fullScreenSensorMode.set(MXSensorMode.SENSOR_AUTO)
        mxVideoStd.setSource(
            MXPlaySource(Uri.parse(record), "" + position)
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