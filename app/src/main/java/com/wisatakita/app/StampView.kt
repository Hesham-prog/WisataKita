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

        canvas.save()
        canvas.rotate(-8f, cx, cy)
        canvas.drawCircle(cx, cy, radius, borderPaint)
        canvas.drawCircle(cx, cy, radius - 8f * resources.displayMetrics.density, innerPaint)
        
        // Draw icon using fuzzy matching
        val lowerCat = item.categoryType.lowercase(Locale.getDefault())
        val iconRes = when {
            lowerCat.contains("pantai") || lowerCat.contains("laut") -> R.drawable.ic_stamp_pantai
            lowerCat.contains("danau") -> R.drawable.ic_stamp_danau
            lowerCat.contains("sejarah") || lowerCat.contains("candi") -> R.drawable.ic_stamp_sejarah
            lowerCat.contains("budaya") -> R.drawable.ic_stamp_budaya
            lowerCat.contains("kota") || lowerCat.contains("wisata") -> R.drawable.ic_stamp_wisata
            else -> R.drawable.ic_stamp_alam // Default for Gunung, Alam, Bukit, dll
        }
        val drawable = ContextCompat.getDrawable(context, iconRes)
        if (drawable != null) {
            drawable.setTint(item.stampColor)
            val iconSize = (radius * 0.72f).toInt()
            val left = (cx - iconSize / 2).toInt()
            val top = (cy - radius * 0.5f - iconSize / 2).toInt()
            drawable.setBounds(left, top, left + iconSize, top + iconSize)
            drawable.draw(canvas)
        }

        // Handle category text to prevent cutting off
        var displayCat = item.categoryType.uppercase(Locale.getDefault())
        if (displayCat.length > 12) {
            displayCat = displayCat.take(10) + "..."
        }
        val baseTextSize = 10f * resources.displayMetrics.scaledDensity
        textPaint.textSize = if (displayCat.length > 8) baseTextSize * 0.85f else baseTextSize
        
        canvas.drawText(displayCat, cx, cy + radius * 0.34f, textPaint)
        textPaint.textSize = 8f * resources.displayMetrics.scaledDensity
        val date = SimpleDateFormat("dd MMM", Locale("id", "ID")).format(Date(item.unlockedAt))
        canvas.drawText(date, cx, cy + radius * 0.55f, textPaint)
        canvas.restore()
    }
}
