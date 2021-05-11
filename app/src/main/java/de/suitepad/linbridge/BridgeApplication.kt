package de.suitepad.linbridge

import android.app.Application
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import de.suitepad.linbridge.logger.LogCatcher
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BridgeApplication : Application() {


    @Inject lateinit var logCatcher: LogCatcher

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        Timber.plant(logCatcher)
        Timber.plant(CrashlyticsTimberTree())
    }


    private inner class CrashlyticsTimberTree : Timber.Tree() {

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            FirebaseCrashlytics.getInstance().log(message)
            if (t != null) {
                FirebaseCrashlytics.getInstance().recordException(t)
            }
        }

        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return priority >= Log.INFO
        }

    }

}