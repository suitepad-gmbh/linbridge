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
     * When [sips] is true, only queries _sips._tcp (TLS).
     * When [sips] is false (default), queries _sip._udp (UDP) and _sip._tcp (TCP).
     * Each result carries the transport it was discovered under.
     * Per-transport results are individually ordered per RFC 2782 (priority + weighted random)
     * and concatenated in transport order so that retry sequences stay within one transport
     * before falling through to the next.
     */
    suspend fun lookupSipSrvRecords(
        domain: String,
        sips: Boolean = false,
    ): List<SrvResult> =
        withContext(Dispatchers.IO) {
            val services =
                if (sips) {
                    listOf("_sips._tcp.$domain" to Transport.TLS)
                } else {
                    listOf(
                        "_sip._udp.$domain" to Transport.UDP,
                        "_sip._tcp.$domain" to Transport.TCP,
                    )
                }
            val results = mutableListOf<SrvResult>()
            for ((serviceDomain, transport) in services) {
                lookupSrvRecords(serviceDomain)
                    .map { it.copy(transport = transport) }
                    .also { results.addAll(it) }
            }
            results
        }

    /**
     * Orders SRV results per RFC 2782:
     * - Priority groups are ordered ascending (lowest value first).
     * - Within each priority group, records are selected via weighted random ordering.
     *   The random target W is chosen in [0, totalWeight] (inclusive). Weight-0 records
     *   are placed first in the pool so they have a small but non-zero chance of being
     *   selected (when W == 0), matching the RFC requirement:
     *   "records with weight 0 should have a very small chance of being selected."
     * - When all remaining weights are 0, a uniformly random element is picked.
     */
    internal fun weightedSrvOrder(records: List<SrvResult>): List<SrvResult> {
        val ordered = mutableListOf<SrvResult>()
        records
            .groupBy { it.priority }
            .entries
            .sortedBy { it.key }
            .forEach { (_, group) ->
                // Sort weight-0 records first per RFC 2782 so they can be selected when W == 0.
                val pool = group.sortedBy { it.weight }.toMutableList()
                while (pool.isNotEmpty()) {
                    val totalWeight = pool.sumOf { it.weight }
                    val selected: SrvResult
                    if (totalWeight == 0) {
                        // All weights are zero: pick a random element so equal-weight
                        // records don't always resolve in the same insertion order.
                        selected = pool.removeAt(Random.nextInt(pool.size))
                    } else {
                        // W in [0, totalWeight]: 0 allows weight-0 records to be picked.
                        var w = Random.nextInt(totalWeight + 1)
                        val iterator = pool.iterator()
                        var pick: SrvResult? = null
                        var runningSum = 0
                        while (iterator.hasNext()) {
                            val candidate = iterator.next()
                            runningSum += candidate.weight
                            if (runningSum >= w) {
                                pick = candidate
                                iterator.remove()
                                break
                            }
                        }
                        selected = pick ?: pool.removeFirst()
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
            val raw =
                records
                    ?.mapNotNull { it as? SRVRecord }
                    // RFC 2782: target "." means the service is decidedly not available.
                    ?.filter {
                        it.target
                            .toString()
                            .trimEnd('.')
                            .isNotEmpty()
                    }?.map { SrvResult(it.target.toString().trimEnd('.'), it.port, it.priority, it.weight) }
                    ?: emptyList()
            weightedSrvOrder(raw)
        } catch (e: Exception) {
            Timber.w(e, "SRV lookup failed for $serviceDomain" + (resolver?.let { " via $it" } ?: ""))
            emptyList()
        }
}