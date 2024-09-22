package com.savestatus.wsstatussaver.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.savestatus.wsstatussaver.R

class ToolView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {

    private var titleView: TextView? = null
    private var descView: TextView? = null
    private var iconView: ImageView? = null

    init {
        addView(inflate(context, R.layout.item_view_tool, null))
        titleView = findViewById(R.id.title)
        descView = findViewById(R.id.description)
        iconView = findViewById(R.id.icon)

        val a = context.obtainStyledAttributes(attrs, R.styleable.ToolView, defStyleAttr, 0)
        setTitle(a.getString(R.styleable.ToolView_toolName))
        setDescription(a.getString(R.styleable.ToolView_toolDescription))
        setIcon(a.getDrawable(R.styleable.ToolView_toolIcon))
        a.recycle()
    }

    fun setIcon(icon: Drawable?) {
        iconView?.setImageDrawable(icon)
    }

    fun setTitle(title: CharSequence?) {
        titleView?.text = title
    }

    fun setDescription(description: CharSequence?) {
        descView?.text = description
    }
}