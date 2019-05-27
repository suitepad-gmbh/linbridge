package de.suitepad.linbridge.dep

import dagger.Component
import de.suitepad.linbridge.BridgeApplication
import javax.inject.Scope

@Component
@AppComponent.AppScope
interface AppComponent {

    fun inject(app: BridgeApplication)

    @Scope
    annotation class AppScope

}