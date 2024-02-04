package com.mx.mxvideo_demo.player.vlc;

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.multidex.BuildConfig
import com.mx.mxvideo_demo.MyApp
import com.mx.mxvideo_demo.R
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.util.AndroidUtil
import org.videolan.libvlc.util.HWDecoderUtil
import org.videolan.libvlc.util.VLCUtil
import java.io.File
import java.util.Collections

object VlcOptions {

    private const val AOUT_AUDIOTRACK = 1
    private const val AOUT_OPENSLES = 2

    private const val HW_ACCELERATION_AUTOMATIC = -1
    private const val HW_ACCELERATION_DISABLED = 0
    private const val HW_ACCELERATION_DECODING = 1
    private const val HW_ACCELERATION_FULL = 2
    private var audiotrackSessionId = 0

    fun getLibOptions(context: Context): ArrayList<String> {
        val pref = Settings.getInstance(context)
        if (audiotrackSessionId == 0) {
            val audioManager = context.getSystemService<AudioManager>()!!
            audiotrackSessionId = audioManager.generateAudioSessionId()
        }

        val options = ArrayList<String>(50)

        val timeStreching = pref.getBoolean("enable_time_stretching_audio", true)
        val subtitlesEncoding = pref.getString("subtitle_text_encoding", "") ?: ""
        val frameSkip = pref.getBoolean("enable_frame_skip", false)
        val verboseMode = pref.getBoolean("enable_verbose_mode", true)

        var deblocking = -1
        try {
            deblocking = getDeblocking(Integer.parseInt(pref.getString("deblocking", "-1")!!))
        } catch (ignored: NumberFormatException) {
        }

        val networkCaching = pref.getInt("network_caching_value", 600).coerceIn(0, 60000)
        val freetypeRelFontsize = pref.getString("subtitles_size", "16")
        val freetypeBold = pref.getBoolean("subtitles_bold", false)

        val freetypeColor = Integer.decode(
            String.format(
                "0x%06X",
                (0xFFFFFF and pref.getInt("subtitles_color", 16777215))
            )
        )
        val freetypeColorOpacity = pref.getInt("subtitles_color_opacity", 255)

        val freetypeBackgroundColor = Integer.decode(
            String.format(
                "0x%06X",
                (0xFFFFFF and pref.getInt("subtitles_background_color", 16777215))
            )
        )
        val freetypeBackgroundColorOpacity =
            pref.getInt("subtitles_background_color_opacity", 255)
        val freetypeBackgroundEnabled = pref.getBoolean("subtitles_background", false)

        val freetypeOutlineEnabled = pref.getBoolean("subtitles_outline", true)
        val freetypeOutlineSize = pref.getString("subtitles_outline_size", "4")
        val freetypeOutlineColor = Integer.decode(
            String.format(
                "0x%06X",
                (0xFFFFFF and pref.getInt("subtitles_outline_color", 0))
            )
        )
        val freetypeOutlineOpacity = pref.getInt("subtitles_outline_color_opacity", 255)

        val freetypeShadowEnabled = pref.getBoolean("subtitles_shadow", true)
        val freetypeShadowColor = Integer.decode(
            String.format(
                "0x%06X",
                (0xFFFFFF and pref.getInt(
                    "subtitles_shadow_color",
                    ContextCompat.getColor(context, R.color.black)
                ))
            )
        )
        val freetypeShadowOpacity = pref.getInt("subtitles_shadow_color_opacity", 128)

        val opengl = Integer.parseInt(pref.getString("opengl", "-1")!!)
        options.add(if (timeStreching) "--audio-time-stretch" else "--no-audio-time-stretch")
        options.add("--avcodec-skiploopfilter")
        options.add("" + deblocking)
        options.add("--avcodec-skip-frame")
        options.add(if (frameSkip) "2" else "0")
        options.add("--avcodec-skip-idct")
        options.add(if (frameSkip) "2" else "0")
        options.add("--subsdec-encoding")
        options.add(subtitlesEncoding)
        options.add("--stats")
        if (networkCaching > 0) options.add("--network-caching=$networkCaching")
        options.add("--android-display-chroma")
        //options.add("--audio-resampler")
        //options.add("soxr")

        options.add("--audiotrack-session-id=$audiotrackSessionId")

        options.add("--freetype-rel-fontsize=" + freetypeRelFontsize!!)
        if (freetypeBold) options.add("--freetype-bold")
        options.add("--freetype-color=$freetypeColor")
        options.add("--freetype-opacity=$freetypeColorOpacity")

        if (freetypeBackgroundEnabled) {
            options.add("--freetype-background-color=$freetypeBackgroundColor")
            options.add("--freetype-background-opacity=$freetypeBackgroundColorOpacity")
        } else options.add("--freetype-background-opacity=0")

        if (freetypeShadowEnabled) {
            options.add("--freetype-shadow-color=$freetypeShadowColor")
            options.add("--freetype-shadow-opacity=$freetypeShadowOpacity")
        } else options.add("--freetype-shadow-opacity=0")

        if (freetypeOutlineEnabled) {
            options.add("--freetype-outline-thickness=$freetypeOutlineSize")
            options.add("--freetype-outline-color=$freetypeOutlineColor")
            options.add("--freetype-outline-opacity=$freetypeOutlineOpacity")
        } else options.add("--freetype-outline-opacity=0")


        if (opengl == 1) options.add("--vout=gles2,none")
        else if (opengl == 0) options.add("--vout=android_display,none")
        else options.add("--vout=android_display,none")

        options.add("--keystore")
        options.add(if (AndroidUtil.isMarshMallowOrLater) "file_crypt,none" else "file_plaintext,none")
        options.add("--keystore-file")
        options.add(File(context.getDir("keystore", Context.MODE_PRIVATE), "file").absolutePath)
        options.add(if (verboseMode) "-vv" else "-v")
        // fixme comment temporarily
        if (pref.getBoolean("casting_passthrough", false))
            options.add("--sout-chromecast-audio-passthrough")
        else
            options.add("--no-sout-chromecast-audio-passthrough")
        options.add(
            "--sout-chromecast-conversion-quality=" + pref.getString(
                "casting_quality",
                "2"
            )!!
        )
        options.add("--sout-keep")

        val customOptions = pref.getString("custom_libvlc_options", null)
        if (!customOptions.isNullOrEmpty()) {
            val optionsArray = customOptions.split("\\r?\\n".toRegex()).toTypedArray()
            if (!optionsArray.isNullOrEmpty()) Collections.addAll(options, *optionsArray)
        }
        if (pref.getBoolean("prefer_smbv1", true))
            options.add("--smb-force-v1")
        //Ambisonic
        val hstfDir = context.getDir("vlc", Context.MODE_PRIVATE)
        val hstfPath =
            "${hstfDir.absolutePath}/.share/hrtfs/dodeca_and_7channel_3DSL_HRTF.sofa"
        options.add("--spatialaudio-headphones")
        options.add("--hrtf-file")
        options.add(hstfPath)
        if (pref.getBoolean("audio-replay-gain-enable", false)) {
            options.add(
                "--audio-replay-gain-mode=${
                    pref.getString(
                        "audio-replay-gain-mode",
                        "track"
                    )
                }"
            )
            options.add(
                "--audio-replay-gain-preamp=${
                    pref.getString(
                        "audio-replay-gain-preamp",
                        "0.0"
                    )
                }"
            )
            options.add(
                "--audio-replay-gain-default=${
                    pref.getString(
                        "audio-replay-gain-default",
                        "-7.0"
                    )
                }"
            )
            if (pref.getBoolean("audio-replay-gain-peak-protection", true))
                options.add("--audio-replay-gain-peak-protection")
            else
                options.add("--no-audio-replay-gain-peak-protection")
        }

        options.add("--preferred-resolution=${pref.getString("preferred_resolution", "-1")!!}")
        //options.add("--http-user-agent=" + RequestConfig.USER_AGENT + "")
        //options.add("--http-referrer=" + RequestConfig.REFERER + "")
        if (BuildConfig.DEBUG) {
//				XLog.d("VLC Options: " + options.joinToString(" "))
        }
        return options
    }

    fun dp2px(context: Context, dp: Float): Float = dp * context.resources.displayMetrics.density

    fun px2dp(context: Context, px: Float): Float = px / context.resources.displayMetrics.density

    fun getAout(context: Context): String? {
        var aout = -1
        try {
            val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            aout = Integer.parseInt(pref.getString("aout", "-1")!!)
        } catch (ignored: NumberFormatException) {
        }

        val hwaout = HWDecoderUtil.getAudioOutputFromDevice()
        if (hwaout == HWDecoderUtil.AudioOutput.OPENSLES)
            aout = AOUT_OPENSLES

        return if (aout == AOUT_OPENSLES) "opensles" else if (aout == AOUT_AUDIOTRACK) "audiotrack" else null /* aaudio is the default */
    }

    private fun getDeblocking(deblocking: Int): Int {
        var ret = deblocking
        if (deblocking < 0) {
            /**
             * Set some reasonable deblocking defaults:
             *
             * Skip all (4) for armv6 and MIPS by default
             * Skip non-ref (1) for all armv7 more than 1.2 Ghz and more than 2 cores
             * Skip non-key (3) for all devices that don't meet anything above
             */
            val m = VLCUtil.getMachineSpecs() ?: return ret
            if (m.hasArmV6 && !m.hasArmV7 || m.hasMips)
                ret = 4
            else if (m.frequency >= 1200 && m.processors > 2)
                ret = 1
            else if (m.bogoMIPS >= 1200 && m.processors > 2) {
                ret = 1
            } else
                ret = 3
        } else if (deblocking > 4) { // sanity check
            ret = 3
        }
        return ret
    }

    fun setMediaOptions(media: IMedia, context: Context) {
        var hardwareAcceleration = HW_ACCELERATION_DISABLED
        val prefs = Settings.getInstance(context)
        try {
            hardwareAcceleration = Integer.parseInt(
                prefs.getString(
                    "hardware_acceleration",
                    "$HW_ACCELERATION_AUTOMATIC"
                )!!
            )
        } catch (ignored: NumberFormatException) {
        }
        if (hardwareAcceleration == HW_ACCELERATION_DISABLED)
            media.setHWDecoderEnabled(false, false)
        else if (hardwareAcceleration == HW_ACCELERATION_FULL || hardwareAcceleration == HW_ACCELERATION_DECODING) {
            media.setHWDecoderEnabled(true, true)
            if (hardwareAcceleration == HW_ACCELERATION_DECODING) {
                media.addOption(":no-mediacodec-dr")
                media.addOption(":no-omxil-dr")
            }
        }
    }

}

