package de.suitepad.linbridge.manager

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import de.suitepad.linbridge.api.AudioConfiguration
import de.suitepad.linbridge.api.core.*
import de.suitepad.linbridge.BuildConfig
import org.linphone.core.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@ServiceScoped
class LinbridgeManager @Inject constructor(
    @ApplicationContext context: Context,
    private val core: Core,
) : OptionalCoreListener, IManager {

    var registrationState: RegistrationState? = null

    private var callEndReason: CallEndReason = CallEndReason.None

    val keepAliveTask = object : TimerTask() {
        override fun run() {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isMicrophoneMute = false
            audioManager.isSpeakerphoneOn = false
            core.iterate()
        }
    }

    var keepAliveTimer: Timer? = null

    val defaultRingtonePath: String

    init {
        core.addListener(this)

        val baseDir = context.filesDir.absolutePath
        core.rootCa = "$baseDir/rootca.pem"
        core.ringback = "$baseDir/ringback.wav"
        defaultRingtonePath = "$baseDir/toymono.wav"
        core.ring = null
        core.incTimeout = 40

        core.clearAllAuthInfo()
        core.clearProxyConfig()
        core.disableChat(Reason.NotImplemented)
        core.isVideoDisplayEnabled = false
        core.isVideoCaptureEnabled = false
        core.isVideoMulticastEnabled = false
        core.isVideoPreviewEnabled = false

        core.setUserAgent(BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME)
        core.maybeConfigureDevice()

        Timber.i(core.config.dumpAsXml())
    }

    override fun start() {
        core.isMicEnabled = true
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
        clearCredentials()

        val sipAddress = "sip:$username@$host:$port"
        val address: Address? = try {
            Factory.instance().createAddress(sipAddress)
        } catch (e: IllegalStateException) {
            Timber.e(e, "couldn't connect using \"$sipAddress\"")
            clearCredentials()
            return
        }
        if (address == null) {
            Timber.w(IllegalArgumentException("Couldn't create address from $sipAddress"))
            return
        }

        val authenticationInfo = address.username
            ?.let { Factory.instance().createAuthInfo(it, authId, password, null, null, address.domain) }
            ?: return Timber.w(
                IllegalArgumentException("Couldn't read username from $address")
            )

        val proxyConfig = core.createProxyConfig()
        var sipProxy = "sip:"
        if (proxy.isNullOrBlank()) {
            sipProxy += host
        } else {
            if (!proxy.startsWith("sip:") && !proxy.startsWith("<sip:") &&
                !proxy.startsWith("sips:") && !proxy.startsWith("<sips:")
            ) {
                sipProxy += proxy
            } else {
                sipProxy = proxy
            }
        }
        val proxyAddress: Address? = Factory.instance().createAddress(sipProxy)
        if (proxyAddress == null) {
            Timber.w(IllegalArgumentException("couldn't create address from $sipProxy"))
            return
        }
        proxyAddress.transport = TransportType.Udp

        proxyConfig.isRegisterEnabled = true
        proxyConfig.serverAddr = proxyAddress.asStringUriOnly()
        proxyConfig.identityAddress = address
        proxyConfig.isQualityReportingEnabled = false
        proxyConfig.avpfMode = AVPFMode.Disabled

        core.addAuthInfo(authenticationInfo)
        core.addProxyConfig(proxyConfig)
        core.defaultProxyConfig = proxyConfig
        core.avpfMode = AVPFMode.Disabled
        core.refreshRegisters()
    }

    override fun clearCredentials() {
        val account = core.defaultProxyConfig ?: return
        account.isRegisterEnabled = false
        core.clearProxyConfig()
        core.clearAllAuthInfo()
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

        val address = if (destination.startsWith("<sip") || destination.startsWith("sip")) {
            destination
        } else {
            val address = core.defaultProxyConfig?.serverAddr ?: return CallError.NetworkUnreachable
            Factory.instance().createAddress(address)?.domain.let { host ->
                "sip:$destination@$host"
            }
        }

        Timber.i("calling $address")
        core.invite(address)
        return null
    }

    override fun answerCall(): CallError? {
        val currentCall = core.currentCall ?: return CallError.NoCallAvailable
        val params = core.createCallParams(null) ?: TODO()
        params.isAudioEnabled = true
        params.isVideoEnabled = true
        params.isMicEnabled = true
        params.isAudioEnabled = true
        params.audioDirection = MediaDirection.SendRecv
        params.isLowBandwidthEnabled = true
        params.inputAudioDevice = core.defaultInputAudioDevice
        currentCall.acceptWithParams(params)
        //currentCall.accept()
        return null
    }

    override fun rejectCall(): CallError? {
        val currentCall = core.currentCall ?: return CallError.NoCallAvailable
        currentCall.terminate()
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
        val domain = info.domain ?: return null
        return Credentials(
            domain.substringBefore(':'),
            domain.substringAfter(':').toIntOrNull() ?: 5060,
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
        core.isMicEnabled = !muted
    }

    override fun isMuted(): Boolean {
        return !core.isMicEnabled
    }

    override fun getCurrentCallDuration(): Int {
        return core.currentCall?.duration ?: -1
    }

    //<editor-fold desc="CoreListener">
    override fun onRegistrationStateChanged(core: Core, proxyConfig: ProxyConfig, cstate: RegistrationState?, message: String) {
        registrationState = cstate
    }

    override fun onSubscribeReceived(core: Core, linphoneEvent: Event, subscribeEvent: String, body: Content?) {
        Timber.i("onSubscribeReceived: $subscribeEvent")
    }

    override fun onReferReceived(core: Core, referToAddr: Address, customHeaders: Headers, content: Content?) {
        Timber.i("onReferReceived: $referToAddr")
    }

    override fun onConferenceInfoReceived(core: Core, conferenceInfo: ConferenceInfo) {
        Timber.i("onConferenceInfoReceived: $conferenceInfo")
    }

    override fun onPushNotificationReceived(core: Core, payload: String?) {
        Timber.i("onPushNotificationReceived: $payload")
    }

    override fun onPreviewDisplayErrorOccurred(core: Core, errorCode: Int) {
        Timber.i("onPreviewDisplayErrorOccurred: $errorCode")
    }

    override fun onRemainingNumberOfFileTransferChanged(core: Core, downloadCount: Int, uploadCount: Int) {
        Timber.i("onRemainingNumberOfFileTransferChanged: download $downloadCount upload $uploadCount")
    }

    override fun onNewMessageReaction(
        core: Core,
        chatRoom: ChatRoom,
        message: ChatMessage,
        reaction: ChatMessageReaction
    ) {
        Timber.i("onNewMessageReaction: $reaction")
    }

    override fun onReactionRemoved(core: Core, chatRoom: ChatRoom, message: ChatMessage, address: Address) {
        Timber.i("onReactionRemoved: $address")
    }

    override fun onMessagesReceived(core: Core, chatRoom: ChatRoom, messages: Array<out ChatMessage>) {
        Timber.i("onMessagesReceived: ${messages.size}")
    }

    override fun onChatRoomSessionStateChanged(core: Core, chatRoom: ChatRoom, state: Call.State?, message: String) {
        Timber.i("onChatRoomSessionStateChanged: $state $message")
    }

    override fun onCallGoclearAckSent(core: Core, call: Call) {
        Timber.i("onCallGoclearAckSent: $call")
    }

    override fun onCallSendMasterKeyChanged(core: Core, call: Call, masterKey: String?) {
        Timber.i("onCallSendMasterKeyChanged: $masterKey")
    }

    override fun onCallReceiveMasterKeyChanged(core: Core, call: Call, masterKey: String?) {
        Timber.i("onCallReceiveMasterKeyChanged: $masterKey")
    }

    override fun onNotifySent(core: Core, linphoneEvent: Event, body: Content?) {
        Timber.i("onNotifySent: ${linphoneEvent.name}")
    }

    override fun onPublishReceived(core: Core, linphoneEvent: Event, publishEvent: String, body: Content?) {
        Timber.i("onPublishReceived: $publishEvent")
    }

    override fun onDefaultAccountChanged(core: Core, account: Account?) {
        Timber.i("onDefaultAccountChanged: $account")
    }

    override fun onAccountAdded(core: Core, account: Account) {
        Timber.i("onAccountAdded: $account")
    }

    override fun onAccountRemoved(core: Core, account: Account) {
        Timber.i("onAccountRemoved: $account")
    }

    override fun onMessageWaitingIndicationChanged(core: Core, lev: Event, mwi: MessageWaitingIndication) {
        Timber.i("onMessageWaitingIndicationChanged: $mwi")
    }

    override fun onSnapshotTaken(core: Core, filePath: String) {
        Timber.i("onSnapshotTaken: $filePath")
    }

    override fun onNewAlertTriggered(core: Core, alert: Alert) {
        Timber.i("onNewAlertTriggered: $alert")
    }

    override fun onCallStateChanged(core: Core, call: Call, cstate: Call.State?, message: String) {
        Timber.i("::: incTimeout = ${core.incTimeout} ::: inCallTimeout = ${core.inCallTimeout}")
        super.onCallStateChanged(core, call, cstate, message)
        callEndReason = call.reason?.toString()?.let { CallEndReason.valueOf(it) } ?: CallEndReason.None
    }

    override fun onNotifyReceived(core: Core, linphoneEvent: Event, notifiedEvent: String, body: Content?) {
        Timber.i("onNotifyReceived: $notifiedEvent")
    }

    //</editor-fold>

}

fun Core.configure(settings: AudioConfiguration) {
    micGainDb = settings.microphoneGain.toFloat()
    playbackGainDb = settings.speakerGain.toFloat()
    if (hasBuiltinEchoCanceller()) {
        isEchoCancellationEnabled = settings.echoCancellation
    }
    isEchoLimiterEnabled = settings.echoLimiter
    //avpfMode = AVPFMode.Disabled
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
        payloadType.enable(
            (types.isNullOrEmpty() && audioCodec != null) || //  if not specifying codecs to open and the codec is supported by bell
                    types?.contains(audioCodec) ?: false
        ) // if codec is in the enabled codecs list
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
        it.echoCancellation = isEchoCancellationEnabled
        it.echoLimiter = isEchoLimiterEnabled
        it.echoLimiterSustain = config.getInt("sound", "el_sustain", 0)
        it.echoLimiterSpeakerThreshold = config.getFloat("sound", "el_thres", 0f)
        it.echoLimiterMicrophoneDecrease = config.getInt("sound", "el_force", 0)
        it.echoLimiterDoubleTalkDetection = config.getFloat("sound", "el_transmit_threshold", 0f)
        it.enabledCodecs = getEnabledCodecs()
    }
}

fun Core.maybeConfigureDevice() {
    when (Build.VERSION.SDK_INT) {
        Build.VERSION_CODES.Q -> {
            isEchoLimiterEnabled = false
            isEchoCancellationEnabled = true

            config.setInt("sound", "noisegate", 1)
            config.setFloat("sound", "ng_thres", 0.06f)
            config.setFloat("sound", "ng_floorgain", 0.03f)

            config.setInt("sound", "echocancellation", 1)
            config.setInt("sound", "echolimiter", 0)
            config.setInt("sound", "agc", 1)
            config.setInt("sound", "ec_tail_len", 250)
            mediastreamerFactory.setDeviceInfo(
                "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
            )
        }
    }
}