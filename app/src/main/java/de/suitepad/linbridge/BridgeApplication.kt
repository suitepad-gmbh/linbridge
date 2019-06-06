package de.suitepad.linbridge

import android.app.Application
import android.app.Service
import android.util.Log
import com.crashlytics.android.Crashlytics
import de.suitepad.linbridge.dep.AppComponent
import de.suitepad.linbridge.dep.DaggerAppComponent
import de.suitepad.linbridge.logger.LogCatcher
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject

class BridgeApplication : Application() {

    companion object {

        @Throws(IllegalArgumentException::class)
        fun getApplication(service: Service): BridgeApplication {
            if (service.application is BridgeApplication) {
                return service.application as BridgeApplication
            } else {
                throw IllegalArgumentException("Application is not BridgeApplication")
            }
        }

    }

    lateinit var component: AppComponent

    @Inject
    lateinit var logCatcher: LogCatcher

    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics.getInstance())

        component = DaggerAppComponent.builder().build()
        component.inject(this)

        Timber.plant(Timber.DebugTree())
        Timber.plant(logCatcher)
        Timber.plant(CrashlyticsTimberTree())
    }


    private inner class CrashlyticsTimberTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (tag != null) {
                Crashlytics.log(priority, tag, message)
            } else {
                Crashlytics.log(message)
            }
            if (t != null) {
                Crashlytics.logException(t)
            }
        }
        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return priority >= Log.INFO
        }

    }

}