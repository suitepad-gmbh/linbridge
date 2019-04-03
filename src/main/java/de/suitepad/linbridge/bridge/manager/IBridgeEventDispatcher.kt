package de.suitepad.linbridge.bridge.manager

import de.suitepad.linbridge.api.ILinbridgeListener

interface IBridgeEventDispatcher {

    var listener: ILinbridgeListener?

}