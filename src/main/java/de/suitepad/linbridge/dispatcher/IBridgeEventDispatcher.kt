package de.suitepad.linbridge.dispatcher

import de.suitepad.linbridge.api.ILinbridgeListener

interface IBridgeEventDispatcher {

    var listener: ILinbridgeListener?

}