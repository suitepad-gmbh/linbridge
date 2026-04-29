package de.suitepad.linbridge.manager

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import de.suitepad.linbridge.BuildConfig
import de.suitepad.linbridge.api.AudioConfiguration
import de.suitepad.linbridge.api.core.AudioCodec
import de.suitepad.linbridge.api.core.AuthenticationState
import de.suitepad.linbridge.api.core.CallEndReason
import de.suitepad.linbridge.api.core.CallError
import de.suitepad.linbridge.api.core.Credentials
import de.suitepad.linbridge.di.LinphoneScope
import kotlinx.coroutines.launch
import org.linphone.core.AVPFMode
import org.linphone.core.Account
import org.linphone.core.Address
import org.linphone.core.Alert
import org.linphone.core.Call
import org.linphone.core.ChatMessage
import org.linphone.core.ChatMessageReaction
import org.linphone.core.ChatRoom
import org.linphone.core.ConferenceInfo
import org.linphone.core.Content
import org.linphone.core.Core
import org.linphone.core.Event
import org.linphone.core.Factory
import org.linphone.core.Headers
import org.linphone.core.MediaDirection
import org.linphone.core.MessageWaitingIndication
import org.linphone.core.ProxyConfig
import org.linphone.core.Reason
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import org.xbill.DNS.SRVRecord
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.O)
@ServiceScoped
class LinbridgeManager @Inject constructor(
    @ApplicationContext context: Context,
    private val core: Core,
) : OptionalCoreListener, IManager {

    var registrationState: RegistrationState? = null

    private var callEndReason: CallEndReason = CallEndReason.None

    private  var possibleSrvRecords : MutableList<SRVRecord> = mutableListOf()

    private var currentSrvIndex = 0

    private var isSrvRecordAvailable = false

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var micMuteSupported = true

    val keepAliveTask = object : TimerTask() {
        override fun run() {
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
        core.ring = defaultRingtonePath
        core.config.setInt("sound", "use_native_ringing", 0)
        core.config.setInt("sound", "disable_ringing", 1)
        core.incTimeout = 40

        core.clearAllAuthInfo()
        core.clearAccounts()
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
        core.setNativeRingingEnabled(false)
        core.isDnsSrvEnabled = true
        core.enableCodecs(null)
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
        if (settings.shouldNotRing) {
            core.ring = null
        } else {
            core.ring = defaultRingtonePath
        }
    }


    override suspend fun authenticate(
        host: String,
        port: Int,
        authId: String?,
        username: String,
        password: String,
        proxy: String?
    ) {
        if (possibleSrvRecords.isEmpty()) {
            possibleSrvRecords = DnsSrvLookupManager.lookupSrvRecordsSuspend("_sip._udp.$proxy").toMutableList()
            isSrvRecordAvailable = possibleSrvRecords.isNotEmpty()
        }


        val (effectiveProxy, shouldClearCredentials) = if (isSrvRecordAvailable) {
            val srvProxy = possibleSrvRecords[currentSrvIndex].target.toString().trimEnd('.')
            currentSrvIndex++
            srvProxy to false
        } else {
            (proxy ?: "$username@$host") to true
        }

        if (shouldClearCredentials) {
            clearCredentials()
        }

        val identity = Factory.instance().createAddress("sip:$username@$host")
        if (identity == null) {
            Timber.e("Failed to create identity address from sip:$username@$host")
            return
        }

        val authInfo = Factory.instance().createAuthInfo(
            username, authId, password, null, null, identity.domain
        )
        core.addAuthInfo(authInfo)

        val sipProxy = buildSipProxy(effectiveProxy, proxy, username, host)
        val serverAddress = Factory.instance().createAddress(sipProxy)
        if (serverAddress == null) {
            Timber.e("Failed to create proxy address from sip:${effectiveProxy ?: host}")
            return
        }

        serverAddress.port = port
        serverAddress.transport = TransportType.Udp

        val accountParams = core.createAccountParams().apply {
            this.identityAddress = identity
            this.serverAddress = serverAddress
            isRegisterEnabled = true
            expires = 600
            avpfMode = AVPFMode.Disabled
            isPublishEnabled = false
            isDialEscapePlusEnabled = false
            isQualityReportingEnabled = false
        }

        val account = core.createAccount(accountParams)
        core.addAccount(account)
        core.defaultAccount = account
        core.refreshRegisters()

        Timber.i("Authentication setup complete for $identity")
    }

    private fun buildSipProxy(
        resolvedProxy: String?,
        originalProxy: String?,
        username: String,
        host: String
    ): String {
        return buildString {
            append("sip:")
            if (originalProxy.isNullOrBlank()) {
                append("$username@$host")
            } else {
                if (!resolvedProxy.isNullOrBlank() &&
                    !resolvedProxy.startsWith("sip:") &&
                    !resolvedProxy.startsWith("<sip:") &&
                    !resolvedProxy.startsWith("sips:") &&
                    !resolvedProxy.startsWith("<sips:")
                ) {
                    append(resolvedProxy)
                } else {
                    append(resolvedProxy?.removePrefix("<")?.removePrefix(">"))
                }
            }
        }
    }

    override fun clearCredentials() {
        core.defaultAccount?.let {
            it.params.isRegisterEnabled = false
        }
        core.clearAccounts()
        core.clearAllAuthInfo()
    }

   /* override fun call(destination: String): CallError? {
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
//            val address = core.defaultProxyConfig?.serverAddr ?: return CallError.NetworkUnreachable
//            Factory.instance().createAddress(address)?.domain.let { host ->
//                "sip:$destination@$host"
//            }
            val domain = core.defaultAccount?.params?.serverAddress?.domain ?: return CallError.NetworkUnreachable
            "sip:$destination@$domain"
        }

        Timber.i("calling $address")
        core.invite(address)
        return null
    } */
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

       /* val address = if (destination.startsWith("<sip") || destination.startsWith("sip")) {
            destination
        } else {
            val address = core.defaultProxyConfig?.serverAddr ?: return CallError.NetworkUnreachable
            Factory.instance().createAddress(address)?.domain.let { host ->
                "sip:$destination@$host"
            }
            val domain = core.defaultAccount?.params?.serverAddress?.domain ?: return CallError.NetworkUnreachable
            "sip:$destination@$domain"
        } */

        val address = if (destination.startsWith("<sip") || destination.startsWith("sip")) {
            destination
        } else {
            val identity = core.defaultProxyConfig?.identityAddress ?: return CallError.NetworkUnreachable
            val domain = identity.domain ?: return CallError.NetworkUnreachable
            "sip:$destination@$domain"
        }
        Timber.i("calling $address")
        core.invite(address)
        return null
    }

    override fun answerCall(): CallError? {
        val currentCall = core.currentCall ?: return CallError.NoCallAvailable
        val params = core.createCallParams(null) ?: return CallError.NoCallAvailable
        params.isAudioEnabled = true
        params.isVideoEnabled = true
        params.isMicEnabled = true
        params.isAudioEnabled = true
        params.audioDirection = MediaDirection.SendRecv
        params.isLowBandwidthEnabled = true
        params.inputAudioDevice = core.defaultInputAudioDevice
        applyCallAudioState()
        currentCall.acceptWithParams(params)
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
            RegistrationState.Refreshing -> AuthenticationState.Progress
            else -> null
        }
    }

    override fun getCurrentConfiguration(): AudioConfiguration {
        return core.getConfiguration()
    }

    override fun getCurrentCredentials(): Credentials? {
        val account = core.defaultAccount ?: return null
        val info = core.authInfoList.firstOrNull() ?: return null
        return Credentials(
             info.domain?.substringBefore(":") ?: return null,
             info.domain?.substringAfter(":")?.toIntOrNull() ?: 5060,
             info.username,
            info.password,
            account.params.serverAddress?.asStringUriOnly(),
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

    override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState?, message: String) {
        if (state == RegistrationState.Failed && isSrvRecordAvailable) {
            registrationState = RegistrationState.Refreshing
            tryNextPossibleSRVRecord()
        } else {
            registrationState = state
        }

    }

    private fun tryNextPossibleSRVRecord() {
        if (currentSrvIndex <= possibleSrvRecords.size -1) {
            LinphoneScope.launch {
                authenticate(
                    host = core.defaultAccount?.params?.domain.toString(),
                    port = core.defaultAccount?.params?.serverAddress?.port ?: 5060,
                    authId = core.defaultAccount?.params?.identityAddress?.username,
                    username = core.defaultAccount?.params?.identityAddress?.username ?: "",
                    password = core.defaultAccount?.params?.identityAddress?.username ?: "",
                    proxy = core.defaultAccount?.params?.serverAddress?.asStringUriOnly()
                )
            }
        } else {
            Timber.i("No more SRV records to try")
            registrationState = RegistrationState.Failed
        }
    }
    @Deprecated("Deprecated in Java", ReplaceWith("TODO(\"Not yet implemented\")"))
    override fun onRegistrationStateChanged(
        core: Core,
        proxyConfig: ProxyConfig,
        state: RegistrationState?,
        message: String
    ) {
        Timber.i("error message: $message")

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

        when (cstate) {
            Call.State.Connected,
            Call.State.StreamsRunning -> applyCallAudioState()
            Call.State.End,
            Call.State.Released,
            Call.State.Error -> {
                audioManager.mode = AudioManager.MODE_NORMAL
                Timber.i("Audio mode set to MODE_NORMAL (call ended)")
            }
            else -> { /* no audio mode change needed */ }
        }
    }

    private fun applyCallAudioState() {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        if (micMuteSupported) {
            try {
                audioManager.isMicrophoneMute = false
            } catch (e: Exception) {
                micMuteSupported = false
                Timber.w(e, "Mic mute not supported by audio HAL, skipping future attempts")
            }
        }
        audioManager.isSpeakerphoneOn = false
        Timber.i("Audio state applied: MODE_IN_COMMUNICATION, mic unmuted, speaker off")
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
            if (types.isNullOrEmpty()) {
                audioCodec != null // enable all codecs known to the AudioCodec enum
            } else {
                types.contains(audioCodec) // enable only explicitly requested codecs
            }
        )
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
            // Specific configuration for MK4 devices (running Android 10)
            // needs to be paired with EngineeringMode mic gain = 200

            // gain settings (will be applied on top of EngineeringMode App gain settings)
            micGainDb = 3.0f
            playbackGainDb = 3.0f

            // misc settings
            isAdaptiveRateControlEnabled = true
            config.setString("sound", "el_type", "mic")

            // echo canceller settings
            isEchoCancellationEnabled = true
            isEchoLimiterEnabled = false

            // noise gate configuration
            // ideal settings as per test Burak/Frank 13-Jan-2026
            config.setInt("sound", "noisegate", 1)
            config.setFloat("sound", "ng_thres", 0.015f)
            config.setFloat("sound", "ng_floorgain", 0.5f)

            // more configuration
            config.setInt("sound", "echocancellation", 1)
            config.setInt("sound", "echolimiter", 0)
            config.setFloat("sound", "mic_gain_db", 3.0f);
            config.setFloat("sound", "playback_gain_db", 3.0f);
            config.setInt("sound", "agc", 0) // AGC off -> doesn't do much, can confuse the noise gate
            config.setInt("sound", "ec_tail_len", 250)
            mediastreamerFactory.setDeviceInfo(
                "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 250, 0
            )
        }
    }
}
