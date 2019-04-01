package de.suitepad.linbridge.bridge.dep

import android.content.Context
import dagger.Module
import dagger.Provides
import de.suitepad.linbridge.bridge.BridgeService

@Module
class BridgeModule(val context: BridgeService) {

    @Provides
    fun context(): Context = context

}