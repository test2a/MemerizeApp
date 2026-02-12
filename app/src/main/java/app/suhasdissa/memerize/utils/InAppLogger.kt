package app.suhasdissa.memerize.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InAppLogger {
    private const val MAX_LOGS = 200
    private val logBuffer = mutableListOf<String>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun log(msg: String, tag: String = "AppLog") {
        val ts = dateFormat.format(Date())
        synchronized(logBuffer) {
            logBuffer.add("[$ts][$tag] $msg")
            if (logBuffer.size > MAX_LOGS) logBuffer.removeAt(0)
        }
    }

    fun getLogs(): String = synchronized(logBuffer) { logBuffer.joinToString("\n") }
    fun clear() = synchronized(logBuffer) { logBuffer.clear() }
}
