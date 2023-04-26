package com.mx.mxvideo_demo.player.exo

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource.RawResourceDataSourceException
import com.google.android.exoplayer2.util.Util

internal object ExoSourceBuild {
    private const val TYPE_RTMP = 14
    private const val sHttpReadTimeout = 60 * 1000
    private const val sHttpConnectTimeout = 60 * 1000
    private const val isForceRtspTcp = true

    private fun inferContentType(fileName: String): Int {
        return if (fileName.startsWith("rtmp:", ignoreCase = true)) {
            TYPE_RTMP
        } else {
            Util.inferContentType(Uri.parse(fileName), null)
        }
    }

    /**
     * @param url 链接
     * @param preview 是否带上header，默认有header自动设置为true
     */
    fun build(
        context: Context, headData: Map<String, String>?,
        url: String, preview: Boolean
    ): MediaSource {
        val contentUri = Uri.parse(url)
        val mediaItem = MediaItem.fromUri(contentUri)
        val contentType = inferContentType(url)
        var uerAgent: String? = null
        if (headData != null) {
            uerAgent = headData["User-Agent"]
        }
        if ("android.resource" == contentUri.scheme) {
            val factory = DataSource.Factory {
                val dataSpec = DataSpec(contentUri)
                val rawResourceDataSource = RawResourceDataSource(context)
                try {
                    rawResourceDataSource.open(dataSpec)
                } catch (e: RawResourceDataSourceException) {
                    e.printStackTrace()
                }
                rawResourceDataSource
            }
            return ProgressiveMediaSource.Factory(
                factory
            ).createMediaSource(mediaItem)
        }
        return when (contentType) {
            C.CONTENT_TYPE_SS -> SsMediaSource.Factory(
                DefaultSsChunkSource.Factory(getDataSourceFactory(context, preview, headData)),
                DefaultDataSource.Factory(
                    context,
                    getHttpDataSourceFactory(context, preview, headData)
                )
            ).createMediaSource(mediaItem)

            C.CONTENT_TYPE_RTSP -> {
                val rtspFactory = RtspMediaSource.Factory()
                if (uerAgent != null) {
                    rtspFactory.setUserAgent(uerAgent)
                }
                if (sHttpConnectTimeout > 0) {
                    rtspFactory.setTimeoutMs(sHttpConnectTimeout.toLong())
                }
                rtspFactory.setForceUseRtpTcp(isForceRtspTcp)
                rtspFactory.createMediaSource(mediaItem)
            }

            C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(
                DefaultDashChunkSource.Factory(getDataSourceFactory(context, preview, headData)),
                DefaultDataSource.Factory(
                    context,
                    getHttpDataSourceFactory(context, preview, headData)
                )
            ).createMediaSource(mediaItem)

            C.CONTENT_TYPE_HLS -> {
                HlsMediaSource.Factory(
                    getDataSourceFactory(context, preview, headData)
                ).setAllowChunklessPreparation(true).createMediaSource(mediaItem)
            }

            TYPE_RTMP -> {
                val rtmpDataSourceFactory = RtmpDataSource.Factory()
                ProgressiveMediaSource.Factory(
                    rtmpDataSourceFactory,
                    DefaultExtractorsFactory()
                )
                    .createMediaSource(mediaItem)
            }

            C.CONTENT_TYPE_OTHER -> ProgressiveMediaSource.Factory(
                getDataSourceFactory(context, preview, headData), DefaultExtractorsFactory()
            )
                .createMediaSource(mediaItem)

            else -> ProgressiveMediaSource.Factory(
                getDataSourceFactory(context, preview, headData), DefaultExtractorsFactory()
            ).createMediaSource(mediaItem)
        }
    }

    /**
     * 获取SourceFactory
     */
    private fun getDataSourceFactory(
        context: Context,
        preview: Boolean,
        headData: Map<String, String>?,
    ): DataSource.Factory {
        val factory = DefaultDataSource.Factory(
            context,
            getHttpDataSourceFactory(context, preview, headData)
        )
        if (preview) {
            factory.setTransferListener(DefaultBandwidthMeter.Builder(context).build())
        }
        return factory
    }

    private fun getHttpDataSourceFactory(
        context: Context,
        preview: Boolean,
        headData: Map<String, String>?,
    ): DataSource.Factory {
        var connectTimeout = DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS
        var readTimeout = DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS
        if (sHttpConnectTimeout > 0) {
            connectTimeout = sHttpConnectTimeout
        }
        if (sHttpReadTimeout > 0) {
            readTimeout = sHttpReadTimeout
        }
        var allowCrossProtocolRedirects = false
        if (headData != null && headData.isNotEmpty()) {
            allowCrossProtocolRedirects = "true" == headData["allowCrossProtocolRedirects"]
        }
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(allowCrossProtocolRedirects)
            .setConnectTimeoutMs(connectTimeout)
            .setReadTimeoutMs(readTimeout)
            .setTransferListener(
                if (preview) null else DefaultBandwidthMeter.Builder(context).build()
            )
        if (headData != null && headData.isNotEmpty()) {
            dataSourceFactory.setDefaultRequestProperties(headData)
        }
        return dataSourceFactory
    }
}