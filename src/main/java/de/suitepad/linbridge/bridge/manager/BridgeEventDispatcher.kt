package de.suitepad.linbridge.bridge.manager

import de.suitepad.linbridge.api.ILinSipListener
import de.suitepad.linbridge.api.core.AuthenticationState
import de.suitepad.linbridge.api.core.CallState
import org.linphone.core.*
import timber.log.Timber

class BridgeEventDispatcher : OptionalCoreListener, IBridgeLinphoneCoreListener {

    override var listener: ILinSipListener? = null

    override fun onSubscriptionStateChanged(lc: Core?, lev: Event?, state: SubscriptionState?) {
        Timber.i("subscription state changed to $state event name is ${lev?.name}")
    }

    override fun onCallStateChanged(lc: Core?, call: Call?, cstate: Call.State?, message: String?) {
        Timber.i("call state [$cstate]")
        if (cstate != null && call != null) {
            listener?.callStateChanged(CallState.valueOf(cstate.name), call.remoteAddress.displayName)
        }
    }

    override fun onAuthenticationRequested(lc: Core?, authInfo: AuthInfo?, method: AuthMethod?) {
        Timber.i("authentication requested $method")
    }

    override fun onNetworkReachable(lc: Core?, reachable: Boolean) {
        Timber.i("onNetworkReachable $reachable")
    }

    override fun onSubscribeReceived(lc: Core?, lev: Event?, subscribeEvent: String?, body: Content?) {
        Timber.i("onSubscribeReceived: $subscribeEvent")
    }

    override fun onRegistrationStateChanged(lc: Core?, cfg: ProxyConfig?, cstate: RegistrationState?, message: String?) {
        Timber.i("registration state changed [$cstate] $message")
        if (cstate != null) {
            listener?.authenticationStateChanged(AuthenticationState.valueOf(cstate.name))
        }
    }

    override fun onInfoReceived(lc: Core?, call: Call?, msg: InfoMessage?) {
        Timber.i("info message received: $msg")
    }

    override fun onCallStatsUpdated(lc: Core?, call: Call?, stats: CallStats?) {
        Timber.v("onCallStatsUpdated: call stats updated")
        Timber.v("onCallStatsUpdated: delay: ${stats?.roundTripDelay}")
        Timber.v("onCallStatsUpdated: upload: ${stats?.uploadBandwidth}")
        Timber.v("onCallStatsUpdated: download ${stats?.downloadBandwidth}")
    }

    override fun onConfiguringStatus(lc: Core?, status: ConfiguringState?, message: String?) {
        Timber.i("onConfiguringStatus: $status $message")
        lc?.config?.sync()
    }

    override fun onCallCreated(lc: Core?, call: Call?) {
        Timber.i("onCallCreated: ")
    }

    override fun onPublishStateChanged(lc: Core?, lev: Event?, state: PublishState?) {
        Timber.i("onPublishStateChanged: publish state changed to $state for event name ${lev?.name}")
    }

    override fun onGlobalStateChanged(lc: Core?, gstate: GlobalState?, message: String?) {
        Timber.i("onGlobalStateChanged: $gstate $message")
    }

    override fun onLogCollectionUploadStateChanged(lc: Core?, state: Core.LogCollectionUploadState?, info: String?) {
    }

}