package de.suitepad.linbridge.bridge.manager

import android.content.Context
import de.suitepad.linbridge.R
import de.suitepad.linbridge.api.SIPConfiguration
import org.linphone.core.*
import timber.log.Timber
import java.io.File
import java.util.*

class LinphoneManager(val context: Context, val core: Core, val coreFactory: Factory) : IManager {

    val keepAliveTask = object : TimerTask() {
        override fun run() {
            core.iterate()
        }
    }
    var keepAliveTimer: Timer? = null

    override fun start() {
        val baseDir = context.filesDir.absolutePath
        copyIfNotExists(context, R.raw.rootca, "$baseDir/rootca.pem")
        copyIfNotExists(context, R.raw.ringback, "$baseDir/ringback.wav")
        copyIfNotExists(context, R.raw.toy_mono, "$baseDir/toymono.wav")
        copyIfNotExists(context, R.raw.lp_default, "$baseDir/linphonerc")

        core.clearProxyConfig()
        core.clearAllAuthInfo()

        core.start()
        iterate()

        core.rootCa = "$baseDir/rootca.pem"
        core.ringback = "$baseDir/ringback.wav"
        core.ring = "$baseDir/toymono.wav"

        Timber.i(core.config.dumpAsXml())

        core.disableChat(Reason.NotImplemented)
        core.enableVideoDisplay(false)
        core.enableVideoCapture(false)
        core.enableVideoMulticast(false)
        core.enableVideoPreview(false)

        core.sipTransportTimeout = 50
        core.transports = core.transports.apply {
            tlsPort = -1
            tcpPort = -1
        }

        core.setUserAgent("LinBridge", "1.0.0")
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

    override fun configure(settings: SIPConfiguration?) {
        core.micGainDb = settings?.microphoneGain?.toFloat() ?: 0f
        core.playbackGainDb = settings?.speakerGain?.toFloat() ?: 0f
        core.enableEchoCancellation(settings?.echoCancellation ?: false)
        core.enableEchoLimiter(settings?.echoLimiter ?: false)
    }

    override fun authenticate(host: String, port: Int, username: String, password: String, proxy: String?) {

        val sipAddress = "sip:$username@$host"
        val address: Address = coreFactory.createAddress(sipAddress)

        val authenticationInfo = coreFactory.createAuthInfo(address.username, null,
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
        val proxyAddress: Address = coreFactory.createAddress(sipProxy)
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

//        iterate()
    }

    fun call() {
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

}