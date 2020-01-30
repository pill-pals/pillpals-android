package com.pillpals.pillpals.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class ColorFadeScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyle: Int = 0
) : ScrollView(context, attrs, defStyle) {
    var fadeColor: Int
        get() = mFadeColor
        set(fadeColor) {
            mFadeColor = fadeColor
        }

    init {
        setFadingEdgeLength(20)
        isVerticalFadingEdgeEnabled = true
    }

    override fun getSolidColor(): Int {
        return mFadeColor
    }

    companion object {
        private var mFadeColor = -0x444444
    }
}