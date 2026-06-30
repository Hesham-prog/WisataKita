package com.wisatakita.app

import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat

enum class SlideDirection {
    UP,
    DOWN,
    START,
    END
}

fun View.bounceClick() {
    setOnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> view.springScale(0.94f)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.springScale(1f)
        }
        false
    }
}

fun View.springScale(targetScale: Float) {
    val duration = if (targetScale < 1f) 90L else 240L
    ViewCompat.animate(this)
        .scaleX(targetScale)
        .scaleY(targetScale)
        .setDuration(duration)
        .setInterpolator(if (targetScale < 1f) DecelerateInterpolator() else OvershootInterpolator(2.4f))
        .start()
}

fun View.fadeInSlide(direction: SlideDirection = SlideDirection.UP) {
    val distance = 28f * resources.displayMetrics.density
    alpha = 0f
    translationX = when (direction) {
        SlideDirection.START -> -distance
        SlideDirection.END -> distance
        else -> 0f
    }
    translationY = when (direction) {
        SlideDirection.UP -> distance
        SlideDirection.DOWN -> -distance
        else -> 0f
    }
    ViewCompat.animate(this)
        .alpha(1f)
        .translationX(0f)
        .translationY(0f)
        .setDuration(360L)
        .setInterpolator(OvershootInterpolator(0.8f))
        .start()
}
