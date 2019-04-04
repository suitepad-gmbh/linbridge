package de.suitepad.linbridge.manager

import android.content.Context
import de.suitepad.linbridge.api.SIPConfiguration
import de.suitepad.linbridge.api.core.CallError
import de.suitepad.linbridge.configure
import org.linphone.core.*
import timber.log.Timber
import java.util.*

class LinbridgeManager(context: Context, val core: Core) : OptionalCoreListener, IManager {

    var registrationState: RegistrationState? = null

    val keepAliveTask = object : TimerTask() {
        override fun run() {
            core.iterate()
        }
    }
    var keepAliveTimer: Timer? = null

    init {
        val baseDir = context.filesDir.absolutePath
        core.rootCa = "$baseDir/rootca.pem"
        core.ringback = "$baseDir/ringback.wav"
        core.ring = "$baseDir/toymono.wav"

        core.disableChat(Reason.NotImplemented)
        core.enableVideoDisplay(false)
        core.enableVideoCapture(false)
        core.enableVideoMulticast(false)
        core.enableVideoPreview(false)

        core.setUserAgent(BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME)

        Timber.i(core.config.dumpAsXml())
    }

    override fun start() {
        core.start()
        iterate()
    }

    private fun iterate() {
        if (keepAliveTimer == null) {
            keepAliveTimer = Timer("Linphone scheduler")
            keepAliveTimer!!.schedule(keepAliveTask, 0, 20)
        }
    }

    override fun destroy() {
        keepAliveTimer?.cancel()
    }

    override fun configure(settings: SIPConfiguration) {
        settings.configure(core)
    }

    override fun authenticate(host: String, port: Int, username: String, password: String, proxy: String?) {

        val sipAddress = "sip:$username@$host:$port"
        val address: Address = core.createAddress(sipAddress)

        val authenticationInfo = core.createAuthInfo(address.username, null,
                password, null, null, address.domain)
        core.addAuthInfo(authenticationInfo)

        val proxyConfig = core.createProxyConfig()
        var sipProxy = "sip:"
        if (proxy == null) {
            sipProxy += host
        } else {
            if (!proxy.startsWith("sip:") && !proxy.startsWith("<sip:") &&
                    !proxy.startsWith("sips:") && !proxy.startsWith("<sips:")) {
                sipProxy += proxy
            } else {
                sipProxy = proxy
            }
        }
        val proxyAddress: Address = core.createAddress(sipProxy)
        proxyAddress.transport = TransportType.Udp
        proxyConfig.enableRegister(true)
        proxyConfig.serverAddr = proxyAddress.asStringUriOnly()
        proxyConfig.identityAddress = address
        proxyConfig.route = null
        proxyConfig.avpfMode = AVPFMode.Enabled
        proxyConfig.avpfRrInterval = 0
        proxyConfig.enableQualityReporting(false)
        proxyConfig.qualityReportingCollector = null
        proxyConfig.qualityReportingInterval = 0

        core.addProxyConfig(proxyConfig)
        core.defaultProxyConfig = proxyConfig
        core.refreshRegisters()
    }

    override fun call(destination: String): CallError? {
        if (!core.isNetworkReachable) {
            return CallError.NetworkUnreachable
        }

        if (!isRegistered()) {
            return CallError.NotAuthenticated
        }

        if (core.inCall()) {
            return CallError.AlreadyInCall
        }

        val address = if (destination.startsWith("<sip") || destination.startsWith("sip"))
            destination
        else "sip:$destination@${core.defaultProxyConfig.domain}"

        core.invite(address)
        return null
    }

    override fun answerCall(): CallError? {
        if (core.currentCall == null) {
            return CallError.NoCallAvailable
        }

        core.currentCall.accept()
        return null
    }

    override fun rejectCall(): CallError? {
        if (core.currentCall == null) {
            return CallError.NoCallAvailable
        }

        core.currentCall.terminate()
        return null
    }

    fun isRegistered(): Boolean = registrationState == RegistrationState.Ok

    //<editor-fold desc="CoreListener">
    override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {
        registrationState = cstate
    }
    //</editor-fold>

}