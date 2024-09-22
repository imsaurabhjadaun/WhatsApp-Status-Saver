package com.savestatus.wsstatussaver.views

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.webkit.WebView

class LollipopFixedWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : WebView(getFixedContext(context), attrs, defStyleAttr){

    companion object {
        fun getFixedContext(context: Context): Context {
            return context.createConfigurationContext(Configuration())
        }
    }
}