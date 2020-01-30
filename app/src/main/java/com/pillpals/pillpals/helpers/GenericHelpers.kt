package com.pillpals.pillpals.helpers

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable



fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = dpToPx(this) }
        top?.run { topMargin = dpToPx(this) }
        right?.run { rightMargin = dpToPx(this) }
        bottom?.run { bottomMargin = dpToPx(this) }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

public fun drawableToBitmap(drawable: Drawable): Bitmap? {
    var bitmap: Bitmap? = null

    if (drawable is BitmapDrawable) {
        if (drawable.bitmap != null) {
            return drawable.bitmap
        }
    }

    if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        bitmap = Bitmap.createBitmap(
            1,
            1,
            Bitmap.Config.ARGB_8888
        ) // Single color bitmap will be created of 1x1 pixel
    } else {
        bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    }

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    drawable.draw(canvas)
    return bitmap
}