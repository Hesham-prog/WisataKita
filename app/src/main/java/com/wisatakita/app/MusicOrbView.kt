package com.wisatakita.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class MusicOrbView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pulse = 0f
    private var playing = MusicService.isPlaying
    private var glowRadius = 0f
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.glass_surface)
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
        color = ContextCompat.getColor(context, R.color.glass_border)
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 3f * resources.displayMetrics.density
        color = ContextCompat.getColor(context, R.color.gold_primary)
    }
    private val bounds = RectF()
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1100L
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        addUpdateListener {
            pulse = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = true
        contentDescription = "Kontrol musik"
    }

    fun setPlaying(isPlaying: Boolean) {
        playing = isPlaying
        if (playing && isAttachedToWindow && !animator.isStarted) animator.start()
        if (!playing) animator.cancel()
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (playing && !animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val viewSize = minOf(width, height).toFloat()
        val radius = viewSize / 2f
        val centerX = width / 2f
        val centerY = height / 2f

        val currentGlowRadius = radius * (0.8f + pulse * 0.2f)
        if (currentGlowRadius != glowRadius) {
            glowRadius = currentGlowRadius
            val glowColor = ContextCompat.getColor(context, R.color.turquoise_primary)
            glowPaint.shader = android.graphics.RadialGradient(
                centerX, centerY, glowRadius,
                intArrayOf(glowColor, glowColor, android.graphics.Color.TRANSPARENT),
                floatArrayOf(0f, 0.4f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        
        glowPaint.alpha = if (playing) (100 + pulse * 155).toInt() else 0
        canvas.drawCircle(centerX, centerY, glowRadius, glowPaint)

        // The actual orb is smaller to give room for the glow
        val orbRadius = radius * 0.65f
        bounds.set(centerX - orbRadius, centerY - orbRadius, centerX + orbRadius, centerY + orbRadius)
        canvas.drawOval(bounds, orbPaint)
        canvas.drawOval(bounds, strokePaint)

        val lineTop = centerY - orbRadius * 0.4f
        val lineBottom = centerY + orbRadius * 0.4f
        val spacing = orbRadius * 0.3f
        val base = centerX - spacing
        for (i in 0..2) {
            val x = base + i * spacing
            val dynamic = if (playing) pulse * (8f - i * 2f) else 0f
            canvas.drawLine(x, lineTop + dynamic, x, lineBottom - dynamic, linePaint)
        }
    }
}
