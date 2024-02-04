# MXVideo

#### Introduce
The player developed based on Kotlin supports MediaPlayer by default, and can be extended with VLC player, IJK player, EXO player, Alibaba Cloud player, and any player that uses TextureView
> Introduction to the book (to be completed)：https://www.jianshu.com/nb/50294642

The latest version：[![](https://jitpack.io/v/zhangmengxiong/MXVideo.svg)](https://jitpack.io/#zhangmengxiong/MXVideo)
```groovy
    implementation 'com.github.zhangmengxiong:MXVideo:1.9.1'
```

![Normal](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/1.png)
![Land Screen](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/2.png)
![Touch Seek](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/3.png)
![Pause](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/4.png)
![Rotation](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/5.png)
![Light Seek](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/6.png)


#### Features
- Any player kernel (including open-source IJK, Google Exo, Alibaba Cloud, etc.)
- Singleton playback, which can only play one program at the same time
- 0 code integrated full-screen functionality
- You can adjust the volume and screen brightness
- You can register playback status monitoring callbacks
- The player height can be automatically adjusted according to the height of the video
- The player supports setting the aspect ratio, and the height is fixed after setting the aspect ratio.
- Automatically save and resume playback progress (can be turned off)
- Supports loop playback, portrait mode when full screen, fast forward and rewind function can be disabled, full screen function can be disabled, and traffic alerts can be turned off in non-WiFi environments
- Support to get real-time screenshot bitmap during playback

##### 1、dependence
```groovy
    dependencies {
        implementation 'com.github.zhangmengxiong:MXVideo:x.x.x'
    }
```

##### 2、Page integrations
```xml
        <com.mx.video.MXVideoStd
            android:id="@+id/mxVideoStd"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
```
```kotlin
    // The lifecycle of an activity or fragment is changed, and the pause/resume function is handled when entering the background/foreground
    override fun onStart() {
        mxVideoStd.onStart()
        super.onStart()
    }
    
    override fun onStop() {
        mxVideoStd.onStop()
        super.onStop()
    }
```

##### 3、Start playing
```kotlin
// Set up a playback placeholder
Glide.with(this).load("http://www.xxx.com/xxx.png").into(mxVideoStd.getPosterImageView())

// By default, it is played from the last progress
mxVideoStd.setSource(MXPlaySource(Uri.parse("https://aaa.bbb.com/xxx.mp4"), "标题1"))
mxVideoStd.startPlay()

// Play from the beginning
mxVideoStd.setSource(MXPlaySource(Uri.parse("https://aaa.bbb.com/xxx.mp4"), "标题1"), seekTo = 0)
mxVideoStd.startPlay()

// Play from the 10th second
mxVideoStd.setSource(MXPlaySource(Uri.parse("https://aaa.bbb.com/xxx.mp4"), "标题1"), seekTo = 10)
mxVideoStd.startPlay()
``` 

> MXPlaySource 可选参数说明：

| Parameter | Description | Default value |
| :----- | :--: | -------: |
| title | Title | "" |
| headerMap | Network request header | null |
| isLooping | Whether to loop | false |
| enableSaveProgress | Whether to store and read playback progress | true |
| isLiveSource | Whether it is a live source, when it is live at that time, the progress is not displayed, and it cannot be fast-forwarded and fast-forwarded and paused | false |

##### 4、Monitor playback progress
```kotlin
mxVideoStd.addOnVideoListener(object : MXVideoListener() {
            // Playback status changes
            override fun onStateChange(state: MXState) {
            }

            // Playback time changes
            override fun onPlayTicket(position: Int, duration: Int) {
            }
        })
```

##### 5、Full screen return + release resources

>  Here, MXVideo holds the currently playing MXVideoStd by default, and can use static methods to operate functions such as exiting the full screen and releasing resources.
>
>  You can also use the methods of Vivid: Max Verder, Max Fals, Max Verder, Max Verder, Max Velis, etc.
```kotlin
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
```

### Function-related
- Switch the player kernel
```kotlin
// The default Mediaplayer player is built-in by default
com.mx.video.player.MXSystemPlayer

// Google's Exo Player
com.mx.mxvideo_demo.player.exo.MXExoPlayer

// VLC Player
com.mx.mxvideo_demo.player.vlc.MXVLCPlayer

// IJK Player
com.mx.mxvideo_demo.player.MXIJKPlayer

// Alibaba Cloud Player supports only armeabi-v7a and arm64-v8a CPUs
com.mx.mxvideo_demo.player.MXAliPlayer

// Setting the playback source is possible to set the kernel, default = MXSystemPlayer
mxVideoStd.setPlayer(MXSystemPlayer::class.java)
```

- Set the playback address, title, jump and other information
```kotlin
mxVideoStd.setSource(MXPlaySource(Uri.parse("xxx"), title = "xxx"), seekTo = 0)
```

- Video rendering rotation angle
```kotlin
// default angle = MXOrientation.DEGREE_0
mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_90)
```

- Set the current video to mute without affecting the system volume
```kotlin
// default value = false
mxVideoStd.setAudioMute(true)
```

- Set the player volume percentage, actual volume = (volume * current system volume)
```kotlin
// Default=1f, when setting=0f, the video is muted
// Valid values: 0f - > 1f 
mxVideoStd.setVolumePercent(0.5f)
```

- Video filling rules
```kotlin
// Force the fill of the width and height MXScale.FILL_PARENT
// Depending on the size of the video, the adaptive width and height MXScale.CENTER_CROP

// Default Fill Rule = MXScale.CENTER_CROP

mxVideoStd.setScaleType(MXScale.CENTER_CROP)
```

- MXVideoStd 控件宽高约束
> 在页面xml中添加，layout_width一般设置match_parent，高度wrap_content
```xml 
    <com.mx.video.MXVideoStd
        android:id="@+id/mxVideoStd"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
```
> 可以设置任意宽高比，如果设置宽高比，则控件高度需要设置android:layout_height="wrap_content"，否则不生效。 
>
> 当取消约束、MXVideo高度自适应、填充规则=MXScale.CENTER_CROP时，控件高度会自动根据视频宽高自动填充高度 
```kotlin
// MXVideoStd控件设置宽高比= 16：9
mxVideoStd.setDimensionRatio(16.0 / 9.0)

// MXVideoStd控件设置宽高比= 4：3
mxVideoStd.setDimensionRatio(4.0 / 3.0)

// 取消约束
mxVideoStd.setDimensionRatio(0.0)
```

- Progress Seek
```kotlin
// Progress Units: Seconds can be invoked after playback is initiated, after an error or before playback ends
mxVideoStd.seekTo(55)
```

- Settings cannot be fast-forwarded or rewinded
```kotlin
mxVideoStd.getConfig().canSeekByUser.set(false)
```

- Settings can't be full-screen
```kotlin 
mxVideoStd.getConfig().canFullScreen.set(false)
```

- Displays network speed information when buffering is set
```kotlin 
mxVideoStd.getConfig().canShowNetSpeed.set(false)
```

- The play button is not displayed when the source is not set
```kotlin 
mxVideoStd.getConfig().hidePlayBtnWhenNoSource.set(false)
```

- Set whether the full-screen button is displayed
```kotlin
// default setting =true
// The full-screen button is only displayed when canFullScreen=true & showFullScreenButton=true
mxVideoStd.getConfig().showFullScreenButton.set(false)
```

- Set the time when the upper right corner of the control is not displayed
```kotlin 
mxVideoStd.getConfig().canShowSystemTime.set(false)
```

- The setting does not show a progress bar at the bottom 1dp height
```kotlin 
mxVideoStd.getConfig().canShowBottomSeekBar.set(false)
```

- Set not to display the power graph in the upper right corner of the control
```kotlin 
mxVideoStd.getConfig().canShowBatteryImg.set(false)
```

- Set a reminder before turning off the WiFi environment
```kotlin 
mxVideoStd.getConfig().showTipIfNotWifi.set(false)
```

- Sets the horizontal mirroring mode to turn on the TextureView
```kotlin 
mxVideoStd.getConfig().mirrorMode.set(true)
```

- Set to automatically exit full screen when playback is complete
```kotlin 
mxVideoStd.getConfig().gotoNormalScreenWhenComplete.set(false)
```

- Set to automatically exit full screen after a playback error
```kotlin 
mxVideoStd.getConfig().gotoNormalScreenWhenError.set(false)
```

- The user cannot pause while setting playback
```kotlin 
mxVideoStd.getConfig().canPauseByUser.set(false)
```

- If the phone is horizontal, it will automatically enter full-screen playback when setting playback
```kotlin 
mxVideoStd.getConfig().autoFullScreenBySensor.set(true)
```

- Set the screen orientation to automatically follow the gravity direction when playing in full screen
```kotlin
//    Pre-playback settings: Default=MXSensorMode.SENSOR_FIT_VIDEO
//    MXSensorMode.SENSOR_AUTO = Follow the direction of gravity
//    MXSensorMode.SENSOR_FIT_VIDEO = Automatically rotates 0 or 180 degrees to follow the width and height of the video
//    MXSensorMode.SENSOR_NO = Fixed landscape or portrait based on the video aspect ratio, landscape = (video width > = height) -- Portrait = (video width < height)
mxVideoStd.getConfig().fullScreenSensorMode.set(MXSensorMode.SENSOR_AUTO)
```

- Set up automatic retries when a live stream plays incorrectly
```kotlin 
mxVideoStd.getConfig().replayLiveSourceWhenError.set(true)
```

- Animate in-player controls
```kotlin  
mxVideoStd.getConfig().animatorDuration.set(200L)
```

- Take a screenshot while playing
```kotlin
if (mxVideoStd.isPlaying()) {
    val bitmap: Bitmap? = mxVideoStd.getTextureView()?.bitmap
    screenCapImg.setImageBitmap(bitmap)
}
```

- When the screen is not full, the settings support sliding to fast forward and rewind, volume adjustment, and brightness adjustment functions
```kotlin 
config.enableTouchWhenNormalScreen.set(true)
```

- Playback multiplier settings
```kotlin
// default = 1.0
config.playSpeed.set(1.0)
```
