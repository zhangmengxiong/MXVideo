# MXVideo

#### 介绍
基于饺子播放器、kotlin编写的开源播放器
最新版本：[![](https://jitpack.io/v/com.gitee.zhangmengxiong/MXVideo.svg)](https://jitpack.io/#com.gitee.zhangmengxiong/MXVideo)

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