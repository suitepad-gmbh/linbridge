package de.suitepad.linbridge.dep

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped
import de.suitepad.linbridge.dispatcher.BridgeEventDispatcher
import de.suitepad.linbridge.dispatcher.IBridgeEventDispatcher
import de.suitepad.linbridge.manager.IManager
import de.suitepad.linbridge.manager.LinbridgeManager

@Module
@InstallIn(ServiceComponent::class)
interface ManagerBinder {

    @Binds
    fun manager(manager: LinbridgeManager): IManager

    @Binds
    @ServiceScoped
    fun provideEventDispatcher(coreListener: BridgeEventDispatcher): IBridgeEventDispatcher

}