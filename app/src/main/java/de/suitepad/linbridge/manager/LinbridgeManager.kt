package de.suitepad.linbridge.manager

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import de.suitepad.linbridge.BuildConfig
import de.suitepad.linbridge.api.AudioConfiguration
import de.suitepad.linbridge.api.core.*
import org.linphone.core.*
import org.linphone.core.tools.Log
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@ServiceScoped
class LinbridgeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val core: Core
) : OptionalCoreListener, IManager {

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
        configureMk4()

        Timber.i(core.config.dumpAsXml())
    }

    private fun bareMinimumConfig() {
        /*
        * Not that great
        * */
        core.mediastreamerFactory.setDeviceInfo(
            "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
            org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
        )
        core.isEchoCancellationEnabled = true
    }

    private fun configureMk4() {
        val TEST_ENTRY = 8
        when (TEST_ENTRY) {
            1 -> {
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.03f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                core.isEchoLimiterEnabled = true
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 1)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 300)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )

                /*
                * Slight reduction in noise. But not that great with the latest image
                * */
            }

            2 -> {
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.03f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Disable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 0)
                core.config.setInt("sound", "ec_tail_len", 300)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
                /*
                * Doesn't work at all
                * */
            }

            22 -> {
                Timber.i("HOLA: at 22")
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.03f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Enable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 300)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
                /*
                * "The 4th one" -> The best one so far was really nice
                * */
            }

            222 -> {
                Timber.i("HOLA: at 22")
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.2f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Enable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 300)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
                /*
                * Increased ng_thres dramatically.
                * The speech quality increased significantly as well.
                * however, sometimes, the audio was cut off. Like some words were cut off.
                * */
            }

            2222 -> {
                Timber.i("HOLA: at 22")
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.1f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Enable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 300)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
                /*
                * Different results on Burak's end and Ankit's end.
                * On ankit's end the audio was better heard but not at burak's end
                * */
            }

            22222 -> {
                Timber.i("HOLA: at 22")
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.06f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Enable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 300)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
            }



            3 -> {
                Timber.i("HOLA: TEST ENTRY 3")
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.03f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Change ec_tail_len to 150
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 150)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
                /*
                * Feels comparably better than 4th
                * */
            }

            4 -> {
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.03f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Change ec_tail_len to 150
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 0)
                core.config.setInt("sound", "ec_tail_len", 150)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
            }

            5 -> {
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 0)
                core.config.setInt("sound", "ec_tail_len", 300)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
                /*
                * mk4 -mk4 test: Speech quality is improved. However, a small noise starts as soon as the other end starts
                * to speak. This is better than the "4th Test" overall.
                * */
            }

            6 -> {
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.06f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Enable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 60)
                core.config.setInt("sound", "ec_frame_size", 128)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
                /*
                * Not that nice.
                * */
            }

            66 -> {
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.06f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Enable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 60)
                core.config.setInt("sound", "ec_frame_size", 256)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
                /*
                * Not that nice.
                * */
            }

            666 -> {
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.06f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Enable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 150)
                core.config.setInt("sound", "ec_frame_size", 256)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
                /*
                * Not that nice.
                * */
            }

            7 -> {
                Timber.i("HOLA: at 7")
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.06f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Enable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 200)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
            }

            8 -> {
                Timber.i("HOLA: at 8")
                core.config.setInt("sound", "noisegate", 1)
                core.config.setFloat("sound", "ng_thres", 0.06f)
                core.config.setFloat("sound", "ng_floorgain", 0.03f)

                /*
                * Disable echo limitter
                * Enable Android AGC (Automatic Gain control)
                * */
                core.isEchoLimiterEnabled = false
                core.isEchoCancellationEnabled = true
                core.config.setInt("sound", "echocancellation", 1)
                core.config.setInt("sound", "echolimiter", 0)
                core.config.setInt("sound", "agc", 1)
                core.config.setInt("sound", "ec_tail_len", 250)
                core.mediastreamerFactory.setDeviceInfo(
                    "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
                    org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
                )
            }

            else -> {

            }
        }
    }

