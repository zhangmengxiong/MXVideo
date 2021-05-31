package com.mx.mxvideo_demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mx.mxvideo_demo.adapts.HomeAdapt
import com.mx.mxvideo_demo.apps.FullScreenActivity
import com.mx.mxvideo_demo.apps.NormalActivity
import com.mx.mxvideo_demo.apps.RecycleViewActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapt = HomeAdapt(
            arrayListOf(
                HomePages("播放器(各种参数切换)", NormalActivity::class.java),
                HomePages("适配RecycleView", RecycleViewActivity::class.java),
                HomePages("全屏播放器", FullScreenActivity::class.java),
            )
        )
        recycleView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycleView.adapter = adapt
        adapt.setItemClick { index, record ->
            startActivity(Intent(this, record.clazz))
        }
    }
}