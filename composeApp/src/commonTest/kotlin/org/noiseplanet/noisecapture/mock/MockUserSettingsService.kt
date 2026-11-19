package org.noiseplanet.noisecapture.mock

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("UNCHECKED_CAST")
class MockUserSettingsService : UserSettingsService {

    // - Properties

    private val values: MutableMap<SettingsKey<*>, Any?> = mutableMapOf()
    private val updatesFlow = MutableSharedFlow<Unit>(replay = 1)


    // - Lifecycle

    init {
        // Upon initialisation, emit a first value so the flow not empty when subscribing
        updatesFlow.tryEmit(Unit)
    }


    // - Public functions

    override fun <T> set(
        key: SettingsKey<T>,
        value: T?,
    ) {
        value?.let {
            values[key] = value
        } ?: run {
            values.remove(key)
        }
        updatesFlow.tryEmit(Unit)
    }

    override fun <T> get(key: SettingsKey<T>): T {
        return (values[key] as? T) ?: key.defaultValue
    }

    override fun <T> getFlow(key: SettingsKey<T>): Flow<T> {
        return updatesFlow.map { get(key) }
    }
}


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
