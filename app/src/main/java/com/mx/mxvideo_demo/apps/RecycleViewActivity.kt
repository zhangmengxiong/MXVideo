package com.mx.mxvideo_demo.apps

import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mx.mxvideo_demo.SourceItem
import com.mx.mxvideo_demo.adapts.SimpleVideoAdapt
import com.mx.mxvideo_demo.databinding.ActivityRecycleViewBinding
import com.mx.video.MXVideo

class RecycleViewActivity : AppCompatActivity() {
    private val binding by lazy { ActivityRecycleViewBinding.inflate(layoutInflater) }
    private val adapt = SimpleVideoAdapt()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.recycleView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycleView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
                if (itemPosition > 0) {
                    outRect.top = 10
                }
            }
        })
        adapt.list.addAll(SourceItem.all())
        binding.recycleView.adapter = adapt
    }

    override fun onBackPressed() {
        if (MXVideo.isFullScreen()) {
            MXVideo.gotoNormalScreen()
            return
        }
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        MXVideo.onStart()
    }

    override fun onStop() {
        MXVideo.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        MXVideo.releaseAll()
        super.onDestroy()
    }
}