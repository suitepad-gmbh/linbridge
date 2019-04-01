package de.suitepad.linbridge.bridge

import android.os.IBinder
import de.suitepad.linbridge.api.ILinbridge

interface IBridgeService : ILinbridge {

    val binder: ILinBridgeBinder
    override fun asBinder(): IBinder = binder.asBinder()

    class ILinBridgeBinder(private val impl: ILinbridge) : ILinbridge.Stub(), ILinbridge by impl {
        override fun asBinder(): IBinder = super.asBinder()
    }

}
