package de.suitepad.linbridge.dep

import android.content.Context
import dagger.Component
import de.suitepad.linbridge.BridgeService
import de.suitepad.linbridge.dispatcher.IBridgeEventDispatcher
import de.suitepad.linbridge.manager.IManager
import javax.inject.Scope

@Component(dependencies = [AppComponent::class], modules = [BridgeModule::class, ManagerModule::class])
@BridgeServiceComponent.BridgeServiceScope
interface BridgeServiceComponent {

    fun provideLinphoneManager(): IManager

    fun provideLinphoneListener(): IBridgeEventDispatcher

    fun provideServiceContext(): Context

    fun inject(service: BridgeService)

    @Scope
    annotation class BridgeServiceScope

}