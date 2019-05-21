package de.suitepad.linbridge

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import de.suitepad.linbridge.api.ILinbridgeListener
import de.suitepad.linbridge.api.SIPConfiguration
import de.suitepad.linbridge.api.core.AuthenticationState
import de.suitepad.linbridge.api.core.CallError
import de.suitepad.linbridge.api.core.Credentials
import de.suitepad.linbridge.dep.BridgeModule
import de.suitepad.linbridge.dep.BridgeServiceComponent
import de.suitepad.linbridge.dep.DaggerBridgeServiceComponent
import de.suitepad.linbridge.dep.ManagerModule
import de.suitepad.linbridge.dispatcher.IBridgeEventDispatcher
import de.suitepad.linbridge.manager.IManager
import org.linphone.core.Core
import timber.log.Timber
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.NullPointerException
import javax.inject.Inject

class BridgeService : Service(), IBridgeService {

    companion object {
        const val SERVICE_NAME = "de.suitepad.linbridge.BridgeService"
        const val EXTRA_ACTION = "ACTION"
    }

    lateinit var component: BridgeServiceComponent

    @Inject
    lateinit var linphoneManager: IManager

    @Inject
    lateinit var eventDispatcher: IBridgeEventDispatcher

    override fun onCreate() {
        super.onCreate()

        val baseDir = filesDir.absolutePath
        copyIfNotExists(this, R.raw.rootca, "$baseDir/rootca.pem")
        copyIfNotExists(this, R.raw.ringback, "$baseDir/ringback.wav")
        copyIfNotExists(this, R.raw.toy_mono, "$baseDir/toymono.wav")
        copyIfNotExists(this, R.raw.lp_default, "$baseDir/linphonerc")

        init()
        startForeground(1, Notification())
        startService()
    }

    private fun init() {
        component = DaggerBridgeServiceComponent.builder()
                .appComponent(BridgeApplication.getApplication(this).component)
                .bridgeModule(BridgeModule(this))
                .managerModule(ManagerModule(true))
                .build()

        component.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var result = super.onStartCommand(intent, flags, startId)
        intent?.let {
            try {
                IntentAction.valueOf(it.getStringExtra(EXTRA_ACTION)).routine.invoke(this, it.extras)
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
        if (credentials == null) {
            throw NullPointerException("credentials are null")
        }

        if (credentials.host == null) {
            throw NullPointerException("host is null")
        }

        val servicePort = if (credentials.port == 0) 5060 else credentials.port

        if (credentials.username == null) {
            throw NullPointerException("username is null")
        }

        if (credentials.password == null) {
            throw NullPointerException("password is null")
        }

        linphoneManager.authenticate(credentials.host, servicePort, credentials.username,
                credentials.password, credentials.proxy)
    }

    override fun updateConfig(configuration: SIPConfiguration?) {
        Timber.d("updating config")
        if (configuration == null) {
            return
        }
//        val listenerBackup = eventDispatcher.listener
        linphoneManager.configure(configuration)
//        linphoneManager.destroy()
//        init()
//        eventDispatcher.shouldReconfigure = false
//        eventDispatcher.listener = listenerBackup
//        linphoneManager.start()
    }

    override fun getConfig(): SIPConfiguration {
        return linphoneManager.getCurrentConfiguration()
    }

    override fun call(destination: String?): CallError? {
        if (destination == null) {
            throw NullPointerException("Destination can't be null")
        }

        return linphoneManager.call(destination)
    }

    override fun registerSipListener(listener: ILinbridgeListener?) {
        if (listener == null)
            throw NullPointerException("passed a null listener")

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

    override fun getCurrentCallDuration(): Int {
        return linphoneManager.getCurrentCallDuration()
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
