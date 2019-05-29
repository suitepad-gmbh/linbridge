package de.suitepad.linbridge

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import de.suitepad.linbridge.logger.LogCatcher
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import timber.log.Timber

class MainActivity : AppCompatActivity(), LogCatcher.LogListener {
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

    override fun log(message: String) {
        runBlocking(Dispatchers.Main) {
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

}