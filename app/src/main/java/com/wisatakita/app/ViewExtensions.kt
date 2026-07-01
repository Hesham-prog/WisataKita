package com.wisatakita.app

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View

/**
 * Applies a true frosted-glass blur to this View using RenderEffect (API 31+).
 * On older devices, this is a no-op — the existing translucent background color
 * already provides a subtle glass appearance.
 *
 * @param radius The blur radius in pixels. Typical values: 8f–24f for visible glassmorphism.
 */
fun View.applyGlassmorphismBlur(radius: Float = 16f) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val blurEffect = RenderEffect.createBlurEffect(
            radius,
            radius,
            Shader.TileMode.MIRROR
        )
        setRenderEffect(blurEffect)
    }
    // API < 31: Gracefully falls back to the existing translucent color background.
    // No crash, no visual regression.
}

/**
 * Removes any previously applied RenderEffect blur from this View.
 */
fun View.clearGlassmorphismBlur() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        setRenderEffect(null)
    }
}
