package com.wisatakita.app

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class MorphingImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    fun setScaleType(value: String) {
        scaleType = when (value) {
            "fitStart" -> ScaleType.FIT_START
            "centerInside" -> ScaleType.CENTER_INSIDE
            else -> ScaleType.CENTER_CROP
        }
    }
}
