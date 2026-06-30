package com.wisatakita.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class TopographicBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.green_primary)
        alpha = 13
        style = Paint.Style.STROKE
        strokeWidth = 1.35f * resources.displayMetrics.density
    }
    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density
        var y = -80f * density
        var index = 0
        while (y < h + 120f * density) {
            path.reset()
            path.moveTo(-40f * density, y)
            val amplitude = (18 + (index % 4) * 6) * density
            val step = 92f * density
            var x = -40f * density
            while (x < w + step) {
                path.cubicTo(
                    x + step * 0.28f,
                    y - amplitude,
                    x + step * 0.62f,
                    y + amplitude,
                    x + step,
                    y
                )
                x += step
            }
            canvas.drawPath(path, paint)
            y += 34f * density
            index++
        }
    }
}
