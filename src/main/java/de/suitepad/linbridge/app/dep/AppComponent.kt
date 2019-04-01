package de.suitepad.linbridge.app.dep

import dagger.Component
import de.suitepad.linbridge.app.BridgeApplication
import javax.inject.Scope

@Component
@AppComponent.AppScope
interface AppComponent {

    fun inject(app: BridgeApplication)

    @Scope
    annotation class AppScope

}