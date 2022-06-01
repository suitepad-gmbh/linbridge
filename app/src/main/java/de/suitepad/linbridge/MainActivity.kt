package de.suitepad.linbridge

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.Html
import android.view.View
import android.widget.Toast
import com.sendgrid.SendGrid
import de.suitepad.linbridge.BuildConfig
import de.suitepad.linbridge.helper.LogsExportHelper
import de.suitepad.linbridge.logger.LogCatcher
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_sendlogs.view.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), LogCatcher.LogListener {

    companion object {
        const val SIZE_LIMIT = 2000
    }

    var cachedLog: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        getLogCatcher().addLogListener(this)
    }

    override fun onStop() {
        super.onStop()
        getLogCatcher().removeLogListener(this)
    }

    @Suppress("DEPRECATION")
    fun setLog(message: String) {
        logText.text = Html.fromHtml(message)
    }

    fun scrollToBottom() {
        logScroller.post {
            logScroller.fullScroll(View.FOCUS_DOWN)
        }
    }

    fun isAnchoredToBottom(): Boolean {
        return ((logScroller.scrollY + logScroller.measuredHeight) - logText.height) == 0
    }

    override fun log(message: String, replaceLastLine: Boolean) {
        runBlocking(Dispatchers.Main) {
            var logList = cachedLog.split(LogCatcher.EXTERNAL_LINE_BREAK)
            if (replaceLastLine) {
                cachedLog = logList.subList(0, logList.size - 2).fold("") { acc, s ->
                    acc + s + LogCatcher.EXTERNAL_LINE_BREAK
                }
                cachedLog = cachedLog.substringBeforeLast(LogCatcher.EXTERNAL_LINE_BREAK) + LogCatcher.EXTERNAL_LINE_BREAK
            } else if (logList.size > SIZE_LIMIT) {
                cachedLog = logList.subList(logList.size - SIZE_LIMIT, logList.size).fold("...") { acc, s ->
                    acc + LogCatcher.EXTERNAL_LINE_BREAK + s
                }
            }
            cachedLog += message
            val shouldScroll = isAnchoredToBottom()
            setLog(cachedLog)
            if (shouldScroll) {
                scrollToBottom()
            }
        }
    }

    fun getLogCatcher(): LogCatcher {
        return (application as BridgeApplication).logCatcher
    }

    fun sendButton(view: View) {
        val view = View.inflate(this, R.layout.dialog_sendlogs, null)
        val dialog = AlertDialog.Builder(this)
                .setNegativeButton("cancel") { dialog, which ->
                    dialog.dismiss()
                }.setPositiveButton("send") { dialog, which ->
                    GlobalScope.launch(Dispatchers.Main) {
                        LogsExportHelper(SendGrid(BuildConfig.SENDGRID_API_KEY)).also {
                            it.logs = cachedLog
                            it.hotelName = view.hotelName.text.toString()
                            it.description = view.logsDescription.text.toString()
                        }.sendIt()
                        Toast.makeText(this@MainActivity, "logs successfully uploaded", Toast.LENGTH_LONG).show()
                    }.start()
                    dialog.dismiss()
                }.setTitle("Send logs to SuitePad")
                .setView(view)
                .create()
        dialog.show()
    }

}
