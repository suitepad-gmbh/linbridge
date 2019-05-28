package de.suitepad.linbridge.dep

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import de.suitepad.linbridge.dispatcher.BridgeEventDispatcher
import de.suitepad.linbridge.dispatcher.IBridgeEventDispatcher
import de.suitepad.linbridge.manager.IManager
import de.suitepad.linbridge.logger.LinbridgeEventLogger
import de.suitepad.linbridge.logger.LogCatcher
import de.suitepad.linbridge.manager.LinbridgeManager
import org.linphone.core.Core
import org.linphone.core.Factory
import org.linphone.core.LoggingServiceListener
import javax.inject.Named

@Module(includes = [BridgeModule::class])
class ManagerModule(val debug: Boolean) {

    @Provides
    @BridgeServiceComponent.BridgeServiceScope
    fun provideManager(context: Context, core: Core, logger: LinbridgeEventLogger): IManager {
        val linphoneManager = LinbridgeManager(context, core)
        linphoneManager.core.addListener(linphoneManager)
        linphoneManager.core.addListener(logger)
        return linphoneManager
    }

    @Provides
    @BridgeServiceComponent.BridgeServiceScope
    fun provideCore(
            linphoneCoreListener: BridgeEventDispatcher,
            factory: Factory,
            context: Context
    ): Core {
        return factory.createCore("${context.filesDir.absolutePath}/linphonerc", "${context.filesDir.absolutePath}/linphonerc", context).apply {
            addListener(linphoneCoreListener)
        }
    }

    @Provides
    @BridgeServiceComponent.BridgeServiceScope
    fun provideEventDispatcher(coreListener: BridgeEventDispatcher): IBridgeEventDispatcher = coreListener

    @Provides
    @BridgeServiceComponent.BridgeServiceScope
    fun provideEventDispatcherImpl(): BridgeEventDispatcher {
        return BridgeEventDispatcher()
    }

    @Provides
    @BridgeServiceComponent.BridgeServiceScope
    fun provideCoreFactory(@DebugFlag debug: Boolean, loggingServiceListener: LogCatcher): Factory {
        return Factory.instance().apply {
            setDebugMode(debug, "LibLinphone")
            this.loggingService.setListener(loggingServiceListener)
        }
    }

    @Provides
    @BridgeServiceComponent.BridgeServiceScope
    fun provideEventLogger(@DebugFlag debug: Boolean): LinbridgeEventLogger {
        return LinbridgeEventLogger(if (debug) Log.INFO else Log.DEBUG)
    }

    @Provides
    @DebugFlag
    @BridgeServiceComponent.BridgeServiceScope
    fun provideIsDebug(): Boolean = debug

    @Named("debug")
    annotation class DebugFlag

}