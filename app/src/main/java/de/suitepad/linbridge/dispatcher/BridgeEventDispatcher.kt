package de.suitepad.linbridge.dispatcher

import dagger.hilt.android.scopes.ServiceScoped
import de.suitepad.linbridge.api.ILinbridgeListener
import de.suitepad.linbridge.api.core.AuthenticationState
import de.suitepad.linbridge.api.core.CallState
import de.suitepad.linbridge.manager.OptionalCoreListener
import de.suitepad.linbridge.manager.configure
import org.linphone.core.*
import timber.log.Timber
import javax.inject.Inject

@ServiceScoped
class BridgeEventDispatcher @Inject constructor() : OptionalCoreListener, IBridgeEventDispatcher {

    override var listener: ILinbridgeListener? = null

    override var shouldReconfigure = true

    override fun onSubscriptionStateChanged(core: Core, linphoneEvent: Event, state: SubscriptionState?) {
        Timber.i("subscription state changed to $state event name is ${linphoneEvent.name}")
    }

    override fun onCallStateChanged(core: Core, call: Call, cstate: Call.State?, message: String) {
        Timber.i("call state [$cstate]")
        if (cstate != null) {
            val callState = CallState.valueOf(cstate.name)
            callState.number = call.remoteAddress.username
            callState.contactName = call.remoteAddress.displayName
            listener?.callStateChanged(callState)
        }
    }

    override fun onAuthenticationRequested(core: Core, authInfo: AuthInfo, method: AuthMethod) {
        Timber.i("authentication requested $method")
    }

    override fun onNetworkReachable(core: Core, reachable: Boolean) {
        Timber.i("onNetworkReachable $reachable")
    }

    override fun onNotifyReceived(core: Core, linphoneEvent: Event, notifiedEvent: String, body: Content?) {
        Timber.i("onNotifyReceived: ${linphoneEvent.name}")
    }

    override fun onSubscribeReceived(core: Core, linphoneEvent: Event, subscribeEvent: String, body: Content?) {
       Timber.i("onSubscribeReceived: $subscribeEvent")
    }

    override fun onRegistrationStateChanged(core: Core, proxyConfig: ProxyConfig, cstate: RegistrationState?, message: String) {
        Timber.i("registration state changed [$cstate] $message")
        Timber.i("onRegistrationStateChanged: $listener")
        if (cstate != null) {
            listener?.authenticationStateChanged(AuthenticationState.valueOf(cstate.name))
        }
    }

    override fun onInfoReceived(core: Core, call: Call, message: InfoMessage) {
        Timber.i("info message received: $)")
    }

    override fun onCallStatsUpdated(core: Core, call: Call, callStats: CallStats) {
        Timber.v("onCallStatsUpdated: call stats updated")
        Timber.v("onCallStatsUpdated: delay: ${callStats.roundTripDelay}")
        Timber.v("onCallStatsUpdated: upload: ${callStats.uploadBandwidth}")
        Timber.v("onCallStatsUpdated: download ${callStats.downloadBandwidth}")
    }

    override fun onReferReceived(core: Core, referToAddr: Address, customHeaders: Headers, content: Content?) {
        Timber.i("onReferReceived: $referToAddr")
    }

    override fun onConfiguringStatus(core: Core, status: ConfiguringState?, message: String?) {
        Timber.i("onConfiguringStatus: $status $message")
    }

    override fun onCallCreated(core: Core, call: Call) {
        Timber.i("onCallCreated: ")
    }

    override fun onPublishStateChanged(core: Core, linphoneEvent: Event, state: PublishState?) {
        Timber.i("onPublishStateChanged: publish state changed to $state for event name ${linphoneEvent.name}")
    }

    override fun onGlobalStateChanged(core: Core, gstate: GlobalState?, message: String) {
        Timber.i("onGlobalStateChanged: $gstate $message")
        when (gstate) {
            GlobalState.Configuring -> {
                if (shouldReconfigure) {
                    listener?.configuration?.let {
                        core.configure(it)
                    }
                }
            }
            else -> {
                // do nothing
            }
        }
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
}
