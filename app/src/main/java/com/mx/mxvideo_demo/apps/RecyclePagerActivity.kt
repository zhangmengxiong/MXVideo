package com.mx.mxvideo_demo.apps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.mx.mxvideo_demo.SourceItem
import com.mx.mxvideo_demo.adapts.PagerVideoAdapt
import com.mx.mxvideo_demo.databinding.ActivityRecycleViewBinding
import com.mx.mxvideo_demo.databinding.AdaptFullVideoItemBinding
import com.mx.video.MXVideo

class RecyclePagerActivity : AppCompatActivity() {
    private val binding by lazy { ActivityRecycleViewBinding.inflate(layoutInflater) }
    private val manager by lazy {
        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
    private val adapt = PagerVideoAdapt()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        PagerSnapHelper().attachToRecyclerView(binding.recycleView)
        binding.recycleView.layoutManager = manager
        adapt.list.addAll(SourceItem.all().shuffled())
        binding.recycleView.adapter = adapt
        binding.recycleView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                val index = manager.findFirstCompletelyVisibleItemPosition()
                val item = manager.findViewByPosition(index) ?: return
                val binding = AdaptFullVideoItemBinding.bind(item)
                if (!binding.mxVideoStd.isPlaying()) {
                    binding.mxVideoStd.startPlay()
                }
            }
        })
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