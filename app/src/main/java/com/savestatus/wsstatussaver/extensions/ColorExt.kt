package com.savestatus.wsstatussaver.extensions

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.roundToInt

val Int.isColorLight: Boolean
    get() = (1 - (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255) < 0.4

val Int.lightenColor: Int
    get() = shiftColor(1.1f)

fun Int.shiftColor(by: Float): Int {
    if (by == 1f) return this
    val alpha = Color.alpha(this)
    val hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    hsv[2] *= by // value component

    return (alpha shl 24) + (0x00ffffff and Color.HSVToColor(hsv))
}

@ColorInt
fun Int.adjustAlpha(@FloatRange(from = 0.0, to = 1.0) factor: Float): Int {
    val alpha = (this.alpha * factor).roundToInt()
    val red = this.red
    val green = this.green
    val blue = this.blue
    return Color.argb(alpha, red, green, blue)
}

fun Int.desaturateColor(ratio: Float = 0.4f): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(this, hsv)
    hsv[1] = hsv[1] / 1 * ratio + 0.2f * (1.0f - ratio)
    return Color.HSVToColor(hsv)
}

fun Int.toHexString() = Integer.toHexString(this).substring(2)

fun Int.toColorStateList(): ColorStateList = ColorStateList.valueOf(this)