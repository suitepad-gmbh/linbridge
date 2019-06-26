package de.suitepad.linbridge.logger

import android.util.Log
import org.linphone.core.*
import timber.log.Timber
import java.nio.ByteBuffer

class LinbridgeEventLogger(val loggerLevel: Int = Log.DEBUG) : LinphoneCoreListener {

    override fun authInfoRequested(p0: LinphoneCore?, p1: String?, p2: String?, p3: String?) {
        log("authInfoRequested")
    }

    override fun callEncryptionChanged(p0: LinphoneCore?, p1: LinphoneCall?, p2: Boolean, p3: String?) {
        log("callEncryptionChanged")
    }

    override fun displayMessage(p0: LinphoneCore?, p1: String?) {
        log("displayMessage")
    }

    override fun newSubscriptionRequest(p0: LinphoneCore?, p1: LinphoneFriend?, p2: String?) {
        log("newSubscriptionRequest")
    }

    override fun callStatsUpdated(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneCallStats?) {
        log("callStatsUpdated")
    }

    override fun isComposingReceived(p0: LinphoneCore?, p1: LinphoneChatRoom?) {
        log("isComposingReceived")
    }

    override fun fileTransferSend(p0: LinphoneCore?, p1: LinphoneChatMessage?, p2: LinphoneContent?, p3: ByteBuffer?, p4: Int): Int {
        log("fileTransferSend")
        return 0
    }

    override fun configuringStatus(p0: LinphoneCore?, p1: LinphoneCore.RemoteProvisioningState?, p2: String?) {
        log("configuringStatus")
    }

    override fun fileTransferProgressIndication(p0: LinphoneCore?, p1: LinphoneChatMessage?, p2: LinphoneContent?, p3: Int) {
        log("fileTransferProgressIndication")
    }

    override fun notifyReceived(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneAddress?, p3: ByteArray?) {
        log("notifyReceived")
    }

    override fun notifyReceived(p0: LinphoneCore?, p1: LinphoneEvent?, p2: String?, p3: LinphoneContent?) {
        log("notifyReceived")
    }

    override fun displayStatus(p0: LinphoneCore?, p1: String?) {
        log("displayStatus")
    }

    override fun authenticationRequested(p0: LinphoneCore?, p1: LinphoneAuthInfo?, p2: LinphoneCore.AuthMethod?) {
        log("authenticationRequested")
    }

    override fun infoReceived(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneInfoMessage?) {
        log("infoReceived")
    }

    override fun notifyPresenceReceived(p0: LinphoneCore?, p1: LinphoneFriend?) {
        log("notifyPresenceReceived")
    }

    override fun friendListCreated(p0: LinphoneCore?, p1: LinphoneFriendList?) {
        log("friendListCreated")
    }

    override fun registrationState(p0: LinphoneCore?, p1: LinphoneProxyConfig?, p2: LinphoneCore.RegistrationState?, p3: String?) {
        log("registrationState")
    }

    override fun dtmfReceived(p0: LinphoneCore?, p1: LinphoneCall?, p2: Int) {
        log("dtmfReceived")
    }

    override fun messageReceived(p0: LinphoneCore?, p1: LinphoneChatRoom?, p2: LinphoneChatMessage?) {
        log("messageReceived")
    }

    override fun transferState(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneCall.State?) {
        log("transferState")
    }

    override fun friendListRemoved(p0: LinphoneCore?, p1: LinphoneFriendList?) {
        log("friendListRemoved")
    }

    override fun subscriptionStateChanged(p0: LinphoneCore?, p1: LinphoneEvent?, p2: SubscriptionState?) {
        log("subscriptionStateChanged")
    }

    override fun show(p0: LinphoneCore?) {
        log("show")
    }

    override fun callState(p0: LinphoneCore?, p1: LinphoneCall?, p2: LinphoneCall.State?, p3: String?) {
        log("callState")
    }

    override fun messageReceivedUnableToDecrypted(p0: LinphoneCore?, p1: LinphoneChatRoom?, p2: LinphoneChatMessage?) {
        log("messageReceivedUnableToDecrypted")
    }

    override fun ecCalibrationStatus(p0: LinphoneCore?, p1: LinphoneCore.EcCalibratorStatus?, p2: Int, p3: Any?) {
        log("ecCalibrationStatus")
    }

    override fun uploadProgressIndication(p0: LinphoneCore?, p1: Int, p2: Int) {
        log("uploadProgressIndication")
    }

    override fun networkReachableChanged(p0: LinphoneCore?, p1: Boolean) {
        log("networkReachableChanged")
    }

    override fun displayWarning(p0: LinphoneCore?, p1: String?) {
        log("displayWarning")
    }

    override fun globalState(p0: LinphoneCore?, p1: LinphoneCore.GlobalState?, p2: String?) {
        log("globalState")
    }

    override fun uploadStateChanged(p0: LinphoneCore?, p1: LinphoneCore.LogCollectionUploadState?, p2: String?) {
        log("uploadStateChanged")
    }

    override fun fileTransferRecv(p0: LinphoneCore?, p1: LinphoneChatMessage?, p2: LinphoneContent?, p3: ByteArray?, p4: Int) {
        log("fileTransferRecv")
    }

    override fun publishStateChanged(p0: LinphoneCore?, p1: LinphoneEvent?, p2: PublishState?) {
        log("publishStateChanged")
    }


    fun log(content: String) {
        Timber.log(loggerLevel, content)
    }

}