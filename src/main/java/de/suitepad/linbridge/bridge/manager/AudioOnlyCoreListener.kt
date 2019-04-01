package de.suitepad.linbridge.bridge.manager

import org.linphone.core.*
import timber.log.Timber

/**
 * This class is meant to reduce the amount of methods to be implemented by a listener that's only
 * interested in audio capability
 */
abstract class AudioOnlyCoreListener : CoreListener {
    override fun onTransferStateChanged(lc: Core?, transfered: Call?, newCallState: Call.State?) {
    }

    override fun onFriendListCreated(lc: Core?, list: FriendList?) {
        Timber.i("onFriendListCreated: ")
    }

    override fun onNotifyPresenceReceivedForUriOrTel(lc: Core?, lf: Friend?, uriOrTel: String?, presenceModel: PresenceModel?) {
        Timber.i("onNotifyPresenceReceivedForUriOrTel: ")
    }

    override fun onChatRoomStateChanged(lc: Core?, cr: ChatRoom?, state: ChatRoom.State?) {
    }

    override fun onBuddyInfoUpdated(lc: Core?, lf: Friend?) {
    }

    override fun onNotifyReceived(lc: Core?, lev: Event?, notifiedEvent: String?, body: Content?) {
    }

    override fun onNewSubscriptionRequested(lc: Core?, lf: Friend?, url: String?) {
    }

    override fun onNotifyPresenceReceived(lc: Core?, lf: Friend?) {
    }

    override fun onMessageReceived(lc: Core?, room: ChatRoom?, message: ChatMessage?) {
    }

    override fun onFriendListRemoved(lc: Core?, list: FriendList?) {
    }

    override fun onQrcodeFound(lc: Core?, result: String?) {
    }

    override fun onReferReceived(lc: Core?, referTo: String?) {
    }

    override fun onIsComposingReceived(lc: Core?, room: ChatRoom?) {
    }

    override fun onLogCollectionUploadProgressIndication(lc: Core?, offset: Int, total: Int) {
    }

    override fun onVersionUpdateCheckResultReceived(lc: Core?, result: VersionUpdateCheckResult?, version: String?, url: String?) {
    }

    override fun onLogCollectionUploadStateChanged(lc: Core?, state: Core.LogCollectionUploadState?, info: String?) {
    }

    override fun onMessageReceivedUnableDecrypt(lc: Core?, room: ChatRoom?, message: ChatMessage?) {
    }
}