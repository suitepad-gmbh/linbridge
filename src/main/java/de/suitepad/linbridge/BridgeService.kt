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

        const val EXTRA_DESTINATION = "DESTINATION"
    }

    enum class Action {
        START,
        STOP,
        AUTHENTICATE,
        CALL,
        ANSWER,
        REJECT,
        CONFIG
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
            val action: Action
            try {
                action = Action.valueOf(it.getStringExtra(EXTRA_ACTION))
            } catch (e: Exception) {
                when (e) {
                    is IllegalArgumentException,
                    is IllegalStateException -> {
                        return result
                    }
                }
                throw e
            }
            when (action) {
                Action.START -> {
                    startService()
                }
                Action.STOP -> {
                    Timber.d("stopping service ${Integer.toHexString(hashCode())}")
                    stopService()
                }
                Action.AUTHENTICATE -> {
                    authenticate(
                            Credentials(
                                    it.getStringExtra(EXTRA_SIP_SERVER),
                                    it.getIntExtra(EXTRA_SIP_PORT, 5060),
                                    it.getStringExtra(EXTRA_SIP_USERNAME),
                                    it.getStringExtra(EXTRA_SIP_PASSWORD),
                                    it.getStringExtra(EXTRA_SIP_PROXY)
                            )
                    )
                }
                Action.CALL -> {
                    call(it.getStringExtra(EXTRA_DESTINATION))
                }
                Action.ANSWER -> {
                    answerCall()
                }
                Action.REJECT -> {
                    rejectCall()
                }
                Action.CONFIG -> {
                    updateConfig(SIPConfiguration().apply {
                        echoCancellation = it.getBooleanExtra(EXTRA_AEC_ENABLED, false)
                        echoLimiter = it.getBooleanExtra(EXTRA_EL_ENABLED, false)
                        echoLimiterDoubleTalkDetection = it.getFloatExtra(EXTRA_EL_DOUBLETALK_THRESHOLD, 1.0f)
                        echoLimiterMicrophoneDecrease = it.getIntExtra(EXTRA_EL_MIC_REDUCTION, 0)
                        echoLimiterSpeakerThreshold = it.getFloatExtra(EXTRA_EL_SPEAKER_THRESHOLD, 1.0f)
                        echoLimiterSustain = it.getIntExtra(EXTRA_EL_SUSTAIN, 0)
                        enabledCodecs = it.getStringArrayExtra(EXTRA_LIST_CODEC_ENABLED)
                        microphoneGain = it.getIntExtra(EXTRA_MICROPHONE_GAIN, 0)
                        speakerGain = it.getIntExtra(EXTRA_SPEAKER_GAIN, 0)
                    })
                }
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
        if (configuration == null) {
            return
        }
        val listenerBackup = eventDispatcher.listener
        linphoneManager.configure(configuration)
        linphoneManager.destroy()
        init()
        eventDispatcher.shouldReconfigure = false
        eventDispatcher.listener = listenerBackup
        linphoneManager.start()
    }

    override fun getConfig(): SIPConfiguration {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

fun SIPConfiguration.configure(core: Core) {
    core.micGainDb = microphoneGain.toFloat()
    core.playbackGainDb = speakerGain.toFloat()
    core.enableEchoCancellation(echoCancellation)
    core.enableEchoLimiter(echoLimiter)
    core.config.setInt("sound", "el_sustain", echoLimiterSustain)
    core.config.setString("sound", "el_type", "mic")
    core.config.setFloat("sound", "el_thres", echoLimiterSpeakerThreshold)
    core.config.setInt("sound", "el_force", echoLimiterMicrophoneDecrease)
    core.config.setFloat("sound", "el_transmit_threshold", echoLimiterDoubleTalkDetection)
    core.config.sync()
}
