package de.suitepad.linbridge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import de.suitepad.linbridge.api.ILinbridgeListener
import de.suitepad.linbridge.api.AudioConfiguration
import de.suitepad.linbridge.api.core.AuthenticationState
import de.suitepad.linbridge.api.core.CallEndReason
import de.suitepad.linbridge.api.core.CallError
import de.suitepad.linbridge.api.core.Credentials
import de.suitepad.linbridge.dispatcher.IBridgeEventDispatcher
import de.suitepad.linbridge.manager.IManager
import timber.log.Timber
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.inject.Inject

@AndroidEntryPoint
class BridgeService : Service(), IBridgeService {

    companion object {
        const val SERVICE_NAME = "de.suitepad.linbridge.BridgeService"
        const val EXTRA_ACTION = "ACTION"
        const val NOTIFICATION_CHANNEL_ID = "linbridge_channel"
    }

    @Inject lateinit var linphoneManager: IManager

    @Inject lateinit var eventDispatcher: IBridgeEventDispatcher

    override fun onCreate() {
        super.onCreate()

        val baseDir = filesDir.absolutePath
        copyIfNotExists(this, R.raw.rootca, "$baseDir/rootca.pem")
        copyIfNotExists(this, R.raw.ringback, "$baseDir/ringback.wav")
        copyIfNotExists(this, R.raw.toy_mono, "$baseDir/toymono.wav")
        copyIfNotExists(this, R.raw.lp_default, "$baseDir/linphonerc")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1, createNotification())
        }
        startService()
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setOngoing(true)
        builder.priority = NotificationCompat.PRIORITY_MIN
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE)

        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Linbridge",
            NotificationManager.IMPORTANCE_LOW
        )

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        if (intent != null) {
            try {
                val action = intent.getStringExtra(EXTRA_ACTION) ?: throw IllegalArgumentException("action missing")
                IntentAction.valueOf(action).routine.invoke(this, intent.extras)
            } catch (e: Exception) {
                when (e) {
                    is IllegalArgumentException,
                    is IllegalStateException -> {
                        return result
                    }
                }
                throw e
            }
        }
        return result
    }

    override fun onDestroy() {
        linphoneManager.destroy()
        super.onDestroy()
    }

    override fun startService() {
        linphoneManager.start()
    }

    override fun authenticate(credentials: Credentials?) {
        if (credentials == null ||
                credentials.host == null ||
                credentials.username == null ||
                credentials.password == null) {
            FirebaseCrashlytics.getInstance().setCustomKey("credentials", "None")
            linphoneManager.clearCredentials()
            return
        } else {
            FirebaseCrashlytics.getInstance().setCustomKey("credentials", "sip:${credentials.username}@${credentials.host}")
            linphoneManager.authenticate(
                    credentials.host,
                    if (credentials.port == 0) 5060 else credentials.port,
                    credentials.authId,
                    credentials.username,
                    credentials.password,
                    credentials.proxy
            )
        }
    }

    override fun updateConfig(configuration: AudioConfiguration?) {
        Timber.d("updating config")
        if (configuration == null) {
            return
        }
        linphoneManager.configure(configuration)
    }

    override fun getConfig(): AudioConfiguration {
        return linphoneManager.getCurrentConfiguration()
    }

    override fun call(destination: String?): CallError? {
        if (destination == null) {
            return null
        }

        return linphoneManager.call(destination)
    }

    override fun registerSipListener(listener: ILinbridgeListener?) {
        if (listener == null)
            throw IllegalArgumentException("passed a null listener")

        eventDispatcher.listener = listener
        Timber.d("registerSipListener: $listener")
    }

    override fun getCurrentCredentials(): Credentials? {
        return linphoneManager.getCurrentCredentials()
    }

    override fun getAuthenticationState(): AuthenticationState? {
        return linphoneManager.getCurrentAuthenticationState()
    }

    override fun stopService() {
        stopSelf()
    }

    override fun answerCall(): CallError? {
        return linphoneManager.answerCall()
    }

    override fun rejectCall(): CallError? {
        return linphoneManager.rejectCall()
    }

    override fun sendDtmf(number: Char) {
        linphoneManager.sendDtmf(number)
    }

    override fun stopDtmf() {
        linphoneManager.stopDtmf()
    }

    override fun mute(muted: Boolean) {
        linphoneManager.mute(muted)
    }

    override fun isMuted(): Boolean {
        return linphoneManager.isMuted()
    }

    override fun getCurrentCallDuration(): Int {
        return linphoneManager.getCurrentCallDuration()
    }

    override fun getCallEndReason(): CallEndReason {
        return linphoneManager.getCallEndReason()
    }

    override fun setUserId(id: String?) {
        if (id != null) {
            FirebaseCrashlytics.getInstance().setUserId(id)
        }
    }

    fun copyIfNotExists(context: Context, resource: Int, target: String) {
        val outputFile = File(target)
        if (!outputFile.exists()) {
            copyFromPackage(context, resource, target)
        }
    }

    fun copyFromPackage(context: Context, resource: Int, target: String) {
        val outputFile = File(target).outputStream()
        val stream = context.resources.openRawResource(resource)
        stream.copyTo(outputFile)
        outputFile.flush()
        outputFile.close()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private var _binder: IBridgeService.ILinBridgeBinder? = null
    override val binder: IBridgeService.ILinBridgeBinder
        get() {
            if (_binder == null) {
                _binder = IBridgeService.ILinBridgeBinder(this)
            }
            return _binder!!
        }


}
