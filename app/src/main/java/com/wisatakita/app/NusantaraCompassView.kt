package com.wisatakita.app

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * NusantaraCompassView — Custom FAB-style navigation button.
 *
 * Renders a golden compass rose disc. On tap, notifies [onTabSelected] with
 * the current tab index (0=Beranda, 1=Jelajahi, 2=Koleksi, 3=Profil).
 * The actual radial petal overlay is managed externally by MainActivity.
 */
class NusantaraCompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var isMenuOpen: Boolean = false
    var onTabSelected: ((Int) -> Unit)? = null
    var onToggleMenu: ((Boolean) -> Unit)? = null

    private val goldPrimary = Color.parseColor("#E3A33A")
    private val goldLight = Color.parseColor("#F0C47A")
    private val goldDark = Color.parseColor("#C87D20")
    private val charcoal = Color.parseColor("#2E2D2A")
    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = goldLight
        strokeWidth = 2f
    }
    private val paintGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintIcon = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = charcoal
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        strokeCap = Paint.Cap.ROUND
    }

    private var gradient: RadialGradient? = null
    private var glowGradient: RadialGradient? = null
    private var cx = 0f
    private var cy = 0f
    private var radius = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f
        radius = min(w, h) / 2f - 6f
        gradient = RadialGradient(
            cx, cy - radius * 0.2f, radius,
            intArrayOf(goldLight, goldPrimary, goldDark),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        glowGradient = RadialGradient(
            cx, cy, radius + 10f,
            intArrayOf(Color.parseColor("#66E3A33A"), Color.parseColor("#1AE3A33A"), Color.TRANSPARENT),
            floatArrayOf(0f, 0.68f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Outer glow
        paintGlow.shader = glowGradient
        paintGlow.alpha = if (isMenuOpen) 255 else 190
        canvas.drawCircle(cx, cy, radius + 8f, paintGlow)

        // Main disc
        paintFill.shader = gradient
        canvas.drawCircle(cx, cy, radius, paintFill)

        // Border ring
        canvas.drawCircle(cx, cy, radius - 1f, paintStroke)

        // Draw compass rose spokes or X (when open)
        if (isMenuOpen) {
            drawCloseX(canvas)
        } else {
            drawCompassRose(canvas)
        }
    }

    private fun drawCompassRose(canvas: Canvas) {
        paintIcon.color = charcoal
        paintIcon.strokeWidth = 2.5f
        paintIcon.style = Paint.Style.STROKE

        val inner = radius * 0.25f
        val outer = radius * 0.72f
        val cardinals = 8

        for (i in 0 until cardinals) {
            val angle = Math.toRadians((i * 360.0 / cardinals) - 90)
            val startR = if (i % 2 == 0) inner else inner * 1.6f
            val endR = if (i % 2 == 0) outer else outer * 0.72f
            canvas.drawLine(
                cx + (startR * cos(angle)).toFloat(),
                cy + (startR * sin(angle)).toFloat(),
                cx + (endR * cos(angle)).toFloat(),
                cy + (endR * sin(angle)).toFloat(),
                paintIcon
            )
        }

        // Center dot
        paintIcon.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, inner * 0.6f, paintIcon)
    }

    private fun drawCloseX(canvas: Canvas) {
        paintIcon.color = charcoal
        paintIcon.strokeWidth = 3f
        paintIcon.style = Paint.Style.STROKE

        val offset = radius * 0.3f
        canvas.drawLine(cx - offset, cy - offset, cx + offset, cy + offset, paintIcon)
        canvas.drawLine(cx + offset, cy - offset, cx - offset, cy + offset, paintIcon)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                animate().scaleX(0.88f).scaleY(0.88f).setDuration(100).start()
                HapticUtil.click(this)
                return true
            }
            MotionEvent.ACTION_UP -> {
                animate().scaleX(1f).scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(OvershootInterpolator(2.5f))
                    .withEndAction {
                        isMenuOpen = !isMenuOpen
                        onToggleMenu?.invoke(isMenuOpen)
                        invalidate()
                    }.start()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = (64 * resources.displayMetrics.density).toInt()
        setMeasuredDimension(desired, desired)
    }
}
