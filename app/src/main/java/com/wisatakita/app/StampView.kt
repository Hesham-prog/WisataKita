package com.wisatakita.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.wisatakita.app.data.db.JourneyStampEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StampView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.4f * resources.displayMetrics.density
    }
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * resources.displayMetrics.density
        alpha = 150
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        alpha = 210
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cream_primary)
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
    }
    private val path = Path()
    private var stamp: JourneyStampEntity? = null

    fun bind(stamp: JourneyStampEntity) {
        this.stamp = stamp
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val item = stamp ?: return
        val cx = width / 2f
        val cy = height / 2f
        val radius = (width.coerceAtMost(height) / 2f) - 8f * resources.displayMetrics.density
        borderPaint.color = item.stampColor
        innerPaint.color = item.stampColor
        iconPaint.color = item.stampColor

        canvas.save()
        canvas.rotate(-8f, cx, cy)
        canvas.drawCircle(cx, cy, radius, borderPaint)
        canvas.drawCircle(cx, cy, radius - 8f * resources.displayMetrics.density, innerPaint)
        drawCategoryIcon(canvas, item.categoryType, cx, cy - 8f * resources.displayMetrics.density, radius * 0.36f)
        textPaint.textSize = 10f * resources.displayMetrics.scaledDensity
        canvas.drawText(item.categoryType.uppercase(Locale.getDefault()).take(10), cx, cy + radius * 0.34f, textPaint)
        textPaint.textSize = 8f * resources.displayMetrics.scaledDensity
        val date = SimpleDateFormat("dd MMM", Locale("id", "ID")).format(Date(item.unlockedAt))
        canvas.drawText(date, cx, cy + radius * 0.55f, textPaint)
        canvas.restore()
    }

    private fun drawCategoryIcon(canvas: Canvas, category: String, cx: Float, cy: Float, size: Float) {
        path.reset()
        if (category.contains("Pantai", ignoreCase = true) || category.contains("Danau", ignoreCase = true)) {
            val rect = RectF(cx - size, cy - size * 0.25f, cx + size, cy + size * 0.85f)
            path.addArc(rect, 200f, 140f)
            path.addArc(RectF(rect.left, rect.top + size * 0.36f, rect.right, rect.bottom + size * 0.36f), 200f, 140f)
            canvas.drawPath(path, borderPaint)
        } else {
            path.moveTo(cx - size, cy + size * 0.65f)
            path.lineTo(cx - size * 0.35f, cy - size * 0.35f)
            path.lineTo(cx, cy + size * 0.1f)
            path.lineTo(cx + size * 0.32f, cy - size * 0.48f)
            path.lineTo(cx + size, cy + size * 0.65f)
            path.close()
            canvas.drawPath(path, iconPaint)
        }
    }
}
