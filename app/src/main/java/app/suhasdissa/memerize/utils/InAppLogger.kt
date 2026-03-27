package app.test2a.memerize.utils

import android.util.Log

object InAppLogger {
    // Overlay removed: forward logs to Android `Log` and disable in-app buffering
    fun log(msg: String, tag: String = "AppLog") {
        Log.i(tag, msg)
    }

    fun getLogs(): String = "" // no in-app overlay
    fun clear() {} // no-op
}
