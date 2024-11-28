package org.noiseplanet.noisecapture.services.settings

import kotlinx.coroutines.flow.Flow


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
