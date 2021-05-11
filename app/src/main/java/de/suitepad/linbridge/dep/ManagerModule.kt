package de.suitepad.linbridge.dep

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import de.suitepad.linbridge.dispatcher.BridgeEventDispatcher
import de.suitepad.linbridge.dispatcher.IBridgeEventDispatcher
import de.suitepad.linbridge.manager.IManager
import de.suitepad.linbridge.logger.LinbridgeEventLogger
import de.suitepad.linbridge.logger.LogCatcher
import de.suitepad.linbridge.manager.LinbridgeManager
import org.linphone.core.BuildConfig
import org.linphone.core.Core
import org.linphone.core.Factory
import org.linphone.core.LoggingServiceListener
import javax.inject.Named

@Module
@InstallIn(ServiceComponent::class)
class ManagerModule {

    @Provides
    @ServiceScoped
    fun provideCore(
            linphoneCoreListener: BridgeEventDispatcher,
            factory: Factory,
            @ApplicationContext context: Context,
    ): Core {
        return factory.createCore("${context.filesDir.absolutePath}/linphonerc", "${context.filesDir.absolutePath}/linphonerc", context).apply {
            addListener(linphoneCoreListener)
        }
    }

    @Provides
    @ServiceScoped
    fun provideCoreFactory(loggingServiceListener: LogCatcher): Factory {
        return Factory.instance().apply {
            setDebugMode(true, "LibLinphone")
            this.loggingService.setListener(loggingServiceListener)
        }
    }

    @Named("debug")
    annotation class DebugFlag

}