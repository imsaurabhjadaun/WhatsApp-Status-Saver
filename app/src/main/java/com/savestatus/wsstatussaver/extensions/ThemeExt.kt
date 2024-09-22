package com.savestatus.wsstatussaver.extensions

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.google.android.material.color.MaterialColors
import com.savestatus.wsstatussaver.R

val Context.isNightModeEnabled: Boolean
    get() = resources.configuration.run {
        this.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

fun Context.getGeneralThemeRes(): Int =
    if (isNightModeEnabled && preferences().isJustBlack()) R.style.Theme_StatusSaver_Black else R.style.Theme_StatusSaver

fun Context.primaryColor() =
    MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary, Color.TRANSPARENT)

fun Context.surfaceColor(fallback: Int = Color.TRANSPARENT) =
    MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, fallback)