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
        strokeWidth = 1.5f * resources.displayMetrics.density
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    // Snow cap on the peak tip
    private val snowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCCFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    private val peakPath = Path()
    private val snowPath = Path()
    private var animatedRating = 0f

    // Alternating peak heights to give a realistic mountain range silhouette
    private val peakHeightFactors = floatArrayOf(0.78f, 0.95f, 1.0f, 0.90f, 0.82f)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (92 * resources.displayMetrics.density).toInt()
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val dp = resources.displayMetrics.density
        val gap = 7f * dp
        val peakWidth = (width - paddingStart - paddingEnd - gap * 4) / 5f
        val top = paddingTop.toFloat() + 6f * dp
        val bottom = height - paddingBottom.toFloat() - 4f * dp
        val totalHeight = bottom - top

        fillPaint.shader = LinearGradient(
            0f, bottom, 0f, top,
            intArrayOf(0xFFE3A33A.toInt(), 0xFFD96B2A.toInt(), 0xFFC85020.toInt()),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )

        repeat(5) { index ->
            val left = paddingStart + index * (peakWidth + gap)
            val heightFactor = peakHeightFactors[index]
            val peakTop = bottom - totalHeight * heightFactor

            buildPeakPath(left, peakTop, top, peakWidth, bottom, index)
            canvas.drawPath(peakPath, peakPaint)

            val peakFill = (animatedRating - index).coerceIn(0f, 1f)
            if (peakFill > 0f) {
                val checkpoint = canvas.save()
                canvas.clipPath(peakPath)
                val fillTop = bottom - ((bottom - peakTop) * peakFill)
                canvas.drawRect(left, fillTop, left + peakWidth, bottom, fillPaint)

                // Draw snow cap on fully-rated peaks
                if (peakFill >= 0.92f) {
                    buildSnowCapPath(left, peakTop, peakWidth, dp)
                    canvas.drawPath(snowPath, snowPaint)
                }
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
            duration = 260L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                animatedRating = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /**
     * Builds a realistic multi-peak mountain silhouette using quadratic Bézier curves.
     * Each mountain has:
     *   - A sharp primary summit at peakTop
     *   - Two secondary side-shoulders creating a ridge-line feel
     *   - Smooth curved flanks flowing down to the base
     */
    private fun buildPeakPath(
        left: Float, peakTop: Float, top: Float,
        peakWidth: Float, bottom: Float, index: Int
    ) {
        val cx = left + peakWidth * 0.5f           // center x (main summit)
        val rightShoulder = left + peakWidth * 0.75f
        val leftShoulder = left + peakWidth * 0.25f
        val shoulderY = peakTop + (bottom - peakTop) * 0.28f    // secondary peaks
        val leftSecondaryPeak = peakTop + (bottom - peakTop) * (0.12f + index * 0.04f)
        val rightSecondaryPeak = peakTop + (bottom - peakTop) * (0.18f + (4 - index) * 0.03f)
        val baseInset = peakWidth * 0.06f

        peakPath.reset()
        // Start at bottom-left
        peakPath.moveTo(left, bottom)
        // Left flank — curve up to left secondary peak
        peakPath.quadTo(left + peakWidth * 0.08f, shoulderY + 10f, leftShoulder, leftSecondaryPeak)
        // Ridge from left secondary peak up to main summit (sharp angle for realism)
        peakPath.lineTo(cx, peakTop)
        // Down to right secondary peak
        peakPath.lineTo(rightShoulder, rightSecondaryPeak)
        // Right flank curve down to base
        peakPath.quadTo(left + peakWidth * 0.92f, shoulderY + 10f, left + peakWidth, bottom)
        peakPath.close()

        // Rounded base bar
        peakPath.addRoundRect(
            RectF(left + baseInset, bottom - 3f, left + peakWidth - baseInset, bottom + 2f),
            3f, 3f, Path.Direction.CW
        )
    }

    /**
     * Draws a small rounded snowcap polygon at the mountain tip.
     */
    private fun buildSnowCapPath(left: Float, peakTop: Float, peakWidth: Float, dp: Float) {
        val cx = left + peakWidth * 0.5f
        val capHeight = 10f * dp
        val capWidth = 9f * dp
        snowPath.reset()
        snowPath.moveTo(cx, peakTop)
        snowPath.lineTo(cx - capWidth, peakTop + capHeight)
        snowPath.quadTo(cx, peakTop + capHeight * 0.6f, cx + capWidth, peakTop + capHeight)
        snowPath.close()
    }
}

