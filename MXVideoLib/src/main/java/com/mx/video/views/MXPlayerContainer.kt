package com.mx.video.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.RelativeLayout
import com.mx.video.utils.MXUtils
import kotlin.math.roundToInt

class MXPlayerContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {
    private var ratioWidth: Float = 0.0f
    private var ratioHeight: Float = 0.0f
    private var fillBy = WIDTH

    init {
        gravity = Gravity.CENTER
    }

    /**
     * 填充占位
     */
    fun startFill() {
        ratioWidth = width.toFloat()
        ratioHeight = height.toFloat()
        fillBy = WIDTH
        MXUtils.log("setRatio  $width x $height")
        postInvalidate()
    }

    /**
     * 取消填充占位
     */
    fun endFill() {
        ratioWidth = 0.0f
        ratioHeight = 0.0f
        fillBy = WIDTH
        postInvalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (ratioWidth > 0f && ratioHeight > 0f) {
            //获取宽度的模式和尺寸
            if (fillBy == WIDTH) {
                val widthSize = MeasureSpec.getSize(widthMeasureSpec)
                val heightSize =
                    ((widthSize * ratioHeight) / ratioWidth).roundToInt()//根据宽度和比例计算高度
                val heightMeasureSpec1 =
                    MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
                setMeasuredDimension(widthMeasureSpec, heightMeasureSpec1)
            } else {
                val heightSize = MeasureSpec.getSize(heightMeasureSpec)
                val widthSize =
                    ((heightSize * ratioWidth) / ratioHeight).roundToInt()//根据宽度和比例计算高度
                val widthMeasureSpec1 = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
                setMeasuredDimension(widthMeasureSpec1, heightSize)
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    companion object {
        const val WIDTH = "width"
        const val HEIGHT = "height"
    }
}