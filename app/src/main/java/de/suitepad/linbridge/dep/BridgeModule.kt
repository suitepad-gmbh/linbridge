package de.suitepad.linbridge.dep

import android.content.Context
import dagger.Module
import dagger.Provides
import de.suitepad.linbridge.BridgeService

@Module
class BridgeModule(val context: BridgeService) {

    @Provides
    @BridgeServiceComponent.BridgeServiceScope
    fun context(): Context = context

}