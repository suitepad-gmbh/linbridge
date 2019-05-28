package de.suitepad.linbridge

import android.app.Application
import android.app.Service
import de.suitepad.linbridge.dep.AppComponent
import de.suitepad.linbridge.dep.DaggerAppComponent
import de.suitepad.linbridge.logger.LogCatcher
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

        component = DaggerAppComponent.builder().build()
        component.inject(this)

        Timber.plant(Timber.DebugTree())
        Timber.plant(logCatcher)
    }

}