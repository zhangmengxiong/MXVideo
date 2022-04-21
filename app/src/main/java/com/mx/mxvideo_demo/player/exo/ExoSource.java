package com.mx.mxvideo_demo.player.exo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.common.base.Ascii;

import java.util.Map;

public class ExoSource {
    public static final int TYPE_RTMP = 14;

    private static final int sHttpReadTimeout = 60 * 1000;

    private static final int sHttpConnectTimeout = 60 * 1000;

    private static final boolean isForceRtspTcp = true;

    private Context mAppContext;

    private Map<String, String> mMapHeadData;

    ExoSource(Context context, Map<String, String> mapHeadData) {
        mAppContext = context.getApplicationContext();
        mMapHeadData = mapHeadData;
    }

    /**
     * @param dataSource 链接
     * @param preview    是否带上header，默认有header自动设置为true
     */
    public MediaSource getMediaSource(String dataSource, boolean preview, @Nullable String overrideExtension) {
        MediaSource mediaSource = null;
        final Uri contentUri = Uri.parse(dataSource);
        MediaItem mediaItem = MediaItem.fromUri(contentUri);
        int contentType = inferContentType(dataSource, overrideExtension);

        String uerAgent = null;
        if (mMapHeadData != null) {
            uerAgent = mMapHeadData.get("User-Agent");
        }
        if ("android.resource".equals(contentUri.getScheme())) {
            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    DataSpec dataSpec = new DataSpec(contentUri);
                    final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(mAppContext);
                    try {
                        rawResourceDataSource.open(dataSpec);
                    } catch (RawResourceDataSource.RawResourceDataSourceException e) {
                        e.printStackTrace();
                    }
                    return rawResourceDataSource;
                }
            };
            return new ProgressiveMediaSource.Factory(
                    factory).createMediaSource(mediaItem);

        }

        switch (contentType) {
            case C.TYPE_SS:
                mediaSource = new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(getDataSourceFactory(mAppContext, preview, uerAgent)),
                        new DefaultDataSource.Factory(mAppContext,
                                getHttpDataSourceFactory(mAppContext, preview, uerAgent))).createMediaSource(mediaItem);
                break;

            case C.TYPE_RTSP:
                RtspMediaSource.Factory rtspFactory = new RtspMediaSource.Factory();
                if (uerAgent != null) {
                    rtspFactory.setUserAgent(uerAgent);
                }
                if (sHttpConnectTimeout > 0) {
                    rtspFactory.setTimeoutMs(sHttpConnectTimeout);
                }
                rtspFactory.setForceUseRtpTcp(isForceRtspTcp);
                mediaSource = rtspFactory.createMediaSource(mediaItem);
                break;

            case C.TYPE_DASH:
                mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(getDataSourceFactory(mAppContext, preview, uerAgent)),
                        new DefaultDataSource.Factory(mAppContext,
                                getHttpDataSourceFactory(mAppContext, preview, uerAgent))).createMediaSource(mediaItem);
                break;
            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource.Factory(getDataSourceFactory(mAppContext, preview, uerAgent))
                        .setAllowChunklessPreparation(true)
                        .createMediaSource(mediaItem);
                break;
            case TYPE_RTMP:
                RtmpDataSource.Factory rtmpDataSourceFactory = new RtmpDataSource.Factory();
                mediaSource = new ProgressiveMediaSource.Factory(rtmpDataSourceFactory,
                        new DefaultExtractorsFactory())
                        .createMediaSource(mediaItem);
                break;
            case C.TYPE_OTHER:
            default:
                mediaSource = new ProgressiveMediaSource.Factory(getDataSourceFactory(mAppContext,
                        preview, uerAgent), new DefaultExtractorsFactory())
                        .createMediaSource(mediaItem);
                break;
        }
        return mediaSource;
    }

    /**
     * 获取SourceFactory
     */
    private DataSource.Factory getDataSourceFactory(Context context, boolean preview, String uerAgent) {
        DefaultDataSource.Factory factory = new DefaultDataSource.Factory(context,
                getHttpDataSourceFactory(context, preview, uerAgent));
        if (preview) {
            factory.setTransferListener(new DefaultBandwidthMeter.Builder(context).build());
        }
        return factory;
    }

    @SuppressLint("WrongConstant")
    @C.ContentType
    public static int inferContentType(String fileName, @Nullable String overrideExtension) {
        fileName = Ascii.toLowerCase(fileName);
        if (fileName.startsWith("rtmp:")) {
            return TYPE_RTMP;
        } else {
            return inferContentType(Uri.parse(fileName), overrideExtension);
        }
    }

    @C.ContentType
    public static int inferContentType(Uri uri, @Nullable String overrideExtension) {
        return Util.inferContentType(uri, overrideExtension);
    }


    private DataSource.Factory getHttpDataSourceFactory(Context context, boolean preview, String uerAgent) {
        int connectTimeout = DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS;
        int readTimeout = DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS;
        if (sHttpConnectTimeout > 0) {
            connectTimeout = sHttpConnectTimeout;
        }
        if (sHttpReadTimeout > 0) {
            readTimeout = sHttpReadTimeout;
        }
        boolean allowCrossProtocolRedirects = false;
        if (mMapHeadData != null && mMapHeadData.size() > 0) {
            allowCrossProtocolRedirects = "true".equals(mMapHeadData.get("allowCrossProtocolRedirects"));
        }
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(allowCrossProtocolRedirects)
                .setConnectTimeoutMs(connectTimeout)
                .setReadTimeoutMs(readTimeout)
                .setTransferListener(preview ? null : new DefaultBandwidthMeter.Builder(context).build());
        if (mMapHeadData != null && mMapHeadData.size() > 0) {
            ((DefaultHttpDataSource.Factory) dataSourceFactory).setDefaultRequestProperties(mMapHeadData);
        }
        return dataSourceFactory;
    }

}
