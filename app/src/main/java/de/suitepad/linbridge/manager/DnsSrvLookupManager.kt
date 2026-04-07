package de.suitepad.linbridge.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.SimpleResolver
import org.xbill.DNS.Type
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val GOOGLE_DNS_SERVER = "8.8.8.8"
object DnsSrvLookupManager {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private fun lookupSrvRecords(serviceDomain: String, onResult: (List<SRVRecord>) -> Unit, onError: (Throwable) -> Unit ) {
        scope.launch {
            try {
                val resolver = SimpleResolver(GOOGLE_DNS_SERVER)
                val lookup = Lookup(serviceDomain, Type.SRV)
                lookup.setResolver(resolver)
                val records = lookup.run()
                val srvRecords = (records?.mapNotNull { it as? SRVRecord } ?: emptyList()).sortedBy { it.priority }
                if (srvRecords.isEmpty()) {
                    Timber.i("No SRV records found for $serviceDomain")
                }
                withContext(Dispatchers.Main) {
                    onResult(srvRecords)
                }
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }

    suspend fun lookupSrvRecordsSuspend(serviceDomain: String): List<SRVRecord> =
        suspendCancellableCoroutine { cont ->
            this.lookupSrvRecords(
                serviceDomain = serviceDomain,
                onResult = { cont.resume(it) },
                onError = { cont.resumeWithException(it) }
            )
        }
    fun clear() {
        job.cancel()
    }
}