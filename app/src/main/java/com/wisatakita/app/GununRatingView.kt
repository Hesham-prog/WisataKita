package com.wisatakita.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.ceil

class GununRatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var rating: Int = 0
        private set

    var onRatingChanged: ((Int) -> Unit)? = null

    private val peakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.charcoal_medium)
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.glass_border)
        style = Paint.Style.STROKE
        strokeWidth = 1.6f * resources.displayMetrics.density
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val peakPath = Path()
    private var animatedRating = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (88 * resources.displayMetrics.density).toInt()
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val gap = 8f * resources.displayMetrics.density
        val peakWidth = (width - paddingStart - paddingEnd - gap * 4) / 5f
        val top = paddingTop.toFloat() + 4f * resources.displayMetrics.density
        val bottom = height - paddingBottom.toFloat() - 4f * resources.displayMetrics.density

        fillPaint.shader = LinearGradient(
            0f,
            bottom,
            0f,
            top,
            0xFFE3A33A.toInt(),
            0xFFD96B2A.toInt(),
            Shader.TileMode.CLAMP
        )

        repeat(5) { index ->
            val left = paddingStart + index * (peakWidth + gap)
            buildPeakPath(left, top, peakWidth, bottom, index)
            canvas.drawPath(peakPath, peakPaint)

            val peakFill = (animatedRating - index).coerceIn(0f, 1f)
            if (peakFill > 0f) {
                val checkpoint = canvas.save()
                canvas.clipPath(peakPath)
                val fillTop = bottom - ((bottom - top) * peakFill)
                canvas.drawRect(left, fillTop, left + peakWidth, bottom, fillPaint)
                canvas.restoreToCount(checkpoint)
            }
            canvas.drawPath(peakPath, strokePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                val nextRating = ceil((event.x / width.coerceAtLeast(1)) * 5f).toInt().coerceIn(1, 5)
                setRating(nextRating, fromUser = true)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun setRating(value: Int, fromUser: Boolean = false) {
        val next = value.coerceIn(0, 5)
        if (next == rating) return
        val previousAnimated = animatedRating
        rating = next
        if (fromUser) performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        onRatingChanged?.invoke(rating)
        ValueAnimator.ofFloat(previousAnimated, rating.toFloat()).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animatedRating = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun buildPeakPath(left: Float, top: Float, peakWidth: Float, bottom: Float, index: Int) {
        val ridgeInset = peakWidth * (0.18f + (index % 2) * 0.04f)
        val shoulder = bottom - (bottom - top) * 0.36f
        val peak = top + (index % 3) * 5f * resources.displayMetrics.density
        peakPath.reset()
        peakPath.moveTo(left, bottom)
        peakPath.lineTo(left + ridgeInset, shoulder)
        peakPath.lineTo(left + peakWidth * 0.48f, peak)
        peakPath.lineTo(left + peakWidth - ridgeInset * 0.65f, shoulder + 8f * resources.displayMetrics.density)
        peakPath.lineTo(left + peakWidth, bottom)
        peakPath.close()
        peakPath.addRoundRect(RectF(left, bottom - 3f, left + peakWidth, bottom + 3f), 4f, 4f, Path.Direction.CW)
    }
}
