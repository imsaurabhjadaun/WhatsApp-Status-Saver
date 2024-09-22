package com.savestatus.wsstatussaver.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.savestatus.wsstatussaver.R
import com.savestatus.wsstatussaver.extensions.*

class IconImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.IconImageView, defStyleAttr, 0)
        setIconColor(a.getColor(R.styleable.IconImageView_iconViewColor, Color.BLACK))
        a.recycle()
    }

    fun setIconColor(@ColorInt color: Int) {
        if (context.isNightModeEnabled) {
            setIconColorInternal(color.desaturateColor(), context.surfaceColor())
        } else {
            setIconColorInternal(color.adjustAlpha(0.22f), color.adjustAlpha(0.75f))
        }
    }

    private fun setIconColorInternal(@ColorInt backgroundColor: Int, @ColorInt imageColor: Int) {
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.WHITE)
        }

        backgroundTintList = backgroundColor.toColorStateList()
        imageTintList = imageColor.toColorStateList()
    }
}