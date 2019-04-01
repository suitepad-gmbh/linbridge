package de.suitepad.linbridge.bridge

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.*
import de.suitepad.linbridge.api.ILinSipListener
import de.suitepad.linbridge.api.SIPConfiguration
import de.suitepad.linbridge.app.BridgeApplication
import de.suitepad.linbridge.bridge.dep.BridgeModule
import de.suitepad.linbridge.bridge.dep.BridgeServiceComponent
import de.suitepad.linbridge.bridge.dep.DaggerBridgeServiceComponent
import de.suitepad.linbridge.bridge.dep.ManagerModule
import de.suitepad.linbridge.bridge.manager.IBridgeLinphoneCoreListener
import de.suitepad.linbridge.bridge.manager.IManager
import timber.log.Timber
import java.lang.NullPointerException
import javax.inject.Inject

class BridgeService : Service(), IBridgeService {

    companion object {
        const val ACTION_START_SERVICE = "de.suitepad.linbridge.bridge.BridgeService.ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "de.suitepad.linbridge.bridge.BridgeService.ACTION_STOP_SERVICE"
        const val ACTION_AUTHENTICATE = "de.suitepad.linbridge.bridge.BridgeService.ACTION_AUTHENTICATE"

        const val EXTRA_SIP_SERVER = "SERVER"
        const val EXTRA_SIP_USERNAME = "USERNAME"
        const val EXTRA_SIP_PASSWORD = "PASSWORD"
        const val EXTRA_SIP_PORT = "PORT"
        const val EXTRA_SIP_PROXY = "PROXY"

        const val EXTRA_MICROPHONE_GAIN = "MICROPHONE_GAIN"
        const val EXTRA_SPEAKER_GAIN = "SPEAKER_GAIN"
        const val EXTRA_AEC_ENABLED = "AEC_ENABLED"
        const val EXTRA_EL_ENABLED = "EL_ENABLED"
        const val EXTRA_EL_MIC_REDUCTION = "EL_MICROPHONE_REDUCTION"
        const val EXTRA_EL_SPEAKER_THRESHOLD = "EL_SPEAKER_THRESHOLD"
        const val EXTRA_EL_SUSTAIN = "EL_SUSTAIN"
        const val EXTRA_EL_DOUBLETALK_THRESHOLD = "EL_DOUBLETALK_THRESHOLD"
        const val EXTRA_LIST_CODEC_ENABLED = "CODECS"
    }

    lateinit var component: BridgeServiceComponent

    @Inject
    lateinit var linphoneManager: IManager

    @Inject
    lateinit var linphoneCoreListener: IBridgeLinphoneCoreListener

    override fun onCreate() {
        super.onCreate()
        component = DaggerBridgeServiceComponent.builder()
                .appComponent(BridgeApplication.getApplication(this).component)
                .bridgeModule(BridgeModule(this))
                .managerModule(ManagerModule(true))
                .build()

        component.inject(this)

        linphoneManager.start()
        startForeground(1, Notification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var result = super.onStartCommand(intent, flags, startId)
        intent?.let {
            when (it.action) {
                ACTION_STOP_SERVICE -> {
                    Timber.d("stopping service ${Integer.toHexString(hashCode())}")
                    stopService()
                }
                ACTION_AUTHENTICATE -> {
                    authenticate(
                            it.getStringExtra(EXTRA_SIP_SERVER),
                            it.getIntExtra(EXTRA_SIP_PORT, 5060),
                            it.getStringExtra(EXTRA_SIP_USERNAME),
                            it.getStringExtra(EXTRA_SIP_PASSWORD),
                            it.getStringExtra(EXTRA_SIP_PROXY)
                    )
                }
            }
        }
        return result
    }

    override fun onDestroy() {
        linphoneManager.destroy()
        super.onDestroy()
    }

    override fun authenticate(host: String?, port: Int, username: String?, password: String?, proxy: String?) {
        if (host == null) {
            throw NullPointerException("host is null")
        }

        val servicePort = if (port == 0) 5060 else port

        if (username == null) {
            throw NullPointerException("username is null")
        }

        if (password == null) {
            throw NullPointerException("password is null")
        }

        linphoneManager.authenticate(host, servicePort, username, password, proxy)
    }

    override fun updateConfig(configuration: SIPConfiguration?) {
        linphoneManager.configure(configuration)
    }

    override fun getConfig(): SIPConfiguration {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun forceRegisterSipListener(listener: ILinSipListener?) {
        if (listener == null)
            throw NullPointerException("passed a null listener")

        linphoneCoreListener.listener = listener
    }

    override fun registerSipListener(listener: ILinSipListener?): Boolean {
        if (listener == null) {
            throw NullPointerException("passed a null listener")
        }

        if (linphoneCoreListener.listener != null) {
            return false
        }

        linphoneCoreListener.listener = listener
        return true
    }

    override fun stopService() {
        stopSelf()
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
