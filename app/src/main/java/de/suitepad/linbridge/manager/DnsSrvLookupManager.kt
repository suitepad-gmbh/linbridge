package de.suitepad.linbridge.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.SimpleResolver
import org.xbill.DNS.Type
import timber.log.Timber

private const val FALLBACK_DNS_SERVER = "8.8.8.8"

object DnsSrvLookupManager {

    data class SrvResult(
        val target: String,
        val port: Int,
        val priority: Int,
        val weight: Int
    )

    /**
     * Performs SRV lookup using system DNS first, then falls back to Google DNS (8.8.8.8).
     * Returns results sorted by priority (ascending), then weight (descending) within same priority.
     */
    suspend fun lookupSrvRecords(serviceDomain: String): List<SrvResult> =
        withContext(Dispatchers.IO) {
            val systemResult = performLookup(serviceDomain, resolver = null)
            if (systemResult.isNotEmpty()) {
                Timber.i("SRV lookup for $serviceDomain resolved via system DNS: ${systemResult.size} records")
                return@withContext systemResult
            }

            Timber.i("System DNS returned no SRV records for $serviceDomain, trying fallback DNS ($FALLBACK_DNS_SERVER)")
            val fallbackResult = performLookup(serviceDomain, resolver = FALLBACK_DNS_SERVER)
            if (fallbackResult.isNotEmpty()) {
                Timber.i("SRV lookup for $serviceDomain resolved via fallback DNS: ${fallbackResult.size} records")
            } else {
                Timber.i("No SRV records found for $serviceDomain via any resolver")
            }
            fallbackResult
        }

    /**
     * Queries SRV records for standard SIP service types per RFC 3263.
     * Queries: _sip._udp, _sip._tcp, _sips._tcp
     * Returns all results merged, sorted by priority then weight.
     */
    suspend fun lookupSipSrvRecords(domain: String): List<SrvResult> =
        withContext(Dispatchers.IO) {
            val services = listOf("_sip._udp", "_sip._tcp", "_sips._tcp")
            val results = mutableListOf<SrvResult>()
            for (service in services) {
                results.addAll(lookupSrvRecords("$service.$domain"))
            }
            results.sortedWith(compareBy<SrvResult> { it.priority }.thenByDescending { it.weight })
        }

    private fun performLookup(serviceDomain: String, resolver: String?): List<SrvResult> {
        return try {
            val lookup = Lookup(serviceDomain, Type.SRV)
            if (resolver != null) {
                lookup.setResolver(SimpleResolver(resolver))
            }
            val records = lookup.run()
            records?.mapNotNull { it as? SRVRecord }
                ?.map { SrvResult(it.target.toString().trimEnd('.'), it.port, it.priority, it.weight) }
                ?.sortedWith(compareBy<SrvResult> { it.priority }.thenByDescending { it.weight })
                ?: emptyList()
        } catch (e: Exception) {
            Timber.w(e, "SRV lookup failed for $serviceDomain" + (resolver?.let { " via $it" } ?: ""))
            emptyList()
        }
    }
}