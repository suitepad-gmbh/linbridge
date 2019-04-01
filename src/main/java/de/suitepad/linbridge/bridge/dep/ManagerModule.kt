package de.suitepad.linbridge.bridge.dep

import android.content.Context
import dagger.Module
import dagger.Provides
import de.suitepad.linbridge.bridge.manager.BridgeLinphoneCoreListener
import de.suitepad.linbridge.bridge.manager.IBridgeLinphoneCoreListener
import de.suitepad.linbridge.bridge.manager.IManager
import de.suitepad.linbridge.bridge.manager.LinphoneManager
import org.linphone.core.Core
import org.linphone.core.CoreListener
import org.linphone.core.Factory
import javax.inject.Named

@Module(includes = [BridgeModule::class])
class ManagerModule(val debug: Boolean) {

    @Provides
    fun linphoneManager(context: Context, core: Core, coreFactory: Factory): IManager {
        return LinphoneManager(context, core, coreFactory)
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
    @DebugFlag
    fun provideIsDebug(): Boolean = debug

    @Named("debug")
    annotation class DebugFlag

}