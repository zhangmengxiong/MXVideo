# MXVideo

#### 介绍
基于Kotlin开发的播放器，默认支持MediaPlayer播放器，可扩展IJK播放器、EXO播放器、阿里云播放器、以及任何使用TextureView的播放器, 开箱即用，欢迎提 issue 和 pull request
> 简书相关介绍（待完善）：https://www.jianshu.com/nb/50294642

最新版本：[![](https://jitpack.io/v/com.gitee.zhangmengxiong/MXVideo.svg)](https://jitpack.io/#com.gitee.zhangmengxiong/MXVideo)
```groovy
    implementation 'com.gitee.zhangmengxiong:MXVideo:1.8.5'
```

![Normal](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/1.png)
![Land Screen](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/2.png)
![Touch Seek](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/3.png)
![Pause](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/4.png)
![Rotation](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/5.png)
![Light Seek](https://gitee.com/zhangmengxiong/MXVideo/raw/master/imgs/6.png)

#### 功能特性
- 任意播放器内核（包含开源IJK、谷歌Exo、阿里云等等）
- 单例播放，只能同时播放一个节目
- 0代码集成全屏功能
- 可以调节音量、屏幕亮度
- 可以注册播放状态监听回调
- 播放器高度可以根据视频高度自动调节
- 播放器支持设置宽高比，设置宽高比后，高度固定。
- 自动保存与恢复播放进度（可关闭）
- 支持循环播放、全屏时竖屏模式、可关闭快进快退功能、可关闭全屏功能、可关闭非WiFi环境下流量提醒
- 支持播放时获取实时截屏Bitmap

##### 1、通过 dependence 引入MXVideo
```groovy
    dependencies {
	        implementation 'com.gitee.zhangmengxiong:MXVideo:x.x.x'
    }
```

##### 2、页面集成
```xml
        <com.mx.video.MXVideoStd
            android:id="@+id/mxVideoStd"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
```
```kotlin
    // Activity或者Fragment中生命周期变更，处理进入后台/前台时的暂停/续播功能
    override fun onStart() {
        mxVideoStd.onStart()
        super.onStart()
    }
    
    override fun onStop() {
        mxVideoStd.onStop()
        super.onStop()
    }
```

##### 3、开始播放
```kotlin
// 设置播放占位图
Glide.with(this).load("http://www.xxx.com/xxx.png").into(mxVideoStd.getPosterImageView())

// 默认从上一次进度播放
mxVideoStd.setSource(MXPlaySource(Uri.parse("https://aaa.bbb.com/xxx.mp4"), "标题1"))
mxVideoStd.startPlay()

// 从头开始播放
mxVideoStd.setSource(MXPlaySource(Uri.parse("https://aaa.bbb.com/xxx.mp4"), "标题1"), seekTo = 0)
mxVideoStd.startPlay()

// 从第10秒开始播放
mxVideoStd.setSource(MXPlaySource(Uri.parse("https://aaa.bbb.com/xxx.mp4"), "标题1"), seekTo = 10)
mxVideoStd.startPlay()
``` 

> MXPlaySource 可选参数说明：

| 参数   | 说明 | 默认值 |
| :----- | :--: | -------: |
| title | 标题 | "" |
| headerMap | 网络请求头部 | null |
| isLooping | 是否循环播放 | false |
| enableSaveProgress | 是否存储、读取播放进度 | true |
| isLiveSource | 是否直播源，当时直播时，不显示进度，无法快进快退暂停 | false |

##### 4、监听播放进度
```kotlin
mxVideoStd.addOnVideoListener(object : MXVideoListener() {
            // 播放状态变更
            override fun onStateChange(state: MXState) {
            }

            // 播放时间变更
            override fun onPlayTicket(position: Int, duration: Int) {
            }
        })
```

##### 5、全屏返回 + 释放资源

>  这里MXVideo默认持有当前播放的MXVideoStd，可以使用静态方法操作退出全屏、释放资源等功能。
>
>  也可以直接使用viewId：mxVideoStd.isFullScreen()，mxVideoStd.isFullScreen()，mxVideoStd.release() 等方法。
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

### 功能相关
- 切换播放器内核
```kotlin
// 默认MediaPlayer播放器，库默认内置
com.mx.video.player.MXSystemPlayer

// 谷歌的Exo播放器
com.mx.mxvideo_demo.player.exo.MXExoPlayer

// IJK播放器
com.mx.mxvideo_demo.player.MXIJKPlayer

// 阿里云播放器，仅支持 ”armeabi-v7a“、”arm64-v8a“ 两种CPU
com.mx.mxvideo_demo.player.MXAliPlayer

// 设置播放源是可以设置内核，默认 = MXSystemPlayer
mxVideoStd.setPlayer(MXSystemPlayer::class.java)
```

- 设置播放地址，标题，跳转等信息
```kotlin
mxVideoStd.setSource(MXPlaySource(Uri.parse("xxx"), title = "xxx"), seekTo = 0)
```

- 视频渲染旋转角度
```kotlin
// 默认旋转角度 = MXOrientation.DEGREE_0
mxVideoStd.setTextureOrientation(MXOrientation.DEGREE_90)
```

- 设置当前视频静音,不会影响系统音量
```kotlin
// 默认=false
mxVideoStd.setAudioMute(true)
```

- 设置播放器音量百分比，实际音量 = (volume * 系统当前音量)
```kotlin
// 默认=1f，当设置=0f时，视频则静音
// 取值范围：0f -> 1f
mxVideoStd.setVolumePercent(0.5f)
```

- 视频填充规则
```kotlin
// 强制填充宽高 MXScale.FILL_PARENT
// 根据视频大小，自适应宽高 MXScale.CENTER_CROP

// 默认填充规则 = MXScale.CENTER_CROP
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

- 进度跳转
```kotlin
// 进度单位：秒  可以在启动播放后、错误或播完之前调用
mxVideoStd.seekTo(55)
```

- 设置不能快进快退
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().canSeekByUser.set(false)
```

- 设置不能全屏
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().canFullScreen.set(false)
```

- 设置缓冲时显示网速信息
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().canShowNetSpeed.set(false)
```

- 没有设置source时不显示播放按钮
```kotlin
// 播放前设置 默认=false
mxVideoStd.getConfig().hidePlayBtnWhenNoSource.set(false)
```

- 设置全屏按钮是否显示
```kotlin
// 播放前设置 默认=true
// 全屏按钮只有在  canFullScreen=true && showFullScreenButton=true 时显示
mxVideoStd.getConfig().showFullScreenButton.set(false)
```

- 设置不显示控件右上角时间
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().canShowSystemTime.set(false)
```

- 设置不显示底部 1dp 高度的进度条
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().canShowBottomSeekBar.set(false)
```

- 设置不显示控件右上角电量图
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().canShowBatteryImg.set(false)
```

- 设置关闭WiFi环境播放前提醒
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().showTipIfNotWifi.set(false)
```

- 设置打开TextureView的水平镜像模式
```kotlin
// 播放前设置 默认=false
mxVideoStd.getConfig().mirrorMode.set(true)
```

- 设置播放完成后自动退出全屏
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().gotoNormalScreenWhenComplete.set(false)
```

- 设置播放错误后自动退出全屏
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().gotoNormalScreenWhenError.set(false)
```

- 设置播放时用户不可以暂停
```kotlin
// 播放前设置 默认=true
mxVideoStd.getConfig().canPauseByUser.set(false)
```

- 设置播放时如果手机横屏则自动进入全屏播放
```kotlin
// 播放前设置 默认=false
mxVideoStd.getConfig().autoFullScreenBySensor.set(true)
```

- 设置全屏播放时屏幕方向自动跟随重力方向
```kotlin
// 播放前设置 默认=MXSensorMode.SENSOR_FIT_VIDEO
// MXSensorMode.SENSOR_AUTO = 跟随重力方向
// MXSensorMode.SENSOR_FIT_VIDEO = 跟随视频宽高自动旋转 0 或 180 度
// MXSensorMode.SENSOR_NO = 根据视频宽高比固定横屏/竖屏，横屏 = 视频宽>=高   --   竖屏 = 视频宽<高
mxVideoStd.getConfig().fullScreenSensorMode.set(MXSensorMode.SENSOR_AUTO)
```

- 设置直播流播放错误时自动重试
```kotlin
// 播放前设置 默认=false
mxVideoStd.getConfig().replayLiveSourceWhenError.set(true)
```

- 设置播放器内控件动画效果
```kotlin
// 将动画时长设置为 <=0 时，禁止动画效果
mxVideoStd.getConfig().animatorDuration.set(0L)

// 默认时长200毫秒
mxVideoStd.getConfig().animatorDuration.set(200L)
```

- 播放时截屏
```kotlin
if (mxVideoStd.isPlaying()) {
    val bitmap: Bitmap? = mxVideoStd.getTextureView()?.bitmap
    screenCapImg.setImageBitmap(bitmap)
}
```


