package de.suitepad.linbridge.logger

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linphone.core.LogLevel
import org.linphone.core.LoggingService
import org.linphone.core.LoggingServiceListener
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
private val simpleDateFormat = SimpleDateFormat("MM-dd HH:mm:ss")

class LogCatcher : Timber.Tree(), LoggingServiceListener {

    companion object {
        const val LOG_SIZE = 250
    }

    val logListeners = mutableSetOf<LogListener>()
    val log = mutableListOf<LogEntry>()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val newLogEntry = LogEntry(Date(), priority, tag, message, t)
        log.add(newLogEntry)
        if (log.size > LOG_SIZE) {
            log.removeAt(0)
        }
        logListeners.forEach {
            it.log(newLogEntry.getStyledText())
        }
    }

    fun addLogListener(logListener: LogListener) {
        logListeners.add(logListener)
        GlobalScope.launch(Dispatchers.IO) {
            logListener.log(buildLogText())
        }
    }

    fun removeLogListener(logListener: LogListener) {
        logListeners.remove(logListener)
    }

    fun buildLogText(): String {
        var result: String = ""
        val iterator = log.iterator()
        while (iterator.hasNext()) {
            result += iterator.next().getStyledText()
        }
        return result
    }

    class LogEntry(val date: Date, val priority: Int, val tag: String?, val message: String, var t: Throwable?) {

        fun getStyledText(): String {
            var result = ""
            if (!message.isBlank()) {
                result += getColoredText(getColor(priority), date, tag, message)
            }
            if (t != null) {
                result += getColoredText("red", date, tag, t.toString())
            }
            return result
        }

        private fun getColoredText(color: String, date: Date, tag: String?, message: String): String {
            return message.lines().fold("<font color=\"$color\">${simpleDateFormat.format(date)} ") { acc, c ->
                "${if (tag != null) "[$tag] " else ""}$acc$c<br />"
            } + "</font>"
        }

        fun getColor(logPriority: Int): String = when (logPriority) {
            Log.DEBUG -> "gray"
            Log.WARN -> "#FF9800"
            Log.ERROR -> "red"
            Log.ASSERT -> "red"
            else -> "white"
        }

    }

    override fun onLogMessageWritten(p0: LoggingService?, p1: String?, p2: LogLevel?, p3: String?) {
        Timber.tag(p1).log(p2?.toInt() ?: Log.INFO, p3)
    }

    interface LogListener {
        fun log(message: String)
    }

}