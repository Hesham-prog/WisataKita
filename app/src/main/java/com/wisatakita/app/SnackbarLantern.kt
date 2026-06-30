package com.wisatakita.app

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.card.MaterialCardView

class SnackbarLantern(context: Context) : FrameLayout(context) {

    private val card = MaterialCardView(context).apply {
        radius = 24f * resources.displayMetrics.density
        cardElevation = 0f
        setCardBackgroundColor(0xCC1A1918.toInt())
        strokeColor = context.getColor(R.color.glass_border)
        strokeWidth = (1 * resources.displayMetrics.density).toInt()
    }
    private val message = TextView(context).apply {
        setTextColor(Color.WHITE)
        textSize = 14f
        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans_semibold)
        setPadding(0, 0, (18 * resources.displayMetrics.density).toInt(), 0)
    }
    private val lantern = LottieAnimationView(context).apply {
        setAnimation(R.raw.lottie_lantern)
        repeatCount = 2
        speed = 0.8f
    }

    init {
        isClickable = false
        alpha = 0f
        val density = resources.displayMetrics.density
        addView(card, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
            leftMargin = (20 * density).toInt()
            rightMargin = (20 * density).toInt()
            bottomMargin = (92 * density).toInt()
        })
        val row = FrameLayout(context).apply {
            setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt())
        }
        card.addView(row)
        row.addView(lantern, LayoutParams((48 * density).toInt(), (48 * density).toInt()).apply {
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
        })
        row.addView(message, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_VERTICAL
            leftMargin = (62 * density).toInt()
        })
    }

    fun show(parent: ViewGroup, text: String) {
        if (parent.indexOfChild(this) == -1) {
            parent.addView(
                this,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }
        message.text = text
        translationY = 44f * resources.displayMetrics.density
        lantern.playAnimation()
        animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(360L)
            .withEndAction {
                postDelayed({
                    animate()
                        .alpha(0f)
                        .translationY(-28f * resources.displayMetrics.density)
                        .setDuration(320L)
                        .withEndAction { parent.removeView(this) }
                        .start()
                }, 2600L)
            }
            .start()
    }
}
