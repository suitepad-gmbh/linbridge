package de.suitepad.linbridge.bridge.manager

import de.suitepad.linbridge.api.ILinSipListener
import org.linphone.core.*
import timber.log.Timber

class BridgeLinphoneCoreListener : AudioOnlyCoreListener(), IBridgeLinphoneCoreListener {

    override var listener: ILinSipListener? = null

    override fun onSubscriptionStateChanged(lc: Core?, lev: Event?, state: SubscriptionState?) {
        Timber.i("subscription state changed to $state event name is ${lev?.name}")
    }

    override fun onCallLogUpdated(lc: Core?, newcl: CallLog?) {
        // do nothing
    }

    override fun onCallStateChanged(lc: Core?, call: Call?, cstate: Call.State?, message: String?) {
        Timber.i("call state [$cstate]")
    }

    override fun onAuthenticationRequested(lc: Core?, authInfo: AuthInfo?, method: AuthMethod?) {
        Timber.i("authentication requested")
    }

    override fun onNetworkReachable(lc: Core?, reachable: Boolean) {
        Timber.i("onNetworkReachable $reachable")
    }

    override fun onSubscribeReceived(lc: Core?, lev: Event?, subscribeEvent: String?, body: Content?) {
        Timber.i("onSubscribeReceived: $subscribeEvent")
    }

    override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {
        Timber.i("registration state changed [$cstate] $message")
    }

    override fun onEcCalibrationAudioInit(lc: Core?) {
        Timber.i("EC calibration init")
    }

    override fun onEcCalibrationResult(lc: Core?, status: EcCalibratorStatus, delayMs: Int) {
        Timber.i("EC calibration ended status=$status delay=$delayMs")
    }

    override fun onInfoReceived(lc: Core?, call: Call, msg: InfoMessage) {
        Timber.i("info message received: $msg")
    }

    override fun onCallStatsUpdated(lc: Core?, call: Call?, stats: CallStats) {
        Timber.v("onCallStatsUpdated: call stats updated")
        Timber.v("onCallStatsUpdated: delay: ${stats.roundTripDelay}")
        Timber.v("onCallStatsUpdated: upload: ${stats.uploadBandwidth}")
        Timber.v("onCallStatsUpdated: download ${stats.downloadBandwidth}")
    }

    override fun onConfiguringStatus(lc: Core?, status: ConfiguringState?, message: String?) {
        Timber.i("onConfiguringStatus: $status $message")
        lc?.config?.sync()
    }

    override fun onCallCreated(lc: Core?, call: Call?) {
        Timber.i("onCallCreated: ")
    }

    override fun onPublishStateChanged(lc: Core?, lev: Event?, state: PublishState?) {
        Timber.i("onPublishStateChanged: publish state changed to $state for event name ${lev?.name}")
    }

    override fun onCallEncryptionChanged(lc: Core?, call: Call?, on: Boolean, authenticationToken: String?) {
        Timber.i("onCallEncryptionChanged: enabled=$on, token: $authenticationToken")
    }

    override fun onLogCollectionUploadProgressIndication(lc: Core?, offset: Int, total: Int) {
    }

    override fun onEcCalibrationAudioUninit(lc: Core?) {
        Timber.i("onEcCalibrationAudioUninit: ")
    }

    override fun onGlobalStateChanged(lc: Core?, gstate: GlobalState?, message: String?) {
        Timber.i("onGlobalStateChanged: $gstate $message")
    }

    override fun onLogCollectionUploadStateChanged(lc: Core?, state: Core.LogCollectionUploadState?, info: String?) {
    }

    override fun onDtmfReceived(lc: Core?, call: Call?, dtmf: Int) {
        Timber.i("onDtmfReceived: $dtmf")
    }

}