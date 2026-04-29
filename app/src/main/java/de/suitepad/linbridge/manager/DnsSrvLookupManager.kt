package de.suitepad.linbridge.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.SimpleResolver
import org.xbill.DNS.Type
import timber.log.Timber
import kotlin.random.Random

private const val FALLBACK_DNS_SERVER = "8.8.8.8"

object DnsSrvLookupManager {

    enum class Transport { UDP, TCP, TLS }

    data class SrvResult(
        val target: String,
        val port: Int,
        val priority: Int,
        val weight: Int,
        val transport: Transport = Transport.UDP,
    )

    /**
     * Performs SRV lookup using system DNS first, then falls back to Google DNS (8.8.8.8).
     * Returns results ordered per RFC 2782: priority groups ascending, within each group
     * records are selected by weighted random ordering (higher weight = higher probability).
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
     * Queries: _sip._udp (UDP), _sip._tcp (TCP), _sips._tcp (TLS)
     * Each result carries the transport it was discovered under.
     * Results are ordered per RFC 2782: priority groups ascending, within each group
     * records are selected by weighted random ordering.
     */
    suspend fun lookupSipSrvRecords(domain: String): List<SrvResult> =
        withContext(Dispatchers.IO) {
            val services = listOf(
                "_sip._udp.$domain" to Transport.UDP,
                "_sip._tcp.$domain" to Transport.TCP,
                "_sips._tcp.$domain" to Transport.TLS,
            )
            val results = mutableListOf<SrvResult>()
            for ((serviceDomain, transport) in services) {
                lookupSrvRecords(serviceDomain)
                    .map { it.copy(transport = transport) }
                    .also { results.addAll(it) }
            }
            weightedSrvOrder(results)
        }

    /**
     * Orders SRV results per RFC 2782:
     * - Priority groups are ordered ascending (lowest value first).
     * - Within each priority group, records are selected via weighted random ordering:
     *   each record's probability of being chosen next is proportional to its weight.
     *   Records with weight 0 are still eligible but only selected when the running sum
     *   target is 0 (i.e. all remaining weights are also 0).
     */
    internal fun weightedSrvOrder(records: List<SrvResult>): List<SrvResult> {
        val ordered = mutableListOf<SrvResult>()
        records.groupBy { it.priority }
            .entries
            .sortedBy { it.key }
            .forEach { (_, group) ->
                val pool = group.toMutableList()
                while (pool.isNotEmpty()) {
                    val totalWeight = pool.sumOf { it.weight }
                    var target = if (totalWeight > 0) Random.nextInt(totalWeight + 1) else 0
                    val iterator = pool.iterator()
                    var selected: SrvResult? = null
                    while (iterator.hasNext()) {
                        val candidate = iterator.next()
                        target -= candidate.weight
                        if (target <= 0) {
                            selected = candidate
                            iterator.remove()
                            break
                        }
                    }
                    // Fallback: if no record was selected (all weights 0), take the first
                    if (selected == null) {
                        selected = pool.removeFirst()
                    }
                    ordered.add(selected)
                }
            }
        return ordered
    }

    private fun performLookup(
        serviceDomain: String,
        resolver: String?,
    ): List<SrvResult> =
        try {
            val lookup = Lookup(serviceDomain, Type.SRV)
            if (resolver != null) {
                lookup.setResolver(SimpleResolver(resolver))
            }
            val records = lookup.run()
            val raw = records
                ?.mapNotNull { it as? SRVRecord }
                ?.map { SrvResult(it.target.toString().trimEnd('.'), it.port, it.priority, it.weight) }
                ?: emptyList()
            weightedSrvOrder(raw)
        } catch (e: Exception) {
            Timber.w(e, "SRV lookup failed for $serviceDomain" + (resolver?.let { " via $it" } ?: ""))
            emptyList()
        }
}