package org.noiseplanet.noisecapture.services

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlin.reflect.KClass


/**
 * User settings keys
 */
enum class SettingsKey(defaultValue: Any? = null) {

    // For testing purpose
    TEST,

    // Profile
    USER_ACOUSTICS_KNOWLEDGE,

    // General
    TOOLTIPS_ENABLED,
    SHOW_APP_DISCLAIMER,
    SHOW_NOISE_PLANET_NOTIFICATION,
    AUTOMATIC_DATA_TRANSFER_ENABLED,
    DATA_TRANSFER_ON_WIFI_ONLY,

    // Measurements
    WINDOWING_MODE,
    RESTRICT_MEASUREMENT_DURATION,
    MEASUREMENT_MAX_DURATION,
    SPECTROGRAM_SCALE_MODE,
    PAUSE_MODE, // TODO: Better name for this

    // Calibration
    GAIN_CORRECTION,
    CALIBRATION_COUNTDOWN_DURATION,
    CALIBRATION_DURATION,
    CALIBRATION_SIGNAL_OUTPUT,

    // Map
    MAX_MEASUREMENTS_SHOWN_ON_MAP,
}


/**
 * Read and write persistent user settings as key value pairs.
 */
interface UserSettingsService {

    /**
     * Sets the value associated to the given key.
     *
     * @param key User settings [SettingsKey]
     * @param value New value
     * @param t Type of value to set
     */
    fun <T : Any> set(key: SettingsKey, value: T?, t: KClass<T>)

    /**
     * Gets the value associated to a given key, or null if value is not set.
     *
     * @param key User settings [SettingsKey]
     * @param t Type of value to get
     * @return Value or null if not found.
     */
    fun <T : Any> get(key: SettingsKey, t: KClass<T>): T?

    /**
     * Gets the value associated to a given key, or null if value is not set.
     *
     * @param key User settings [SettingsKey]
     * @param defaultValue Default value to return if no value was found
     * @param t Type of value to get
     * @return Value or [defaultValue] if not found
     */
    fun <T : Any> get(key: SettingsKey, defaultValue: T, t: KClass<T>): T
}


/**
 * Default [UserSettingsService] implementation relying on a platform specific settings provider
 *
 * @param settingsProvider Platform specific settings provider
 */
class DefaultUserSettingsService(
    private val settingsProvider: Settings,
) : UserSettingsService {

    override fun <T : Any> set(key: SettingsKey, value: T?, t: KClass<T>) {
        when (t) {
            Int::class -> settingsProvider[key.name] = value as Int
            Long::class -> settingsProvider[key.name] = value as Long
            Double::class -> settingsProvider[key.name] = value as Double
            Float::class -> settingsProvider[key.name] = value as Float
            String::class -> settingsProvider[key.name] = value as String
            Boolean::class -> settingsProvider[key.name] = value as Boolean
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: SettingsKey, t: KClass<T>): T? {
        return when (t) {
            Int::class -> settingsProvider.getIntOrNull(key.name) as T?
            Long::class -> settingsProvider.getLongOrNull(key.name) as T?
            Double::class -> settingsProvider.getDoubleOrNull(key.name) as T?
            Float::class -> settingsProvider.getFloatOrNull(key.name) as T?
            String::class -> settingsProvider.getStringOrNull(key.name) as T?
            Boolean::class -> settingsProvider.getBooleanOrNull(key.name) as T?
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: SettingsKey, defaultValue: T, t: KClass<T>): T {
        return when (t) {
            Int::class -> settingsProvider[key.name, defaultValue as Int] as T
            Long::class -> settingsProvider[key.name, defaultValue as Long] as T
            Double::class -> settingsProvider[key.name, defaultValue as Double] as T
            Float::class -> settingsProvider[key.name, defaultValue as Float] as T
            String::class -> settingsProvider[key.name, defaultValue as String] as T
            Boolean::class -> settingsProvider[key.name, defaultValue as Boolean] as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
}
