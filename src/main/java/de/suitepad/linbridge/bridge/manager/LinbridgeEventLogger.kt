package de.suitepad.linbridge.bridge.manager

import android.util.Log
import org.linphone.core.*
import timber.log.Timber

class LinbridgeEventLogger(val loggerLevel: Int = Log.DEBUG) : CoreListener {

    override fun onTransferStateChanged(lc: Core?, transfered: Call?, newCallState: Call.State?) {
        log("onTransferStateChanged: ")
    }

    override fun onFriendListCreated(lc: Core?, list: FriendList?) {
        log("onFriendListCreated: ")
    }

    override fun onSubscriptionStateChanged(lc: Core?, lev: Event?, state: SubscriptionState?) {
        log("onSubscriptionStateChanged: ")
    }

    override fun onCallLogUpdated(lc: Core?, newcl: CallLog?) {
        log("onCallLogUpdated: ")
    }

    override fun onCallStateChanged(lc: Core?, call: Call?, cstate: Call.State?, message: String?) {
        log("onCallStateChanged: ")
    }

    override fun onAuthenticationRequested(lc: Core?, authInfo: AuthInfo?, method: AuthMethod?) {
        log("onAuthenticationRequested: ")
    }

    override fun onNotifyPresenceReceivedForUriOrTel(lc: Core?, lf: Friend?, uriOrTel: String?, presenceModel: PresenceModel?) {
        log("onNotifyPresenceReceivedForUriOrTel: ")
    }

    override fun onChatRoomStateChanged(lc: Core?, cr: ChatRoom?, state: ChatRoom.State?) {
        log("onChatRoomStateChanged: ")
    }

    override fun onBuddyInfoUpdated(lc: Core?, lf: Friend?) {
        log("onBuddyInfoUpdated: ")
    }

    override fun onNetworkReachable(lc: Core?, reachable: Boolean) {
        log("onNetworkReachable: ")
    }

    override fun onNotifyReceived(lc: Core?, lev: Event?, notifiedEvent: String?, body: Content?) {
        log("onNotifyReceived: ")
    }

    override fun onNewSubscriptionRequested(lc: Core?, lf: Friend?, url: String?) {
        log("onNewSubscriptionRequested: ")
    }

    override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {
        log("onRegistrationStateChanged: ")
    }

    override fun onNotifyPresenceReceived(lc: Core?, lf: Friend?) {
        log("onNotifyPresenceReceived: ")
    }

    override fun onEcCalibrationAudioInit(lc: Core?) {
        log("onEcCalibrationAudioInit: ")
    }

    override fun onMessageReceived(lc: Core?, room: ChatRoom?, message: ChatMessage?) {
        log("onMessageReceived: ")
    }

    override fun onEcCalibrationResult(lc: Core?, status: EcCalibratorStatus?, delayMs: Int) {
        log("onEcCalibrationResult: ")
    }

    override fun onSubscribeReceived(lc: Core?, lev: Event?, subscribeEvent: String?, body: Content?) {
        log("onSubscribeReceived: ")
    }

    override fun onInfoReceived(lc: Core?, call: Call?, msg: InfoMessage?) {
        log("onInfoReceived: ")
    }

    override fun onCallStatsUpdated(lc: Core?, call: Call?, stats: CallStats?) {
        log("onCallStatsUpdated: ")
    }

    override fun onFriendListRemoved(lc: Core?, list: FriendList?) {
        log("onFriendListRemoved: ")
    }

    override fun onReferReceived(lc: Core?, referTo: String?) {
        log("onReferReceived: ")
    }

    override fun onQrcodeFound(lc: Core?, result: String?) {
        log("onQrcodeFound: ")
    }

    override fun onConfiguringStatus(lc: Core?, status: ConfiguringState?, message: String?) {
        log("onConfiguringStatus: ")
    }

    override fun onCallCreated(lc: Core?, call: Call?) {
        log("onCallCreated: ")
    }

    override fun onPublishStateChanged(lc: Core?, lev: Event?, state: PublishState?) {
        log("onPublishStateChanged: ")
    }

    override fun onCallEncryptionChanged(lc: Core?, call: Call?, on: Boolean, authenticationToken: String?) {
        log("onCallEncryptionChanged: ")
    }

    override fun onIsComposingReceived(lc: Core?, room: ChatRoom?) {
        log("onIsComposingReceived: ")
    }

    override fun onMessageReceivedUnableDecrypt(lc: Core?, room: ChatRoom?, message: ChatMessage?) {
        log("onMessageReceivedUnableDecrypt: ")
    }

    override fun onLogCollectionUploadProgressIndication(lc: Core?, offset: Int, total: Int) {
        log("onLogCollectionUploadProgressIndication: ")
    }

    override fun onVersionUpdateCheckResultReceived(lc: Core?, result: VersionUpdateCheckResult?, version: String?, url: String?) {
        log("onVersionUpdateCheckResultReceived: ")
    }

    override fun onEcCalibrationAudioUninit(lc: Core?) {
        log("onEcCalibrationAudioUninit: ")
    }

    override fun onGlobalStateChanged(lc: Core?, gstate: GlobalState?, message: String?) {
        log("onGlobalStateChanged: ")
    }

    override fun onLogCollectionUploadStateChanged(lc: Core?, state: Core.LogCollectionUploadState?, info: String?) {
        log("onLogCollectionUploadStateChanged: ")
    }

    override fun onDtmfReceived(lc: Core?, call: Call?, dtmf: Int) {
        log("onDtmfReceived: ")
    }

    fun log(content: String) {
        Timber.log(loggerLevel, content)
    }

}