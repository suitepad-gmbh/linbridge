package de.suitepad.linbridge.dispatcher

import de.suitepad.linbridge.api.ILinbridgeListener

interface IBridgeEventDispatcher {

    /**
     * a bound hosting app
     */
    var listener: ILinbridgeListener?

    /**
     * flag that indicates whether the hosting app should be asked about the configuration
     */
    var shouldReconfigure: Boolean
}