package com.wisatakita.app

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.core.graphics.ColorUtils

class BatikShimmerDrawable : Drawable(), Animatable {

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ColorUtils.setAlphaComponent(0xFF3D3C38.toInt(), 235)
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.2f
        color = ColorUtils.setAlphaComponent(0xFFF2D8B3.toInt(), 34)
    }
    private val shimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val petal = Path()
    private var shimmerShift = 0f
    private var alphaValue = 255

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1500L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            shimmerShift = it.animatedValue as Float
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return

        basePaint.alpha = alphaValue
        linePaint.alpha = (34 * (alphaValue / 255f)).toInt()
        canvas.drawRect(b, basePaint)
        drawKawungPattern(canvas, b)
        drawShimmer(canvas, b)
        if (!isRunning) start()
    }

    private fun drawKawungPattern(canvas: Canvas, b: Rect) {
        val cell = (b.width().coerceAtLeast(b.height()) / 5f).coerceAtLeast(34f)
        var y = b.top - cell
        while (y < b.bottom + cell) {
            var x = b.left - cell
            while (x < b.right + cell) {
                drawPetal(canvas, x + cell * 0.5f, y + cell * 0.5f, cell * 0.42f, true)
                drawPetal(canvas, x + cell * 0.5f, y + cell * 0.5f, cell * 0.42f, false)
                x += cell
            }
            y += cell
        }
    }

    private fun drawPetal(canvas: Canvas, cx: Float, cy: Float, radius: Float, vertical: Boolean) {
        petal.reset()
        if (vertical) {
            petal.moveTo(cx, cy - radius)
            petal.cubicTo(cx + radius * 0.75f, cy - radius * 0.3f, cx + radius * 0.75f, cy + radius * 0.3f, cx, cy + radius)
            petal.cubicTo(cx - radius * 0.75f, cy + radius * 0.3f, cx - radius * 0.75f, cy - radius * 0.3f, cx, cy - radius)
        } else {
            petal.moveTo(cx - radius, cy)
            petal.cubicTo(cx - radius * 0.3f, cy - radius * 0.75f, cx + radius * 0.3f, cy - radius * 0.75f, cx + radius, cy)
            petal.cubicTo(cx + radius * 0.3f, cy + radius * 0.75f, cx - radius * 0.3f, cy + radius * 0.75f, cx - radius, cy)
        }
        canvas.drawPath(petal, linePaint)
    }

    private fun drawShimmer(canvas: Canvas, b: Rect) {
        val width = b.width().toFloat().coerceAtLeast(1f)
        val start = b.left + (shimmerShift * width * 2f) - width
        shimmerPaint.shader = LinearGradient(
            start,
            b.top.toFloat(),
            start + width * 0.9f,
            b.bottom.toFloat(),
            intArrayOf(
                ColorUtils.setAlphaComponent(0x00000000, 0),
                ColorUtils.setAlphaComponent(0xFFE3A33A.toInt(), (54 * (alphaValue / 255f)).toInt()),
                ColorUtils.setAlphaComponent(0x0020B8C7, 0)
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(b, shimmerPaint)
    }

    override fun setAlpha(alpha: Int) {
        alphaValue = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        basePaint.colorFilter = colorFilter
        linePaint.colorFilter = colorFilter
        shimmerPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun start() {
        if (!animator.isStarted) animator.start()
    }

    override fun stop() {
        animator.cancel()
    }

    override fun isRunning(): Boolean = animator.isRunning

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        val changed = super.setVisible(visible, restart)
        if (visible) {
            if (restart) {
                animator.cancel()
            }
            start()
        } else {
            stop()
        }
        return changed
    }
}
