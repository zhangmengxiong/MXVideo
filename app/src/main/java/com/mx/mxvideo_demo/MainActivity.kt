package com.mx.mxvideo_demo

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.adapts.HomeAdapt
import com.mx.mxvideo_demo.adapts.SimpleVideoAdapt
import com.mx.mxvideo_demo.apps.RecycleViewActivity
import com.mx.recycleview.base.BaseSimpleAdapt
import com.mx.recycleview.base.BaseViewHolder
import com.mx.video.MXPlaySource
import com.mx.video.MXScale
import com.mx.video.MXVideo
import com.mx.video.MXVideoStd
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapt = HomeAdapt(
            arrayListOf(
                HomePages("适配RecycleView", RecycleViewActivity::class.java),
            )
        )
        recycleView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycleView.adapter = adapt
        adapt.setItemClick { index, record ->
            startActivity(Intent(this, record.clazz))
        }
    }
}