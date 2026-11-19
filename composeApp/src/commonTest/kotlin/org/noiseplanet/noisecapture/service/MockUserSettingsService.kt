package org.noiseplanet.noisecapture.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService

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
