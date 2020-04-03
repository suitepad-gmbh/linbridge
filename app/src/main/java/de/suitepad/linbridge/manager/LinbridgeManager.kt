package de.suitepad.linbridge.manager

import android.content.Context
import de.suitepad.linbridge.api.AudioConfiguration
import de.suitepad.linbridge.api.core.*
import org.linphone.core.*
import timber.log.Timber
import java.lang.IllegalStateException
import java.util.*

class LinbridgeManager(context: Context, val core: Core) : OptionalCoreListener, IManager {

    var registrationState: RegistrationState? = null

    private var callEndReason: CallEndReason = CallEndReason.None

    val keepAliveTask = object : TimerTask() {
        override fun run() {
            core.iterate()
        }
    }

    var keepAliveTimer: Timer? = null

    val defaultRingtonePath: String

    init {
        val baseDir = context.filesDir.absolutePath
        core.rootCa = "$baseDir/rootca.pem"
        core.ringback = "$baseDir/ringback.wav"
        defaultRingtonePath = "$baseDir/toymono.wav"
        core.ring = null

        core.clearAllAuthInfo()
        core.clearProxyConfig()
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

    override fun configure(settings: AudioConfiguration) {
        core.configure(settings)
        // todo: this is just a migration step, replace this with always null ringtone in init code
        if (settings.shouldNotRing) {
            core.ring = null
        } else {
            core.ring = defaultRingtonePath
        }
    }

    override fun authenticate(host: String, port: Int, authId: String?, username: String, password: String, proxy: String?) {
        core.clearProxyConfig()
        core.clearAllAuthInfo()

        val sipAddress = "sip:$username@$host:$port"
        val address: Address = try {
            core.createAddress(sipAddress)
        } catch (e: IllegalStateException) {
            Timber.e(e, "couldn't connect using \"$sipAddress\"")
            clearCredentials()
            return
        }

        val authenticationInfo = core.createAuthInfo(address.username, authId,
                password, null, null, address.domain)
        core.addAuthInfo(authenticationInfo)

        val proxyConfig = core.createProxyConfig()
        var sipProxy = "sip:"
        if (proxy.isNullOrBlank()) {
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
        proxyConfig.enableQualityReporting(false)
        proxyConfig.qualityReportingCollector = null
        proxyConfig.qualityReportingInterval = 0
        proxyConfig.avpfMode = AVPFMode.Disabled

        core.addProxyConfig(proxyConfig)
        core.defaultProxyConfig = proxyConfig
        core.avpfMode = AVPFMode.Disabled
        core.refreshRegisters()
    }

    override fun clearCredentials() {
        core.clearProxyConfig()
        core.clearAllAuthInfo()
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

    override fun getCurrentAuthenticationState(): AuthenticationState? {
        return when (registrationState) {
            RegistrationState.Progress -> AuthenticationState.Progress
            RegistrationState.Ok -> AuthenticationState.Ok
            RegistrationState.Cleared -> AuthenticationState.Cleared
            RegistrationState.Failed -> AuthenticationState.Failed
            else -> null
        }
    }

    override fun getCurrentConfiguration(): AudioConfiguration {
        return core.getConfiguration()
    }

    override fun getCurrentCredentials(): Credentials? {
        if (core.authInfoList.isEmpty()) {
            return null
        }
        val info = core.authInfoList[0]
        val proxy = core.defaultProxyConfig
        return Credentials(
                info.domain.substringBefore(':'),
                info.domain.substringAfter(':').toIntOrNull() ?: 5060,
                info.username,
                info.password,
                proxy?.serverAddr,
                info.userid
        )
    }

    override fun getCallEndReason(): CallEndReason {
        return callEndReason
    }

    override fun sendDtmf(number: Char) {
        core.stopDtmf()
        core.playDtmf(number, -1)
        core.currentCall?.sendDtmf(number)
    }

    override fun stopDtmf() {
        core.stopDtmf()
    }

    override fun mute(muted: Boolean) {
        core.enableMic(!muted)
    }

    override fun isMuted(): Boolean {
        return !core.micEnabled()
    }

    override fun getCurrentCallDuration(): Int {
        return core.currentCall?.duration ?: -1
    }

    //<editor-fold desc="CoreListener">
    override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {
        registrationState = cstate
    }

    override fun onCallStateChanged(lc: Core?, call: Call?, cstate: Call.State?, message: String?) {
        super.onCallStateChanged(lc, call, cstate, message)
        callEndReason = call?.reason?.toString()?.let { CallEndReason.valueOf(it) } ?: CallEndReason.None
    }

    //</editor-fold>

}

fun Core.configure(settings: AudioConfiguration) {
    micGainDb = settings.microphoneGain.toFloat()
    playbackGainDb = settings.speakerGain.toFloat()
    enableEchoCancellation(settings.echoCancellation)
    enableEchoLimiter(settings.echoLimiter)
    config.setInt("sound", "el_sustain", settings.echoLimiterSustain)
    config.setString("sound", "el_type", "mic")
    config.setFloat("sound", "el_thres", settings.echoLimiterSpeakerThreshold)
    config.setInt("sound", "el_force", settings.echoLimiterMicrophoneDecrease)
    config.setFloat("sound", "el_transmit_threshold", settings.echoLimiterDoubleTalkDetection)
    config.setBool("misc", "add_missing_audio_codecs", settings.enabledCodecs.isEmpty())
    enableCodecs(settings.enabledCodecs)
    config.sync()
    Timber.v(config.dump())
}

fun Core.enableCodecs(types: Array<AudioCodec>?) {
    audioPayloadTypes.forEach { payloadType ->
        val audioCodec = AudioCodec.getAudioCodecByMimeAndRate(payloadType.mimeType, payloadType.clockRate)
        payloadType.enable((types.isNullOrEmpty() && audioCodec != null) || //  if not specifying codecs to open and the codec is supported by bell
                types?.contains(audioCodec) ?: false) // if codec is in the enabled codecs list
    }
}

fun Core.getEnabledCodecs(): Array<AudioCodec>? {
    return audioPayloadTypes.filter { it.enabled() }.mapNotNull {
        AudioCodec.getAudioCodecByMimeAndRate(it.mimeType, it.clockRate)
    }.toTypedArray()
}

fun Core.getConfiguration(): AudioConfiguration {
    return AudioConfiguration().also {
        it.microphoneGain = micGainDb.toInt()
        it.speakerGain = playbackGainDb.toInt()
        it.echoCancellation = echoCancellationEnabled()
        it.echoLimiter = echoLimiterEnabled()
        it.echoLimiterSustain = config.getInt("sound", "el_sustain", 0)
        it.echoLimiterSpeakerThreshold = config.getFloat("sound", "el_thres", 0f)
        it.echoLimiterMicrophoneDecrease = config.getInt("sound", "el_force", 0)
        it.echoLimiterDoubleTalkDetection = config.getFloat("sound", "el_transmit_threshold", 0f)
        it.enabledCodecs = getEnabledCodecs()
    }
}
