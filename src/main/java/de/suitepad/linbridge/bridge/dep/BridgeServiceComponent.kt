package de.suitepad.linbridge.bridge.dep

import android.content.Context
import dagger.Component
import de.suitepad.linbridge.app.dep.AppComponent
import de.suitepad.linbridge.bridge.BridgeService
import de.suitepad.linbridge.bridge.manager.IBridgeLinphoneCoreListener
import de.suitepad.linbridge.bridge.manager.IManager
import de.suitepad.linbridge.bridge.manager.LinphoneManager
import javax.inject.Scope

@Component(dependencies = [AppComponent::class], modules = [BridgeModule::class, ManagerModule::class])
@BridgeServiceComponent.LinphoneBridgeServiceScope
interface BridgeServiceComponent {

    fun provideLinphoneManager(): IManager

    fun provideLinphoneListener(): IBridgeLinphoneCoreListener

    fun provideServiceContext(): Context

    fun inject(service: BridgeService)

    @Scope
    annotation class LinphoneBridgeServiceScope

}