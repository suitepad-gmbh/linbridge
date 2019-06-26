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
import org.linphone.core.LinphoneCore
import org.linphone.core.LinphoneCoreFactory
import javax.inject.Named

@Module(includes = [BridgeModule::class])
class ManagerModule(val debug: Boolean) {

    @Provides
    @BridgeServiceComponent.BridgeServiceScope
    fun provideManager(context: Context, core: LinphoneCore, logger: LinbridgeEventLogger): IManager {
        val linphoneManager = LinbridgeManager(context, core)
        linphoneManager.core.addListener(linphoneManager)
        linphoneManager.core.addListener(logger)
        return linphoneManager
    }

    @Provides
    @BridgeServiceComponent.BridgeServiceScope
    fun provideCore(
            linphoneCoreListener: BridgeEventDispatcher,
            factory: LinphoneCoreFactory,
            context: Context
    ): LinphoneCore {
        return factory.createLinphoneCore(linphoneCoreListener, "${context.filesDir.absolutePath}/linphonerc", "${context.filesDir.absolutePath}/linphonerc", null, context)
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
    fun provideCoreFactory(@DebugFlag debug: Boolean, loggingServiceListener: LogCatcher):LinphoneCoreFactory {
        return LinphoneCoreFactory.instance().apply {
            setDebugMode(debug, "LibLinphone")
            this.setLogHandler(loggingServiceListener)
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