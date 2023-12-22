package com.mx.mxvideo_demo.apps

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mx.mxvideo_demo.SourceItem
import com.mx.mxvideo_demo.databinding.ActivityTestBinding
import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScreen
import com.mx.video.player.IMXPlayer

class TestActivity : AppCompatActivity() {
    private val binding by lazy { ActivityTestBinding.inflate(layoutInflater) }
    private var playerClass: Class<out IMXPlayer>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.mxVideoStd.setOnErrorListener { source, message ->
            println("播放錯誤：$message --> $source")
        }
        binding.mxVideoStd.setOnEmptyPlayListener {
            val source = SourceItem.random16x9()
            Glide.with(this).load(source.img).into(binding.mxVideoStd.getPosterImageView())
            binding.mxVideoStd.setPlayer(playerClass)
            binding.mxVideoStd.setSource(
                MXPlaySource(
                    Uri.parse("https://media6.smartstudy.com/ae/07/3997/2/dest.m3u8"),
                    source.name,
                    isLiveSource = source.live()
                ),
                seekTo = 0
            )
            binding.mxVideoStd.startPlay()
        }

        binding.startFullPlay.setOnClickListener {
            binding.mxVideoStd.switchToScreen(MXScreen.FULL)
            Handler().postDelayed({
                binding.mxVideoStd.switchToScreen(MXScreen.NORMAL)
                binding.mxVideoStd.stopPlay()
            }, 10L * 1000)
        }

        var preTime = 0L
        var prePosition = 0
        binding.mxVideoStd.setOnPlayTicketListener { position, duration ->
            val positionDiff = position - prePosition
            val now = System.currentTimeMillis()
            if (prePosition <= 0) {
                preTime = now
                prePosition = position
                return@setOnPlayTicketListener
            }
            if (positionDiff < 15) return@setOnPlayTicketListener
            val diff = (now - preTime) / 1000f
            println("TIME -> diff = $diff  positionDiff=$positionDiff  position=$position / $duration ")
            preTime = now
            prePosition = position
        }
    }

    override fun onStart() {
        binding.mxVideoStd.onStart()
        super.onStart()
    }

    override fun onStop() {
        binding.mxVideoStd.onStop()
        super.onStop()
    }

    override fun onBackPressed() {
        if (binding.mxVideoStd.currentScreen() == MXScreen.FULL) {
            binding.mxVideoStd.switchToScreen(MXScreen.NORMAL)
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        binding.mxVideoStd.release()
        super.onDestroy()
    }
}