package de.suitepad.linbridge.dep

import dagger.Component
import dagger.Module
import dagger.Provides
import de.suitepad.linbridge.BridgeApplication
import de.suitepad.linbridge.logger.LogCatcher
import org.linphone.core.LoggingServiceListener
import javax.inject.Scope

@Component(modules = [AppModule::class])
@AppComponent.AppScope
interface AppComponent {

    fun logCatcher(): LogCatcher

    fun inject(app: BridgeApplication)

    @Scope
    annotation class AppScope

}

@Module
class AppModule {

    @Provides
    @AppComponent.AppScope
    fun provideLoggingServiceListener(): LogCatcher {
        return LogCatcher()
    }

}