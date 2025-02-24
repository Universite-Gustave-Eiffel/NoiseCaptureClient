package org.noiseplanet.noisecapture.services.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import com.russhwolf.settings.serialization.removeValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi


/**
 * Default [UserSettingsService] implementation relying on a platform specific settings provider
 *
 * @param settingsProvider Platform specific settings provider
 */
@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class DefaultUserSettingsService(
    private val settingsProvider: Settings,
) : UserSettingsService {

    /**
     * Tracks changes made to settings value.
     */
    private val settingsChangeListener = MutableSharedFlow<Unit>(replay = 1)

    init {
        // Upon initialisation, emit a first value so the flow not empty when subscribing
        settingsChangeListener.tryEmit(Unit)
    }

    override fun <T> set(key: SettingsKey<T>, value: T?) {
        val keyName = requireNotNull(key::class.simpleName) {
            "Could not get name from settings key"
        }

        value?.let {
            settingsProvider.encodeValue(key.serializer, keyName, it)
        } ?: {
            settingsProvider.removeValue(key.serializer, keyName)
        }
        // Notify that a new value was stored
        settingsChangeListener.tryEmit(Unit)
    }

    override fun <T> get(key: SettingsKey<T>): T {
        val keyName = requireNotNull(key::class.simpleName) {
            "Could not get name from settings key"
        }

        return settingsProvider.decodeValueOrNull(key.serializer, keyName) ?: key.defaultValue
    }

    override fun <T> getFlow(key: SettingsKey<T>): Flow<T> {
        return settingsChangeListener
            .map { get(key) ?: key.defaultValue }
            .distinctUntilChanged()
    }
}
