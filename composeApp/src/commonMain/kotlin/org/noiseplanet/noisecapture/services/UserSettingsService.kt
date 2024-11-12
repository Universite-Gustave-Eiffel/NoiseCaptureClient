package org.noiseplanet.noisecapture.services

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
 * Read and write persistent user settings as key value pairs.
 */
interface UserSettingsService {

    /**
     * Sets the value associated to the given key.
     *
     * @param key User settings [SettingsKey]
     * @param value New value
     */
    fun <T> set(key: SettingsKey<T>, value: T?)

    /**
     * Gets the value associated to a given key, or [SettingsKey.defaultValue] if value is not set.
     *
     * @param key User settings [SettingsKey]
     * @return Value or [SettingsKey.defaultValue] if not found
     */
    fun <T> get(key: SettingsKey<T>): T

    /**
     * Gets a flow of values associated to a given key, starting with [SettingsKey.defaultValue]
     * if value is not set.
     *
     * @param key User settings [SettingsKey]
     * @return Value or [SettingsKey.defaultValue] if not found
     */
    fun <T> getFlow(key: SettingsKey<T>): Flow<T>
}


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
