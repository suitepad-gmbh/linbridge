package de.suitepad.linbridge.bridge.manager

import junit.framework.Assert.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

class LinphoneManagerTest: Spek({

    println("this is the root")
    Feature("authentication") {

        val set by memoized { mutableSetOf<String>() }

        Scenario("adding items") {
            When("adding foo") {
                set.add("foo")
            }
            Then("it should have size of 1") {
                assertEquals(1, set.size)
            }
            Then("it should contain foo") {
                assertTrue(set.contains("foo"))
            }
        }

    }

})