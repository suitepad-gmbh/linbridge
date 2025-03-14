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

    override fun onImeeUserRegistration(core: Core, status: Boolean, userId: String, info: String) {
        log("onImeeUserRegistration: ")
    }

    override fun onFriendListCreated(core: Core, friendList: FriendList) {
        log("onFriendListCreated: ")
    }

    override fun onSubscriptionStateChanged(core: Core, linphoneEvent: Event, state: SubscriptionState?) {
        log("onSubscriptionStateChanged: ")
    }

    override fun onNotifySent(core: Core, linphoneEvent: Event, body: Content?) {
        TODO("Not yet implemented")
    }

    override fun onNotifyReceived(core: Core, linphoneEvent: Event, notifiedEvent: String, body: Content?) {
        TODO("Not yet implemented")
    }

    override fun onSubscribeReceived(core: Core, linphoneEvent: Event, subscribeEvent: String, body: Content?) {
        TODO("Not yet implemented")
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

    override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState?, message: String) {
        log("onAccountRegistrationStateChanged: ")
    }

    override fun onDefaultAccountChanged(core: Core, account: Account?) {
        TODO("Not yet implemented")
    }

    override fun onAccountAdded(core: Core, account: Account) {
        TODO("Not yet implemented")
    }

    override fun onAccountRemoved(core: Core, account: Account) {
        TODO("Not yet implemented")
    }

    override fun onMessageWaitingIndicationChanged(core: Core, lev: Event, mwi: MessageWaitingIndication) {
        TODO("Not yet implemented")
    }

    override fun onSnapshotTaken(core: Core, filePath: String) {
        TODO("Not yet implemented")
    }

    override fun onNewAlertTriggered(core: Core, alert: Alert) {
        TODO("Not yet implemented")
    }

    override fun onBuddyInfoUpdated(core: Core, linphoneFriend: Friend) {
        log("onBuddyInfoUpdated: ")
    }

    override fun onNetworkReachable(core: Core, reachable: Boolean) {
        log("onNetworkReachable: ")
    }


    override fun onNewSubscriptionRequested(core: Core, linphoneFriend: Friend, url: String) {
        log("onNewSubscriptionRequested: ")
    }

    override fun onRegistrationStateChanged(core: Core, proxyConfig: ProxyConfig, cstate: RegistrationState?, message: String) {
        log("onRegistrationStateChanged: $cstate")
    }

    override fun onConferenceInfoReceived(core: Core, conferenceInfo: ConferenceInfo) {
        TODO("Not yet implemented")
    }

    override fun onPushNotificationReceived(core: Core, payload: String?) {
        TODO("Not yet implemented")
    }

    override fun onPreviewDisplayErrorOccurred(core: Core, errorCode: Int) {
        TODO("Not yet implemented")
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

    override fun onNewMessageReaction(
        core: Core,
        chatRoom: ChatRoom,
        message: ChatMessage,
        reaction: ChatMessageReaction
    ) {
        TODO("Not yet implemented")
    }

    override fun onReactionRemoved(core: Core, chatRoom: ChatRoom, message: ChatMessage, address: Address) {
        TODO("Not yet implemented")
    }

    override fun onMessagesReceived(core: Core, chatRoom: ChatRoom, messages: Array<out ChatMessage>) {
        TODO("Not yet implemented")
    }

    override fun onConferenceStateChanged(core: Core, conference: Conference, state: Conference.State?) {
        log("onConferenceStateChanged: ")
    }

    override fun onEcCalibrationResult(core: Core, status: EcCalibratorStatus?, delayMs: Int) {
        log("onEcCalibrationResult: ")
    }

    override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
        log("onAudioDeviceChanged: ")
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

    override fun onCallIdUpdated(core: Core, previousCallId: String, currentCallId: String) {
        log("onCallIdUpdated: ")
    }

    override fun onRemainingNumberOfFileTransferChanged(core: Core, downloadCount: Int, uploadCount: Int) {
        TODO("Not yet implemented")
    }

    override fun onQrcodeFound(core: Core, result: String?) {
        log("onQrcodeFound: ")
    }

    override fun onConfiguringStatus(core: Core, status: ConfiguringState?, message: String?) {
        log("onConfiguringStatus: ")
    }

    override fun onFirstCallStarted(core: Core) {
        log("onFirstCallStarted: ")
    }

    override fun onCallCreated(core: Core, call: Call) {
        log("onCallCreated: ")
    }

    override fun onPublishStateChanged(core: Core, linphoneEvent: Event, state: PublishState?) {
        log("onPublishStateChanged: ")
    }

    override fun onPublishReceived(core: Core, linphoneEvent: Event, publishEvent: String, body: Content?) {
        TODO("Not yet implemented")
    }

    override fun onAudioDevicesListUpdated(core: Core) {
        log("onAudioDevicesListUpdated: ")
    }

    override fun onCallEncryptionChanged(core: Core, call: Call, on: Boolean, authenticationToken: String?) {
        log("onCallEncryptionChanged: ")
    }

    override fun onCallSendMasterKeyChanged(core: Core, call: Call, masterKey: String?) {
        TODO("Not yet implemented")
    }

    override fun onCallReceiveMasterKeyChanged(core: Core, call: Call, masterKey: String?) {
        TODO("Not yet implemented")
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

    override fun onLastCallEnded(core: Core) {
        log("onLastCallEnded: ")
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

    override fun onReferReceived(core: Core, referToAddr: Address, customHeaders: Headers, content: Content?) {
        TODO("Not yet implemented")
    }

    override fun onCallGoclearAckSent(core: Core, call: Call) {
        TODO("Not yet implemented")
    }

    override fun onChatRoomEphemeralMessageDeleted(core: Core, chatRoom: ChatRoom) {
        log("onChatRoomEphemeralMessageDeleted: ")
    }

    override fun onMessageSent(core: Core, chatRoom: ChatRoom, message: ChatMessage) {
        log("onMessageSent: ")
    }

    override fun onChatRoomSessionStateChanged(core: Core, chatRoom: ChatRoom, state: Call.State?, message: String) {
        TODO("Not yet implemented")
    }

    fun log(content: String) {
        Timber.log(loggerLevel, content)
    }
}
