package de.suitepad.linbridge.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object LinphoneScope : CoroutineScope {
    private val dispatcher = Executors.newSingleThreadExecutor { r -> Thread(r, "LinphoneThread") }.asCoroutineDispatcher()
    private val job = SupervisorJob()
    override val coroutineContext = dispatcher + job

    fun shutdown() {
        job.cancel()
        (dispatcher as ExecutorCoroutineDispatcher).close()
    }
}