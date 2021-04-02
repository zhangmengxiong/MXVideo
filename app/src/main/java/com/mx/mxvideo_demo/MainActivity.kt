package com.mx.mxvideo_demo

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mx.recycleview.base.BaseSimpleAdapt
import com.mx.video.MXPlaySource
import com.mx.video.MXScale
import com.mx.video.MXVideo
import com.mx.video.MXVideoStd
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapt.list.addAll(ldjVideos)

        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
                outRect.top = 10
                outRect.bottom = 10
            }
        })
        recycleView.adapter = adapt
    }

    private val adapt = object : BaseSimpleAdapt<String>() {
        override fun createItem(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
            return inflater.inflate(R.layout.adapt_video_item, parent, false)
        }

        override fun bindView(position: Int, itemView: View, record: String) {
            val mxVideoStd = itemView.findViewById<MXVideoStd>(R.id.mxVideoStd)
            Glide.with(mxVideoStd.context).load(thumbnails.random())
                .into(mxVideoStd.getPosterImageView())
            mxVideoStd.setDimensionRatio(16.0 / 9.0)
            mxVideoStd.setSource(
                MXPlaySource(record, "" + position, isOnlineSource = true),
                start = false
            )
        }


    }

    override fun onBackPressed() {
        if (MXVideo.isFullScreen()) {
            MXVideo.gotoNormalScreen()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        MXVideo.releaseAll()
        super.onDestroy()
    }
}