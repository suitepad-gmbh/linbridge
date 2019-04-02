package de.suitepad.linbridge.bridge.dep

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import de.suitepad.linbridge.bridge.manager.*
import org.linphone.core.Core
import org.linphone.core.CoreListener
import org.linphone.core.Factory
import javax.inject.Named

@Module(includes = [BridgeModule::class])
class ManagerModule(val debug: Boolean) {

    @Provides
    fun linphoneManager(context: Context, core: Core, coreFactory: Factory, @EventLogger logger: CoreListener): IManager {
        val linphoneManager = LinphoneManager(context, core, coreFactory)
        linphoneManager.core.addListener(linphoneManager)
        linphoneManager.core.addListener(logger)
        return linphoneManager
    }

    @Provides
    fun linphoneCore(
            linphoneCoreListener: CoreListener,
            factory: Factory,
            context: Context
    ): Core {
        return factory.createCore("${context.filesDir.absolutePath}/linphonerc", "${context.filesDir.absolutePath}/linphonerc", context).apply {
            addListener(linphoneCoreListener)
        }
    }

    @Provides
    fun bridgeLinphoneCoreListener(coreListener: CoreListener): IBridgeLinphoneCoreListener {
        return coreListener as IBridgeLinphoneCoreListener
    }

    @Provides
    fun linphoneCoreListener(): CoreListener {
        return BridgeLinphoneCoreListener()
    }

    @Provides
    fun linphoneCoreFactory(@DebugFlag debug: Boolean): Factory {
        return Factory.instance().apply {
            setDebugMode(debug, "LibLinphone")
        }
    }

    @Provides
    @EventLogger
    fun linphoneEventLogger(@DebugFlag debug: Boolean): CoreListener {
        return LinbridgeEventLogger(if (debug) Log.INFO else Log.DEBUG)
    }

    @Provides
    @DebugFlag
    fun provideIsDebug(): Boolean = debug

    @Named("eventLogger")
    annotation class EventLogger

    @Named("debug")
    annotation class DebugFlag

}