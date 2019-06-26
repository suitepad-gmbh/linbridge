package de.suitepad.linbridge.manager

import org.linphone.core.*
import java.nio.ByteBuffer

interface OptionalCoreListener : LinphoneCoreListener {

    override fun authInfoRequested(p0: LinphoneCore?, p1: String?, p2: String?, p3: String?) {
    }

    override fun callEncryptionChanged(p0: LinphoneCore?, p1: LinphoneCall?, p2: Boolean, p3: String?) {
    }

    override fun displayMessage(p0: LinphoneCore?, p1: String?) {
    }

    override fun newSubscriptionRequest(p0: LinphoneCore?, p1: LinphoneFriend?, p2: String?) {
    }

    override fun callStatsUpdated(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneCallStats?) {
    }

    override fun isComposingReceived(p0: LinphoneCore?, p1: LinphoneChatRoom?) {
    }

    override fun fileTransferSend(p0: LinphoneCore?, p1: LinphoneChatMessage?, p2: LinphoneContent?, p3: ByteBuffer?, p4: Int): Int {
        return 0
    }

    override fun configuringStatus(p0: LinphoneCore?, p1: LinphoneCore.RemoteProvisioningState?, p2: String?) {
    }

    override fun fileTransferProgressIndication(p0: LinphoneCore?, p1: LinphoneChatMessage?, p2: LinphoneContent?, p3: Int) {
    }

    override fun notifyReceived(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneAddress?, p3: ByteArray?) {
    }

    override fun notifyReceived(p0: LinphoneCore?, p1: LinphoneEvent?, p2: String?, p3: LinphoneContent?) {
    }

    override fun displayStatus(p0: LinphoneCore?, p1: String?) {
    }

    override fun authenticationRequested(p0: LinphoneCore?, p1: LinphoneAuthInfo?, p2: LinphoneCore.AuthMethod?) {
    }

    override fun infoReceived(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneInfoMessage?) {
    }

    override fun notifyPresenceReceived(p0: LinphoneCore?, p1: LinphoneFriend?) {
    }

    override fun friendListCreated(p0: LinphoneCore?, p1: LinphoneFriendList?) {
    }

    override fun registrationState(p0: LinphoneCore?, p1: LinphoneProxyConfig?, p2: LinphoneCore.RegistrationState?, p3: String?) {
    }

    override fun dtmfReceived(p0: LinphoneCore?, p1: LinphoneCall?, p2: Int) {
    }

    override fun messageReceived(p0: LinphoneCore?, p1: LinphoneChatRoom?, p2: LinphoneChatMessage?) {
    }

    override fun transferState(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneCall.State?) {
    }

    override fun friendListRemoved(p0: LinphoneCore?, p1: LinphoneFriendList?) {
    }

    override fun subscriptionStateChanged(p0: LinphoneCore?, p1: LinphoneEvent?, p2: SubscriptionState?) {
    }

    override fun show(p0: LinphoneCore?) {
    }

    override fun callState(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneCall.State?, p3: String?) {
    }

    override fun messageReceivedUnableToDecrypted(p0: LinphoneCore?, p1: LinphoneChatRoom?, p2: LinphoneChatMessage?) {
    }

    override fun ecCalibrationStatus(p0: LinphoneCore?, p1: LinphoneCore.EcCalibratorStatus?, p2: Int, p3: Any?) {
    }

    override fun uploadProgressIndication(p0: LinphoneCore?, p1: Int, p2: Int) {
    }

    override fun networkReachableChanged(p0: LinphoneCore?, p1: Boolean) {
    }

    override fun displayWarning(p0: LinphoneCore?, p1: String?) {
    }

    override fun globalState(p0: LinphoneCore?, p1: LinphoneCore.GlobalState?, p2: String?) {
    }

    override fun uploadStateChanged(p0: LinphoneCore?, p1: LinphoneCore.LogCollectionUploadState?, p2: String?) {
    }

    override fun fileTransferRecv(p0: LinphoneCore?, p1: LinphoneChatMessage?, p2: LinphoneContent?, p3: ByteArray?, p4: Int) {
    }

    override fun publishStateChanged(p0: LinphoneCore?, p1: LinphoneEvent?, p2: PublishState?) {
    }
}
