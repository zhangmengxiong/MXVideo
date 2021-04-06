package com.mx.mxvideo_demo.apps

import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mx.mxvideo_demo.R
import com.mx.mxvideo_demo.adapts.SimpleVideoAdapt
import com.mx.video.MXVideo
import kotlinx.android.synthetic.main.activity_main.*

class RecycleViewActivity : AppCompatActivity() {
    private val adapt = SimpleVideoAdapt()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycle_view)

        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
                if (itemPosition > 0) {
                    outRect.top = 10
                }
            }
        })
        recycleView.adapter = adapt
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