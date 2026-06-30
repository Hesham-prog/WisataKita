package com.wisatakita.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.max
import kotlin.random.Random

class RainOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class Drop(
        val xRatio: Float,
        val yRatio: Float,
        val length: Float,
        val speed: Float,
        val alpha: Int
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 2.2f
        strokeCap = Paint.Cap.ROUND
    }
    private val drops = List(42) {
        Drop(
            xRatio = Random.nextFloat(),
            yRatio = Random.nextFloat(),
            length = Random.nextFloat() * 34f + 18f,
            speed = Random.nextFloat() * 0.42f + 0.18f,
            alpha = Random.nextInt(46, 118)
        )
    }
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1800L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }
    private var progress = 0f

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (visibility == VISIBLE && !animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            if (!animator.isStarted) animator.start()
        } else {
            animator.cancel()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = max(height.toFloat(), 1f)
        drops.forEach { drop ->
            val fall = ((drop.yRatio + progress * drop.speed) % 1f) * (h + drop.length * 2f)
            val startX = drop.xRatio * w
            val startY = fall - drop.length
            paint.alpha = drop.alpha
            canvas.drawLine(startX, startY, startX - drop.length * 0.42f, startY + drop.length, paint)
        }
    }
}
