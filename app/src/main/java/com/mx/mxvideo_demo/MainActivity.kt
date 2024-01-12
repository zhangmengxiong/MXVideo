package com.mx.mxvideo_demo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mx.mxvideo_demo.adapts.HomeAdapt
import com.mx.mxvideo_demo.apps.FullScreenActivity
import com.mx.mxvideo_demo.apps.NormalActivity
import com.mx.mxvideo_demo.apps.RecyclePagerActivity
import com.mx.mxvideo_demo.apps.RecycleViewActivity
import com.mx.mxvideo_demo.apps.TestActivity
import com.mx.mxvideo_demo.databinding.ActivityMainBinding
import com.mx.video.MXVideo

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        SourceItem.init(this)
        val adapt = HomeAdapt(
            arrayListOf(
                HomePages("播放器(各种参数切换)", NormalActivity::class.java),
                HomePages("播放器(测试)", TestActivity::class.java),
                HomePages("适配RecycleView", RecycleViewActivity::class.java),
                HomePages("适配Pager全屏RecycleView", RecyclePagerActivity::class.java),
                HomePages("全屏播放器", FullScreenActivity::class.java),
                HomePages("清理播放记录", null) {
                    MXVideo.clearProgress()
                    Toast.makeText(this, "清理成功！", Toast.LENGTH_SHORT).show()
                },
            )
        )
        binding.recycleView.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false
        )
        binding.recycleView.adapter = adapt
        adapt.setItemClick { index, record ->
            if (record.action != null) {
                record.action.invoke()
                return@setItemClick
            }
            startActivity(Intent(this, record.clazz))
        }
    }
}