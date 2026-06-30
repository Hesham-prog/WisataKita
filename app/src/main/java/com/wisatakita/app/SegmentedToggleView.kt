package com.wisatakita.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

class SegmentedToggleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onModeSelected: ((Int) -> Unit)? = null
    private val labels = listOf("LIST", "GRID", "CARD")
    private var selectedIndex = 0

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.glass_surface)
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
        color = ContextCompat.getColor(context, R.color.glass_border)
    }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.gold_primary)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 9f * resources.displayMetrics.scaledDensity
        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
    }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.glass_border)
        strokeWidth = resources.displayMetrics.density
    }
    private val bounds = RectF()
    private val selectedPath = Path()

    init {
        isClickable = true
        contentDescription = "Pilih mode tampilan"
    }

    fun setSelectedIndex(index: Int) {
        selectedIndex = index.coerceIn(0, labels.lastIndex)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bounds.set(0f, 0f, width.toFloat(), height.toFloat())
        val radius = height / 2f

        canvas.drawRoundRect(bounds, radius, radius, backgroundPaint)
        drawSelectedIsland(canvas)

        val segmentWidth = width / labels.size.toFloat()
        for (i in 1 until labels.size) {
            val x = segmentWidth * i
            canvas.drawLine(x, height * 0.28f, x, height * 0.72f, dividerPaint)
        }

        val baseline = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        labels.forEachIndexed { index, label ->
            textPaint.color = ContextCompat.getColor(
                context,
                if (index == selectedIndex) R.color.charcoal_dark else R.color.cream_primary
            )
            textPaint.alpha = if (index == selectedIndex) 255 else 170
            canvas.drawText(label, segmentWidth * index + segmentWidth / 2f, baseline, textPaint)
        }

        canvas.drawRoundRect(bounds, radius, radius, strokePaint)
    }

    private fun drawSelectedIsland(canvas: Canvas) {
        val segmentWidth = width / labels.size.toFloat()
        val left = segmentWidth * selectedIndex + 4f
        val right = left + segmentWidth - 8f
        val top = 4f
        val bottom = height - 4f

        selectedPath.reset()
        selectedPath.moveTo(left + 12f, top)
        selectedPath.cubicTo(left + 5f, top + 4f, left, height * 0.32f, left, height / 2f)
        selectedPath.cubicTo(left, height * 0.72f, left + 8f, bottom, left + 20f, bottom)
        selectedPath.lineTo(right - 14f, bottom)
        selectedPath.cubicTo(right - 5f, bottom - 2f, right, height * 0.70f, right, height / 2f)
        selectedPath.cubicTo(right, height * 0.30f, right - 7f, top + 2f, right - 18f, top)
        selectedPath.close()
        canvas.drawPath(selectedPath, selectedPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        if (event.action == MotionEvent.ACTION_UP) {
            val index = ((event.x / width) * labels.size).toInt().coerceIn(0, labels.lastIndex)
            if (index != selectedIndex) {
                selectedIndex = index
                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                invalidate()
                onModeSelected?.invoke(index)
            }
            performClick()
            return true
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
