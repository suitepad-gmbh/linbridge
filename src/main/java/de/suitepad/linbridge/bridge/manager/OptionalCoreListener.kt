package de.suitepad.linbridge.bridge.manager

import org.linphone.core.*

interface OptionalCoreListener : CoreListener {

    override fun onTransferStateChanged(lc: Core?, transfered: Call?, newCallState: Call.State?) {

    }

    override fun onFriendListCreated(lc: Core?, list: FriendList?) {

    }

    override fun onSubscriptionStateChanged(lc: Core?, lev: Event?, state: SubscriptionState?) {
    }

    override fun onCallLogUpdated(lc: Core?, newcl: CallLog?) {

    }

    override fun onCallStateChanged(lc: Core?, call: Call?, cstate: Call.State?, message: String?) {

    }

    override fun onAuthenticationRequested(lc: Core?, authInfo: AuthInfo?, method: AuthMethod?) {

    }

    override fun onNotifyPresenceReceivedForUriOrTel(lc: Core?, lf: Friend?, uriOrTel: String?, presenceModel: PresenceModel?) {

    }

    override fun onChatRoomStateChanged(lc: Core?, cr: ChatRoom?, state: ChatRoom.State?) {

    }

    override fun onBuddyInfoUpdated(lc: Core?, lf: Friend?) {

    }

    override fun onNetworkReachable(lc: Core?, reachable: Boolean) {

    }

    override fun onNotifyReceived(lc: Core?, lev: Event?, notifiedEvent: String?, body: Content?) {

    }

    override fun onNewSubscriptionRequested(lc: Core?, lf: Friend?, url: String?) {

    }

    override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {

    }

    override fun onNotifyPresenceReceived(lc: Core?, lf: Friend?) {

    }

    override fun onEcCalibrationAudioInit(lc: Core?) {

    }

    override fun onMessageReceived(lc: Core?, room: ChatRoom?, message: ChatMessage?) {

    }

    override fun onEcCalibrationResult(lc: Core?, status: EcCalibratorStatus?, delayMs: Int) {

    }

    override fun onSubscribeReceived(lc: Core?, lev: Event?, subscribeEvent: String?, body: Content?) {

    }

    override fun onInfoReceived(lc: Core?, call: Call?, msg: InfoMessage?) {

    }

    override fun onCallStatsUpdated(lc: Core?, call: Call?, stats: CallStats?) {

    }

    override fun onFriendListRemoved(lc: Core?, list: FriendList?) {

    }

    override fun onReferReceived(lc: Core?, referTo: String?) {

    }

    override fun onQrcodeFound(lc: Core?, result: String?) {

    }

    override fun onConfiguringStatus(lc: Core?, status: ConfiguringState?, message: String?) {

    }

    override fun onCallCreated(lc: Core?, call: Call?) {

    }

    override fun onPublishStateChanged(lc: Core?, lev: Event?, state: PublishState?) {

    }

    override fun onCallEncryptionChanged(lc: Core?, call: Call?, on: Boolean, authenticationToken: String?) {

    }

    override fun onIsComposingReceived(lc: Core?, room: ChatRoom?) {

    }

    override fun onMessageReceivedUnableDecrypt(lc: Core?, room: ChatRoom?, message: ChatMessage?) {

    }

    override fun onLogCollectionUploadProgressIndication(lc: Core?, offset: Int, total: Int) {

    }

    override fun onVersionUpdateCheckResultReceived(lc: Core?, result: VersionUpdateCheckResult?, version: String?, url: String?) {

    }

    override fun onEcCalibrationAudioUninit(lc: Core?) {

    }

    override fun onGlobalStateChanged(lc: Core?, gstate: GlobalState?, message: String?) {

    }

    override fun onLogCollectionUploadStateChanged(lc: Core?, state: Core.LogCollectionUploadState?, info: String?) {

    }

    override fun onDtmfReceived(lc: Core?, call: Call?, dtmf: Int) {

    }

}
