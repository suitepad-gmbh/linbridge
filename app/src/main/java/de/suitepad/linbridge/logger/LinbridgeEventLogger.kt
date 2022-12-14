package de.suitepad.linbridge.logger

import android.util.Log
import de.suitepad.linbridge.dep.ManagerModule
import org.linphone.core.*
import timber.log.Timber
import javax.inject.Inject

class LinbridgeEventLogger @Inject constructor(
    @ManagerModule.DebugFlag isDebug: Boolean,
    core: Core
) : CoreListener {

    val loggerLevel: Int = if (isDebug) Log.INFO else Log.DEBUG

    init {
        core.addListener(this)
    }

    override fun onTransferStateChanged(core: Core, transfered: Call, newCallState: Call.State?) {
        log("onTransferStateChanged: ")
    }


    override fun onFriendListCreated(core: Core, friendList: FriendList) {
        log("onFriendListCreated: ")
    }

    override fun onSubscriptionStateChanged(core: Core, linphoneEvent: Event, state: SubscriptionState?) {
        log("onSubscriptionStateChanged: ")
    }

    override fun onCallLogUpdated(core: Core, callLog: CallLog) {
        log("onCallLogUpdated: ")
    }

    override fun onCallStateChanged(core: Core, call: Call, cstate: Call.State?, message: String) {
        val payloadType = call.currentParams.usedAudioPayloadType
        log("onCallStateChanged: $cstate ${call.reason} ${payloadType?.mimeType} ${payloadType?.clockRate}")
    }

    override fun onAuthenticationRequested(core: Core, authInfo: AuthInfo, method: AuthMethod) {
        log("onAuthenticationRequested: ")
    }

    override fun onNotifyPresenceReceivedForUriOrTel(core: Core, linphoneFriend: Friend, uriOrTel: String, presenceModel: PresenceModel) {
        log("onNotifyPresenceReceivedForUriOrTel: ")
    }

    override fun onChatRoomStateChanged(core: Core, chatRoom: ChatRoom, state: ChatRoom.State?) {
        log("onChatRoomStateChanged: ")
    }

    override fun onBuddyInfoUpdated(core: Core, linphoneFriend: Friend) {
        log("onBuddyInfoUpdated: ")
    }

    override fun onNetworkReachable(core: Core, reachable: Boolean) {
        log("onNetworkReachable: ")
    }

    override fun onNotifyReceived(core: Core, linphoneEvent: Event, notifiedEvent: String, body: Content) {
        log("onNotifyReceived: ")
    }

    override fun onNewSubscriptionRequested(core: Core, linphoneFriend: Friend, url: String) {
        log("onNewSubscriptionRequested: ")
    }

    override fun onRegistrationStateChanged(core: Core, proxyConfig: ProxyConfig, cstate: RegistrationState?, message: String) {
        log("onRegistrationStateChanged: $cstate")
    }

    override fun onNotifyPresenceReceived(core: Core, linphoneFriend: Friend) {
        log("onNotifyPresenceReceived: ")
    }

    override fun onEcCalibrationAudioInit(core: Core) {
        log("onEcCalibrationAudioInit: ")
    }

    override fun onMessageReceived(core: Core, chatRoom: ChatRoom, message: ChatMessage) {
        log("onMessageReceived: ")
    }

    override fun onEcCalibrationResult(core: Core, status: EcCalibratorStatus?, delayMs: Int) {
        log("onEcCalibrationResult: ")
    }

    override fun onSubscribeReceived(core: Core, linphoneEvent: Event, subscribeEvent: String, body: Content) {
        log("onSubscribeReceived: ")
    }

    override fun onInfoReceived(core: Core, call: Call, message: InfoMessage) {
        log("onInfoReceived: ")
    }

    override fun onChatRoomRead(core: Core, chatRoom: ChatRoom) {
    }

    override fun onCallStatsUpdated(core: Core, call: Call, callStats: CallStats) {
        log("onCallStatsUpdated: ")
    }

    override fun onFriendListRemoved(core: Core, friendList: FriendList) {
        log("onFriendListRemoved: ")
    }

    override fun onReferReceived(core: Core, referTo: String) {
        log("onReferReceived: ")
    }


    override fun onQrcodeFound(core: Core, result: String?) {
        log("onQrcodeFound: ")
    }

    override fun onConfiguringStatus(core: Core, status: ConfiguringState?, message: String?) {
        log("onConfiguringStatus: ")
    }

    override fun onCallCreated(core: Core, call: Call) {
        log("onCallCreated: ")
    }

    override fun onPublishStateChanged(core: Core, linphoneEvent: Event, state: PublishState?) {
        log("onPublishStateChanged: ")
    }

    override fun onCallEncryptionChanged(core: Core, call: Call, on: Boolean, authenticationToken: String?) {
        log("onCallEncryptionChanged: ")
    }

    override fun onIsComposingReceived(core: Core, room: ChatRoom) {
        log("onIsComposingReceived: ")
    }

    override fun onMessageReceivedUnableDecrypt(core: Core, room: ChatRoom, message: ChatMessage) {
        log("onMessageReceivedUnableDecrypt: ")
    }

    override fun onLogCollectionUploadProgressIndication(core: Core, offset: Int, total: Int) {
        log("onLogCollectionUploadProgressIndication: ")
    }

    override fun onChatRoomSubjectChanged(core: Core, chatRoom: ChatRoom) {
    }


    override fun onVersionUpdateCheckResultReceived(core: Core, result: VersionUpdateCheckResult, version: String?, url: String?) {
        log("onVersionUpdateCheckResultReceived: ")
    }

    override fun onEcCalibrationAudioUninit(core: Core) {
        log("onEcCalibrationAudioUninit: ")
    }

    override fun onGlobalStateChanged(core: Core, gstate: GlobalState?, message: String) {
        log("onGlobalStateChanged: $gstate")
    }

    override fun onLogCollectionUploadStateChanged(core: Core, state: Core.LogCollectionUploadState?, info: String) {
        log("onLogCollectionUploadStateChanged: ")
    }

    override fun onDtmfReceived(core: Core, call: Call, dtmf: Int) {
        log("onDtmfReceived: ")
    }

    override fun onMessageSent(core: Core, chatRoom: ChatRoom, message: ChatMessage) {
        log("onMessageSent: ")
    }

    fun log(content: String) {
        Timber.log(loggerLevel, content)
    }
}
