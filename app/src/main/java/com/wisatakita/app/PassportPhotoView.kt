package com.wisatakita.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat

class PassportPhotoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.2f * resources.displayMetrics.density
        color = ContextCompat.getColor(context, R.color.gold_primary)
        pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
    }
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.4f * resources.displayMetrics.density
        color = ContextCompat.getColor(context, R.color.glass_border)
    }

    init {
        scaleType = ScaleType.CENTER_INSIDE
        setPadding(14, 14, 14, 14)
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas) {
        val checkpoint = canvas.save()
        val radius = width.coerceAtMost(height) / 2f
        canvas.clipPath(android.graphics.Path().apply {
            addCircle(width / 2f, height / 2f, radius - 4f * resources.displayMetrics.density, android.graphics.Path.Direction.CW)
        })
        super.onDraw(canvas)
        canvas.restoreToCount(checkpoint)
        val cx = width / 2f
        val cy = height / 2f
        canvas.drawCircle(cx, cy, radius - 5f * resources.displayMetrics.density, outerPaint)
        canvas.drawCircle(cx, cy, radius - 12f * resources.displayMetrics.density, innerPaint)
    }
}
