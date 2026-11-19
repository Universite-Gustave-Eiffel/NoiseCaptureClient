package org.noiseplanet.noisecapture.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MockUserSettingsServiceTest {

    @Test
    fun testDefaultValue() = runTest {
        val service = MockUserSettingsService()

        assertEquals(
            SettingsKey.IntTest.defaultValue,
            service.get(SettingsKey.IntTest),
        )
        assertEquals(
            SettingsKey.NullableStringTest.defaultValue,
            service.get(SettingsKey.NullableStringTest),
        )
    }

    @Test
    fun testSetValue() = runTest {
        val service = MockUserSettingsService()

        service.set(SettingsKey.IntTest, 1234)
        assertEquals(1234, service.get(SettingsKey.IntTest))

        service.set(SettingsKey.NullableStringTest, "1234")
        assertEquals("1234", service.get(SettingsKey.NullableStringTest))

        service.set(SettingsKey.NullableStringTest, null)
        assertNull(service.get(SettingsKey.NullableStringTest))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testFlow() = runTest {
        val service = MockUserSettingsService()
        val values = mutableListOf<String?>()

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            service.getFlow(SettingsKey.NullableStringTest).toList(values)
        }

        assertEquals(1, values.size)
        assertEquals(null, values[0])

        service.set(SettingsKey.NullableStringTest, "1234")
        assertEquals(2, values.size)
        assertEquals("1234", values[1])

        service.set(SettingsKey.NullableStringTest, null)
        assertEquals(3, values.size)
        assertEquals(null, values[2])
    }
}
