# MXVideo

#### 介绍
基于饺子播放器、kotlin编写的开源播放器
最新版本：[![](https://jitpack.io/v/com.gitee.zhangmengxiong/MXVideo.svg)](https://jitpack.io/#com.gitee.zhangmengxiong/MXVideo)

#### 功能特性
- 1、单例播放，只能同时播放一个节目
- 2、0代码集成全屏功能
- 3、可以调节音量、屏幕亮度
- 4、可以注册播放状态监听回调
- 5、播放器高度可以根据视频高度自动调节
- 6、播放器支持设置宽高比，设置宽高比后，高度固定。
- 7、自动保存与恢复播放进度（可关闭）
- 7、支持循环播放、全屏时竖屏模式、可关闭快进快退功能、可关闭全屏功能、可关闭非WiFi环境下流量提醒

##### 1、通过 dependence 引入MXVideo
```
    dependencies {
	        implementation 'com.gitee.zhangmengxiong:MXVideo:1.0.0'
    }
```

##### 2、页面集成
```
        <com.mx.video.MXVideoStd
            android:id="@+id/mxVideoStd"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
```

##### 3、开始播放
```
// 设置播放占位图
Glide.with(this).load(thumbnails.random()).into(mxVideoStd.getPosterImageView())

mxVideoStd.setSource(MXPlaySource(Uri.parse("https://aaa.bbb.com/xxx.mp4"), "标题1"))
mxVideoStd.startPlay()

```

##### 4、监听播放进度
```
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
```
    // 这里MXVideo默认持有当前播放的MXVideoStd，可以使用静态方法操作退出全屏、释放资源等功能。
    // 也可以直接使用viewId：mxVideoStd.isFullScreen()，mxVideoStd.isFullScreen()，mxVideoStd.release() 等方法。


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