package de.suitepad.linbridge.dispatcher

import de.suitepad.linbridge.api.ILinbridgeListener
import de.suitepad.linbridge.api.core.AuthenticationState
import de.suitepad.linbridge.api.core.CallState
import de.suitepad.linbridge.manager.OptionalCoreListener
import de.suitepad.linbridge.manager.configure
import org.linphone.core.*
import timber.log.Timber

class BridgeEventDispatcher : OptionalCoreListener, IBridgeEventDispatcher {

    override var listener: ILinbridgeListener? = null

    override var shouldReconfigure = true

    override fun subscriptionStateChanged(p0: LinphoneCore?, p1: LinphoneEvent?, p2: SubscriptionState?) {
        super.subscriptionStateChanged(p0, p1, p2)
        Timber.i("subscription state changed to $p2")
    }

    override fun callState(p0: LinphoneCore?, call: LinphoneCall?, cstate: LinphoneCall.State?, p3: String?) {
        Timber.i("call state [$cstate]")
        if (cstate != null && call != null) {
            val callState = CallState.valueOf(cstate.toString())
            callState.number = call.remoteAddress.userName
            callState.contactName = call.remoteAddress.displayName
            listener?.callStateChanged(callState)
        }
    }

    override fun registrationState(p0: LinphoneCore?, proxyConfig: LinphoneProxyConfig?, cstate: LinphoneCore.RegistrationState?, message: String?) {
        Timber.i("registration state changed [$cstate] $message")
        Timber.i("onRegistrationStateChanged: $listener")
        if (cstate != null) {
            listener?.authenticationStateChanged(when (cstate) {
                LinphoneCore.RegistrationState.RegistrationFailed -> AuthenticationState.Failed
                LinphoneCore.RegistrationState.RegistrationCleared -> AuthenticationState.Cleared
                LinphoneCore.RegistrationState.RegistrationOk -> AuthenticationState.Ok
                LinphoneCore.RegistrationState.RegistrationProgress -> AuthenticationState.Progress
                else -> AuthenticationState.Cleared
            })
        }
    }

    override fun globalState(lc: LinphoneCore?, globalState: LinphoneCore.GlobalState?, message: String?) {
        Timber.i("onGlobalStateChanged: $globalState $message")
        when (globalState?.toString()) {
            "GlobalConfiguring" -> {
                if (shouldReconfigure && lc != null) {
                    listener?.configuration?.let {
                        lc.configure(it)
                    }
                }
            }
            else -> {
                // do nothing
            }
        }
    }

}