package de.suitepad.linbridge.manager

import de.suitepad.linbridge.manager.DnsSrvLookupManager.SrvResult
import de.suitepad.linbridge.manager.DnsSrvLookupManager.Transport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DnsSrvLookupManagerTest {

    // --- Priority ordering ---

    @Test
    fun `weightedSrvOrder sorts by priority ascending`() {
        val records = listOf(
            SrvResult("c.example.com", 5060, priority = 30, weight = 0),
            SrvResult("a.example.com", 5060, priority = 10, weight = 0),
            SrvResult("b.example.com", 5060, priority = 20, weight = 0),
        )
        val ordered = DnsSrvLookupManager.weightedSrvOrder(records)
        assertEquals(10, ordered[0].priority)
        assertEquals(20, ordered[1].priority)
        assertEquals(30, ordered[2].priority)
    }

    @Test
    fun `weightedSrvOrder returns all records`() {
        val records = listOf(
            SrvResult("a.example.com", 5060, priority = 10, weight = 50),
            SrvResult("b.example.com", 5060, priority = 10, weight = 30),
            SrvResult("c.example.com", 5060, priority = 20, weight = 10),
        )
        val ordered = DnsSrvLookupManager.weightedSrvOrder(records)
        assertEquals(records.size, ordered.size)
        assertTrue(ordered.containsAll(records))
    }

    @Test
    fun `weightedSrvOrder with empty list returns empty`() {
        assertEquals(emptyList<SrvResult>(), DnsSrvLookupManager.weightedSrvOrder(emptyList()))
    }

    @Test
    fun `weightedSrvOrder with single record returns it`() {
        val record = SrvResult("a.example.com", 5060, priority = 10, weight = 100)
        val ordered = DnsSrvLookupManager.weightedSrvOrder(listOf(record))
        assertEquals(listOf(record), ordered)
    }

    // --- Weighted selection within priority group ---

    @Test
    fun `weightedSrvOrder higher weight records are selected more often`() {
        val heavy = SrvResult("heavy.example.com", 5060, priority = 10, weight = 100)
        val light = SrvResult("light.example.com", 5060, priority = 10, weight = 1)
        val records = listOf(heavy, light)

        var heavyFirst = 0
        val iterations = 1000
        repeat(iterations) {
            val ordered = DnsSrvLookupManager.weightedSrvOrder(records)
            if (ordered[0] == heavy) heavyFirst++
        }
        // With weights 100:1, heavy should be first ~99% of the time.
        // Use a generous threshold to avoid flaky tests.
        assertTrue(
            "Expected heavy to be first most of the time, but was first $heavyFirst/$iterations times",
            heavyFirst > iterations * 0.85,
        )
    }

    // --- Weight-0 RFC 2782 compliance ---

    @Test
    fun `weightedSrvOrder all-zero weights distributes selections randomly`() {
        val records = listOf(
            SrvResult("a.example.com", 5060, priority = 10, weight = 0),
            SrvResult("b.example.com", 5060, priority = 10, weight = 0),
            SrvResult("c.example.com", 5060, priority = 10, weight = 0),
        )
        val firstCounts = mutableMapOf<String, Int>()
        val iterations = 1000
        repeat(iterations) {
            val ordered = DnsSrvLookupManager.weightedSrvOrder(records)
            val first = ordered[0].target
            firstCounts[first] = (firstCounts[first] ?: 0) + 1
        }
        // All three should appear as first at least sometimes (uniform random).
        for (record in records) {
            val count = firstCounts[record.target] ?: 0
            assertTrue(
                "Expected ${record.target} to be first at least once in $iterations iterations, got $count",
                count > 0,
            )
        }
    }

    @Test
    fun `weightedSrvOrder weight-0 records have small but nonzero chance in mixed pool`() {
        // RFC 2782: "records with weight 0 should have a very small chance of being selected"
        val zeroWeight = SrvResult("zero.example.com", 5060, priority = 10, weight = 0)
        val nonZero = SrvResult("heavy.example.com", 5060, priority = 10, weight = 100)
        val records = listOf(zeroWeight, nonZero)

        var zeroFirst = 0
        val iterations = 10000
        repeat(iterations) {
            val ordered = DnsSrvLookupManager.weightedSrvOrder(records)
            if (ordered[0] == zeroWeight) zeroFirst++
        }
        // Weight-0 should be picked first ~1/101 of the time ≈ ~1%.
        // Verify it's picked at least once (non-zero chance) but less than 5% (small chance).
        assertTrue(
            "Expected weight-0 record to be first at least once in $iterations iterations, got $zeroFirst",
            zeroFirst > 0,
        )
        assertTrue(
            "Expected weight-0 record to be first rarely, but was first $zeroFirst/$iterations times",
            zeroFirst < iterations * 0.05,
        )
    }

    // --- Multiple priority groups ---

    @Test
    fun `weightedSrvOrder preserves priority ordering across groups`() {
        val records = listOf(
            SrvResult("low1.example.com", 5060, priority = 10, weight = 50),
            SrvResult("low2.example.com", 5060, priority = 10, weight = 50),
            SrvResult("high1.example.com", 5060, priority = 20, weight = 50),
            SrvResult("high2.example.com", 5060, priority = 20, weight = 50),
        )
        repeat(100) {
            val ordered = DnsSrvLookupManager.weightedSrvOrder(records)
            // First two must be from priority 10, last two from priority 20.
            assertEquals(10, ordered[0].priority)
            assertEquals(10, ordered[1].priority)
            assertEquals(20, ordered[2].priority)
            assertEquals(20, ordered[3].priority)
        }
    }

    // --- Transport field is preserved ---

    @Test
    fun `weightedSrvOrder preserves transport field`() {
        val records = listOf(
            SrvResult("a.example.com", 5060, priority = 10, weight = 10, transport = Transport.TLS),
            SrvResult("b.example.com", 5060, priority = 10, weight = 10, transport = Transport.UDP),
        )
        val ordered = DnsSrvLookupManager.weightedSrvOrder(records)
        val transports = ordered.map { it.transport }.toSet()
        assertEquals(setOf(Transport.TLS, Transport.UDP), transports)
    }
}
