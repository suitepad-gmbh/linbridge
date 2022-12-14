package de.suitepad.linbridge.manager

import org.linphone.core.*

interface OptionalCoreListener : CoreListener {

    override fun onTransferStateChanged(core: Core, transfered: Call, newCallState: Call.State?) {

    }

    override fun onFriendListCreated(core: Core, friendList: FriendList) {

    }

    override fun onSubscriptionStateChanged(core: Core, linphoneEvent: Event, state: SubscriptionState?) {
    }

    override fun onCallLogUpdated(core: Core, callLog: CallLog) {

    }

    override fun onCallStateChanged(core: Core, call: Call, cstate: Call.State?, message: String) {

    }

    override fun onAuthenticationRequested(core: Core, authInfo: AuthInfo, method: AuthMethod) {

    }

    override fun onNotifyPresenceReceivedForUriOrTel(core: Core, linphoneFriend: Friend, uriOrTel: String, presenceModel: PresenceModel) {

    }

    override fun onChatRoomStateChanged(core: Core, chatRoom: ChatRoom, state: ChatRoom.State?) {

    }

    override fun onBuddyInfoUpdated(core: Core, linphoneFriend: Friend) {

    }

    override fun onNetworkReachable(core: Core, reachable: Boolean) {

    }

    override fun onNotifyReceived(core: Core, linphoneEvent: Event, notifiedEvent: String, body: Content) {

    }

    override fun onNewSubscriptionRequested(core: Core, linphoneFriend: Friend, url: String) {

    }

    override fun onRegistrationStateChanged(core: Core, proxyConfig: ProxyConfig, cstate: RegistrationState?, message: String) {
    }

    override fun onNotifyPresenceReceived(core: Core, linphoneFriend: Friend) {

    }

    override fun onEcCalibrationAudioInit(core: Core) {

    }

    override fun onMessageReceived(core: Core, chatRoom: ChatRoom, message: ChatMessage) {

    }

    override fun onEcCalibrationResult(core: Core, status: EcCalibratorStatus?, delayMs: Int) {

    }

    override fun onSubscribeReceived(core: Core, linphoneEvent: Event, subscribeEvent: String, body: Content) {

    }

    override fun onInfoReceived(core: Core, call: Call, message: InfoMessage) {

    }

    override fun onCallStatsUpdated(core: Core, call: Call, callStats: CallStats) {

    }

    override fun onFriendListRemoved(core: Core, friendList: FriendList) {

    }

    override fun onReferReceived(core: Core, referTo: String) {

    }

    override fun onQrcodeFound(core: Core, result: String?) {

    }

    override fun onConfiguringStatus(core: Core, status: ConfiguringState?, message: String?) {

    }

    override fun onCallCreated(core: Core, call: Call) {

    }

    override fun onPublishStateChanged(core: Core, linphoneEvent: Event, state: PublishState?) {

    }

    override fun onCallEncryptionChanged(core: Core, call: Call, on: Boolean, authenticationToken: String?) {

    }

    override fun onIsComposingReceived(core: Core, chatRoom: ChatRoom) {

    }

    override fun onMessageReceivedUnableDecrypt(core: Core, chatRoom: ChatRoom, message: ChatMessage) {

    }

    override fun onLogCollectionUploadProgressIndication(core: Core, offset: Int, total: Int) {

    }

    override fun onVersionUpdateCheckResultReceived(core: Core, result: VersionUpdateCheckResult, version: String?, url: String?) {

    }

    override fun onEcCalibrationAudioUninit(core: Core) {

    }

    override fun onGlobalStateChanged(core: Core, gstate: GlobalState?, message: String) {

    }

    override fun onLogCollectionUploadStateChanged(core: Core, state: Core.LogCollectionUploadState?, info: String) {

    }

    override fun onDtmfReceived(core: Core, call: Call, dtmf: Int) {

    }

    override fun onChatRoomRead(core: Core, chatRoom: ChatRoom) {
    }

    override fun onChatRoomSubjectChanged(core: Core, chatRoom: ChatRoom) {
    }

    override fun onMessageSent(core: Core, chatRoom: ChatRoom, message: ChatMessage) {
    }



}
