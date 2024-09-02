package org.noiseplanet.noisecapture.util

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class UUIDGeneratorTest {

    @Test
    fun testCheckVersionAndVariant() {
        for(i in 0..10) {
            val generated = UUIDGenerator.createV4UUID()
            assertEquals("4", generated.subSequence(14, 15))
            assertContains("89ab",generated.subSequence(19, 20))
        }
    }
}