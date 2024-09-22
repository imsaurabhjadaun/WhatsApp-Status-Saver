package com.savestatus.wsstatussaver

/*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.google.firebase.analytics.FirebaseAnalytics.Param
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

private val firebaseAnalytics: FirebaseAnalytics by lazy { Firebase.analytics }
private val firebaseCrashlytics: FirebaseCrashlytics by lazy { Firebase.crashlytics }
*/

fun setAnalyticsEnabled(enabled: Boolean) {
 //   firebaseAnalytics.setAnalyticsCollectionEnabled(!BuildConfig.DEBUG && enabled)
   // firebaseCrashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG && enabled)
}

fun logToolView(clazz: String, name: String) {
    /*firebaseAnalytics.logEvent(Event.SCREEN_VIEW) {
        param(Param.SCREEN_CLASS, clazz)
        param(Param.SCREEN_NAME, name)
    }*/
}

fun logUpdateRequest(newVersion: String, accepted: Boolean) {
    /*firebaseAnalytics.logEvent("request_app_update") {
        param("new_version", newVersion)
        param("user_accepted", accepted.toString())
    }*/
}

fun logThemeSelected(themeName: String) {
    /*firebaseAnalytics.logEvent(Event.SELECT_CONTENT) {
        param(Param.CONTENT_TYPE, "general_theme")
        param(Param.ITEM_ID, themeName)
    }*/
}

fun logLanguageSelected(languageName: String) {
    /*firebaseAnalytics.logEvent(Event.SELECT_CONTENT) {
        param(Param.CONTENT_TYPE, "language_name")
        param(Param.ITEM_ID, languageName)
    }*/
}

fun logLongPressActionSelected(actionName: String) {
    /*firebaseAnalytics.logEvent(Event.SELECT_CONTENT) {
        param(Param.CONTENT_TYPE, "long_press_action")
        param(Param.ITEM_ID, actionName)
    }*/
}

fun logDefaultClient(packageName: String) {
    /*firebaseAnalytics.logEvent("select_default_client") {
        param("client_id", packageName)
    }*/
}

fun logUrlView(url: String) {
   /* firebaseAnalytics.logEvent("open_url") {
     //   param("url", url)
    }*/
}

fun recordException(throwable: Throwable) {
    //firebaseCrashlytics.recordException(throwable)
}