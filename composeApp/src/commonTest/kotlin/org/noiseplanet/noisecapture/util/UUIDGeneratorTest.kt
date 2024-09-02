package org.noiseplanet.noisecapture.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class UUIDGeneratorTest {

    @Test
    fun testCheckVersionAndVariant() {
        val random = Random(123456)
        for(i in 0..10) {
            val generated = UUIDGenerator.createV4UUID(random)
            assertEquals("4", generated.subSequence(14, 15))
            assertContains("89ab",generated.subSequence(19, 20))
        }
    }
}