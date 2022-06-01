package de.suitepad.linbridge.logger

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.linphone.core.LogLevel
import org.linphone.core.LoggingService
import org.linphone.core.LoggingServiceListener
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@SuppressLint("SimpleDateFormat")
private val simpleDateFormat = SimpleDateFormat("MM-dd HH:mm:ss")

@Singleton
class LogCatcher @Inject constructor() : Timber.Tree(), LoggingServiceListener, CoroutineScope {

    companion object {
        const val LOG_SIZE = 250

        private const val LINE_BREAK = "<br />"
        const val EXTERNAL_LINE_BREAK = "<br  />"
    }

    val logListeners = mutableSetOf<LogListener>()
    val log = mutableListOf<LogEntry>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + logJob

    private val logJob: Job = Job()
    private val mutex = Mutex()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val newLogEntry = LogEntry(Date(), priority, tag, message, t)
        launch {
            mutex.withLock {
                if (log.isNotEmpty() && newLogEntry.equals(log.last())) {
                    log[log.size - 1].date = newLogEntry.date
                    log[log.size - 1].count++
                    logListeners.forEach {
                        it.log(log[log.size - 1].getStyledText(), true)
                    }
                } else {
                    log.add(newLogEntry)
                    if (log.size > LOG_SIZE) {
                        log.removeAt(0)
                    }
                    logListeners.forEach {
                        it.log(newLogEntry.getStyledText(), false)
                    }
                }
            }
        }
    }

    fun addLogListener(logListener: LogListener) {
        logListeners.add(logListener)
        launch {
            logListener.log(buildLogText(), false)
        }
    }

    fun removeLogListener(logListener: LogListener) {
        logListeners.remove(logListener)
    }

    private suspend fun buildLogText(): String {
        var result = ""
        mutex.withLock {
            log.forEach {
                result += it.getStyledText()
            }
        }
        return result
    }

    class LogEntry(var date: Date, val priority: Int, val tag: String?, val message: String, var t: Throwable?, var count: Int = 1) {

        fun getStyledText(): String {
            var result = if (count > 1) "<font color=\"red\">[x$count]</font>" else ""
            if (!message.isBlank()) {
                result += getColoredText(getColor(priority), date, tag, message)
            }
            if (t != null) {
                result += getColoredText("red", date, tag, t.toString())
            }
            return result
        }

        private fun getColoredText(color: String, date: Date, tag: String?, message: String): String {
            return message.lines().foldIndexed("<font color=\"$color\">${simpleDateFormat.format(date)} ") { index, acc, c ->
                "${if (tag != null) "[$tag] " else ""}$acc$c${if (index == message.lines().size - 1) "" else LINE_BREAK}"
            } + "</font>$EXTERNAL_LINE_BREAK"
        }

        fun getColor(logPriority: Int): String = when (logPriority) {
            Log.DEBUG -> "gray"
            Log.WARN -> "#FF9800"
            Log.ERROR -> "red"
            Log.ASSERT -> "red"
            else -> "white"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LogEntry

            if (priority != other.priority) return false
            if (tag != other.tag) return false
            if (message != other.message) return false
            if (t != other.t) return false

            return true
        }

        override fun hashCode(): Int {
            var result = priority
            result = 31 * result + (tag?.hashCode() ?: 0)
            result = 31 * result + message.hashCode()
            result = 31 * result + (t?.hashCode() ?: 0)
            return result
        }


    }

    override fun onLogMessageWritten(logService: LoggingService, domain: String, level: LogLevel?, message: String) {
        Timber.tag(domain).log(level?.toInt() ?: Log.INFO, message)
    }

    interface LogListener {
        fun log(message: String, replaceLastLine: Boolean)
    }
}