//
//    Noise reduction
//    noisegate=1 : This means that noise reduction is turned on, and there will be background sound when it is not turned on
//    ng_thres=0.03: This means that the sound above the threshold can pass, which is used to judge which is noise
//    ng_floorgain=0.03: This indicates that the sound below the threshold is used for gain to compensate for the sound that is too small to be eaten

    private fun setOpenSlesDevice() {
        val slesOutputDevice =
            core.extendedAudioDevices.find {
                it.driverName == "openSLES" && it.hasCapability(AudioDevice.Capabilities.CapabilityPlay)
            }
        val slesInputDevice =
            core.extendedAudioDevices.find {
                it.driverName == "openSLES" && it.hasCapability(AudioDevice.Capabilities.CapabilityRecord)
            }

        if (slesOutputDevice != null) {
            Timber.i("HOLA: Output device was not null")
            core.defaultOutputAudioDevice = slesOutputDevice
        } else {
            Timber.i("HOLA: Output device was null")
        }

        if (slesInputDevice != null) {
            Timber.i("HOLA: Input device was not null")
            core.defaultInputAudioDevice = slesInputDevice
        } else {
            Timber.i("HOLA: Input device was null")
        }
    }

    private fun getMicrophoneDevice(
        output: Boolean = true,
        types: List<AudioDevice.Type>,
    ): AudioDevice? {
        val conference = core.conference
        val capability = if (output)
            AudioDevice.Capabilities.CapabilityPlay
        else
            AudioDevice.Capabilities.CapabilityRecord
        val preferredDriver = if (output) {
            core.defaultOutputAudioDevice?.driverName
        } else {
            core.defaultInputAudioDevice?.driverName
        }

        val extendedAudioDevices = core.extendedAudioDevices
        Log.i("[Audio Route Helper] Looking for an ${if (output) "output" else "input"} audio device with capability [$capability], driver name [$preferredDriver] and type [$types] in extended audio devices list (size ${extendedAudioDevices.size})")
        val foundAudioDevice = extendedAudioDevices.find {
            it.driverName == preferredDriver && types.contains(it.type) && it.hasCapability(capability)
        }
        val audioDevice = if (foundAudioDevice == null) {
            Log.w("[Audio Route Helper] Failed to find an audio device with capability [$capability], driver name [$preferredDriver] and type [$types]")
            extendedAudioDevices.find {
                types.contains(it.type) && it.hasCapability(capability)
            }
        } else {
            foundAudioDevice
        }

        return audioDevice
    }

    fun useBluetoothDevice() {
        val headphone = getMicrophoneDevice(types = arrayListOf(AudioDevice.Type.Bluetooth))
        if (headphone != null) {
            Timber.i("HOLA: Audio device is not null: ${headphone.deviceName} :: ${headphone.id} :: ${headphone.driverName}")
            core.playbackDevice = headphone.id
            core.defaultOutputAudioDevice = headphone
//            core.defaultInputAudioDevice = headphone
            core.ringerDevice
        } else {
            Timber.i("HOLA: Audio device was null")
        }

        if (AudioRouteUtils.isBluetoothAudioRouteAvailable(core)) {
            AudioRouteUtils.routeAudioToBluetooth(core = core)
        }
    }

    fun Core.tempConfig() {
        Timber.i("HOLA: Configuring tempConfig")
        micGainDb = 1f
        config.setInt("sound", "ec_tail_len", 300)
        config.setFloat("sound", "mic_gain_db", 2f)
        config.setFloat("sound", "playback_gain_db", 3f)
//        if (hasBuiltinEchoCanceller()) {
            isEchoCancellationEnabled = true
            isEchoLimiterEnabled = true
//        }

        if(hasBuiltinEchoCanceller()) {
            Timber.i("HOLA: HARDWARE ECHO CANCELLATION IS AVAILABLE")
        }

        core.mediastreamerFactory.setDeviceInfo(
            "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
            org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 250
        )
        core.mediastreamerFactory.setDeviceInfo(
            "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
            org.linphone.mediastream.Factory.DEVICE_HAS_CRAPPY_AAUDIO, 0, 250
        )
        core.mediastreamerFactory.setDeviceInfo(
            "alps", "tb8168p1_64_l_d4x_qy_fhd_bsp", "mt8168",
            org.linphone.mediastream.Factory.DEVICE_MCH265_LIMIT_DEQUEUE_OF_OUTPUT_BUFFERS, 0, 250
        )
//        org.linphone.mediastream.Factory.set
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

    override fun authenticate(
        host: String,
        port: Int,
        authId: String?,
        username: String,
        password: String,
        proxy: String?
    ) {
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
        currentCall.accept()
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
    override fun onRegistrationStateChanged(
        core: Core,
        proxyConfig: ProxyConfig,
        cstate: RegistrationState?,
        message: String
    ) {
        registrationState = cstate
    }

    override fun onCallStateChanged(core: Core, call: Call, cstate: Call.State?, message: String) {
        callEndReason = call.reason?.toString()?.let { CallEndReason.valueOf(it) } ?: CallEndReason.None
        val audioManager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)

        if (cstate == Call.State.IncomingReceived || cstate == Call.State.IncomingEarlyMedia) {
//            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            routeAudioToBluetooth(call)
        } else if (cstate == Call.State.OutgoingProgress) {
//            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            routeAudioToBluetooth(call)
        } else if (cstate == Call.State.StreamsRunning) {
//            routeAudioToBluetooth(call)
        } else if (cstate == Call.State.End || cstate == Call.State.Error || cstate == Call.State.Released) {
            Timber.i("HOLA: Call was ended")
//            audioManager.mode = AudioManager.MODE_NORMAL
        }
        super.onCallStateChanged(core, call, cstate, message)
    }

    //</editor-fold>

    fun routeAudioToBluetooth(call: Call?) {
//        if (core.callsNb == 1) {
//            if (AudioRouteUtils.isBluetoothAudioRouteAvailable(core)) {
//                AudioRouteUtils.routeAudioToBluetooth(call, core = core)
//            } else {
//                Timber.i("HOLA: Headset is not available")
//            }
//        }
    }

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
