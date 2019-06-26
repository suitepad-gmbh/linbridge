package de.suitepad.linbridge.manager

import android.content.Context
import de.suitepad.linbridge.api.AudioConfiguration
import de.suitepad.linbridge.api.core.*
import org.linphone.core.*
import timber.log.Timber
import java.util.*
import org.linphone.core.LinphoneCoreException
import org.linphone.core.LinphoneProxyConfig
import org.linphone.core.LinphoneCore
import org.linphone.core.LinphoneCoreFactory
import org.linphone.core.LinphoneAddress



class LinbridgeManager(context: Context, val core: LinphoneCore) : OptionalCoreListener, IManager {

    var registrationState: LinphoneCore.RegistrationState? = null
    private var callEndReason: CallEndReason = CallEndReason.None

    val keepAliveTask = object : TimerTask() {
        override fun run() {
            core.iterate()
        }
    }

    var keepAliveTimer: Timer? = null

    init {
        val baseDir = context.filesDir.absolutePath
        core.setRootCA("$baseDir/rootca.pem")
        core.setRingback("$baseDir/ringback.wav")
        core.ring = "$baseDir/toymono.wav"

        core.clearAuthInfos()
        core.clearProxyConfigs()
        core.disableChat(Reason.NotImplemented)
        core.enableVideo(false, false)
        core.enableVideoMulticast(false)
        core.enableVideoMulticast(false)
        core.maxCalls = 1

        core.setUserAgent(BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME)
    }

    override fun start() {
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
    }

    override fun authenticate(host: String, port: Int, username: String, password: String, proxy: String?) {
        core.clearProxyConfigs()
        core.clearAuthInfos()

        val sipAddress = "sip:$username@$host:$port"

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
        val proxyAddress: LinphoneAddress = LinphoneCoreFactory.instance().createLinphoneAddress(sipProxy)
        proxyAddress.transport = LinphoneAddress.TransportType.LinphoneTransportUdp

        try {
            val address = LinphoneCoreFactory.instance().createLinphoneAddress(sipAddress)
            val auth = LinphoneCoreFactory.instance().createAuthInfo(username, password, null, address.domain)
            core.clearAuthInfos()
            core.addAuthInfo(auth)

            val proxycon = core.createProxyConfig(address.asStringUriOnly(), proxyAddress.toString(), null, true)
            core.clearProxyConfigs()
            core.addProxyConfig(proxycon)
            proxycon.done()
            core.setDefaultProxyConfig(proxycon)
            core.refreshRegisters()
        } catch (e: LinphoneCoreException) {
            Timber.e(e)
        }

    }

    override fun clearCredentials() {
        core.clearProxyConfigs()
        core.clearAuthInfos()
        core.refreshRegisters()
    }

    override fun call(destination: String): CallError? {
        if (!isRegistered()) {
            return CallError.NotAuthenticated
        }

        if (destination.startsWith("<sip") || destination.startsWith("sip")) {
            core.invite(destination)
        } else {
            core.invite(destination)
        }
        return null
    }

    override fun answerCall(): CallError? {
        if (core.currentCall == null) {
            return CallError.NoCallAvailable
        }

        core.acceptCall(core.currentCall)
        return null
    }

    override fun rejectCall(): CallError? {
        if (core.currentCall == null) {
            return CallError.NoCallAvailable
        }

        core.terminateAllCalls()
        return null
    }

    fun isRegistered(): Boolean = registrationState == LinphoneCore.RegistrationState.RegistrationOk

    override fun getCurrentAuthenticationState(): AuthenticationState? {
        return when (registrationState) {
            LinphoneCore.RegistrationState.RegistrationProgress -> AuthenticationState.Progress
            LinphoneCore.RegistrationState.RegistrationOk -> AuthenticationState.Ok
            LinphoneCore.RegistrationState.RegistrationCleared -> AuthenticationState.Cleared
            LinphoneCore.RegistrationState.RegistrationFailed -> AuthenticationState.Failed
            else -> null
        }
    }

    override fun getCurrentConfiguration(): AudioConfiguration {
        return core.getConfiguration()
    }

    override fun getCurrentCredentials(): Credentials? {
        if (core.authInfosList.isEmpty()) {
            return null
        }
        val info = core.authInfosList[0]
        val proxy = core.defaultProxyConfig
        return Credentials(
                info.domain.substringBefore(':'),
                info.domain.substringAfter(':').toIntOrNull() ?: 5060,
                info.username,
                info.password,
                proxy?.address?.asStringUriOnly()
        )
    }

    override fun getCallEndReason(): CallEndReason {
        return callEndReason
    }

    override fun sendDtmf(number: Char) {
        core.stopDtmf()
        core.playDtmf(number, -1)
        core.sendDtmf(number)
    }

    override fun stopDtmf() {
        core.stopDtmf()
    }

    override fun mute(muted: Boolean) {
        core.muteMic(muted)
    }

    override fun getCurrentCallDuration(): Int {
        return core.currentCall?.duration ?: -1
    }

    //<editor-fold desc="CoreListener">
    override fun registrationState(p0: LinphoneCore?, p1: LinphoneProxyConfig?, cstate: LinphoneCore.RegistrationState?, p3: String?) {
        registrationState = cstate
    }

    override fun callState(core: LinphoneCore?, call: LinphoneCall?, cstate: LinphoneCall.State?, message: String?) {
        callEndReason = call?.reason?.toString()?.let { CallEndReason.valueOf(it) } ?: CallEndReason.None
    }

    //</editor-fold>

}

fun LinphoneCore.configure(settings: AudioConfiguration) {
    setMicrophoneGain(settings.microphoneGain.toFloat())
    playbackGain = settings.speakerGain.toFloat()
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
}

fun LinphoneCore.enableCodecs(types: Array<AudioCodec>?) {
    audioCodecs.forEach { payloadType ->
        val audioCodec = AudioCodec.getAudioCodecByMimeAndRate(payloadType.mime, payloadType.rate)
        enablePayloadType(payloadType, types.isNullOrEmpty() && audioCodec != null //  if not specifying codecs to open and the codec is supported by bell
                || types?.contains(audioCodec) ?: false) // if codec is in the enabled codecs list
    }
}

fun LinphoneCore.getEnabledCodecs(): Array<AudioCodec>? {
    return audioCodecs.filter { isPayloadTypeEnabled(it) }.mapNotNull {
        AudioCodec.getAudioCodecByMimeAndRate(it.mime, it.rate)
    }.toTypedArray()
}

fun LinphoneCore.getConfiguration(): AudioConfiguration {
    return AudioConfiguration().also {
        it.microphoneGain = 0 // TODO: fix this
        it.speakerGain = playbackGain.toInt()
        it.echoCancellation = isEchoCancellationEnabled
        it.echoLimiter = isEchoLimiterEnabled
        it.echoLimiterSustain = config.getInt("sound", "el_sustain", 0)
        it.echoLimiterSpeakerThreshold = config.getFloat("sound", "el_thres", 0f)
        it.echoLimiterMicrophoneDecrease = config.getInt("sound", "el_force", 0)
        it.echoLimiterDoubleTalkDetection = config.getFloat("sound", "el_transmit_threshold", 0f)
        it.enabledCodecs = getEnabledCodecs()
    }
}
