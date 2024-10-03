package org.noiseplanet.noisecapture.services

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrogram.SpectrogramBitmap
import kotlin.reflect.KClass


/**
 * User settings keys
 */
sealed class SettingsKey<T : Any>(val valueType: KClass<T>, val defaultValue: T? = null) {

    // For testing purpose
    data object Test : SettingsKey<Int>(Int::class)

    // User profile
    data object UserAcousticsKnowledge : SettingsKey<String>(String::class)

    // General
    data object TooltipsEnabled : SettingsKey<Boolean>(Boolean::class)
    data object DisclaimersEnabled : SettingsKey<Boolean>(Boolean::class)
    data object NotificationEnabled : SettingsKey<Boolean>(Boolean::class)
    data object AutomaticTransferEnabled : SettingsKey<Boolean>(Boolean::class)
    data object TransferOverWifiOnly : SettingsKey<Boolean>(Boolean::class)

    // Measurements
    data object WindowingMode : SettingsKey<String>(String::class)
    data object LimitMeasurementDuration : SettingsKey<Boolean>(Boolean::class)
    data object MaxMeasurementDuration : SettingsKey<Int>(Int::class)
    data object SpectrogramScaleMode : SettingsKey<String>(String::class)

    // Calibration
    data object SignalGainCorrection : SettingsKey<Double>(Double::class)
    data object CalibrationCountdown : SettingsKey<Int>(Int::class)
    data object CalibrationDuration : SettingsKey<Int>(Int::class)
    data object TestSignalAudioOutput : SettingsKey<String>(String::class)

    // Map
    data object MapMaxMeasurementsCount : SettingsKey<Int>(Int::class)

    companion object {

        val defaults = mapOf(
            Test to 42,

            TooltipsEnabled to true,
            DisclaimersEnabled to true,
            NotificationEnabled to true,
            AutomaticTransferEnabled to true,
            TransferOverWifiOnly to true,

            WindowingMode to "TODO", // TODO: Add an enum for this
            LimitMeasurementDuration to false,
            MaxMeasurementDuration to 30,
            SpectrogramScaleMode to SpectrogramBitmap.ScaleMode.SCALE_LOG.name,

            SignalGainCorrection to 0.0,
            CalibrationCountdown to 4,
            CalibrationDuration to 4,
            TestSignalAudioOutput to "TODO", // TODO: Add an enum for this
            MapMaxMeasurementsCount to 500,
        )
    }
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
     */
    fun <T : Any> set(key: SettingsKey<out T>, value: T?)

    /**
     * Gets the value associated to a given key, or null if value is not set.
     *
     * @param key User settings [SettingsKey]
     * @return Value or null if not found.
     */
    fun <T : Any> get(key: SettingsKey<out T>): T?

    /**
     * Gets the value associated to a given key, or [defaultValue] if value is not set.
     *
     * @param key User settings [SettingsKey]
     * @param defaultValue Default value to return if no value was found
     * @return Value or [defaultValue] if not found
     */
    fun <T : Any> get(key: SettingsKey<out T>, defaultValue: T): T

    /**
     * Gets a flow of values associated to a given key, starting with null if value is not set.
     *
     * @param key User settings [SettingsKey]
     * @return Value or null if not found.
     */
    fun <T : Any> getFlow(key: SettingsKey<out T>): Flow<T?>

    /**
     * Gets a flow of values associated to a given key, starting with [defaultValue] if value is not set.
     *
     * @param key User settings [SettingsKey]
     * @param defaultValue Default value to return if no value was found
     * @return Value or [defaultValue] if not found
     */
    fun <T : Any> getFlow(key: SettingsKey<out T>, defaultValue: T): Flow<T>
}


/**
 * Default [UserSettingsService] implementation relying on a platform specific settings provider
 *
 * @param settingsProvider Platform specific settings provider
 */
class DefaultUserSettingsService(
    private val settingsProvider: Settings,
) : UserSettingsService {

    /**
     * Tracks changes made to settings value.
     */
    private val settingsChangeListener = MutableSharedFlow<Unit>(replay = 1)

    init {
        // Upon instantiation, initialize default values if no value has already been set before
        SettingsKey.defaults.forEach { (key, value) ->
            if (get(key) == null) {
                set(key, value)
            }
        }
    }

    override fun <T : Any> set(key: SettingsKey<out T>, value: T?) {
        val name = requireNotNull(key::class.simpleName) {
            "Could not get name from settings key"
        }

        value?.let {
            when (key.valueType) {
                Int::class -> settingsProvider[name] = it as Int
                Long::class -> settingsProvider[name] = it as Long
                Double::class -> settingsProvider[name] = it as Double
                Float::class -> settingsProvider[name] = it as Float
                String::class -> settingsProvider[name] = it as String
                Boolean::class -> settingsProvider[name] = it as Boolean
                else -> throw IllegalArgumentException("Unsupported type")
            }
        } ?: {
            settingsProvider[name] = null
        }
        // Notify that a new value was stored
        settingsChangeListener.tryEmit(Unit)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: SettingsKey<out T>): T? {
        val name = requireNotNull(key::class.simpleName) {
            "Could not get name from settings key"
        }

        return when (key.valueType) {
            Int::class -> settingsProvider.getIntOrNull(name) as T?
            Long::class -> settingsProvider.getLongOrNull(name) as T?
            Double::class -> settingsProvider.getDoubleOrNull(name) as T?
            Float::class -> settingsProvider.getFloatOrNull(name) as T?
            String::class -> settingsProvider.getStringOrNull(name) as T?
            Boolean::class -> settingsProvider.getBooleanOrNull(name) as T?
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: SettingsKey<out T>, defaultValue: T): T {
        val name = requireNotNull(key::class.simpleName) {
            "Could not get name from settings key"
        }

        return when (key.valueType) {
            Int::class -> settingsProvider[name, defaultValue as Int] as T
            Long::class -> settingsProvider[name, defaultValue as Long] as T
            Double::class -> settingsProvider[name, defaultValue as Double] as T
            Float::class -> settingsProvider[name, defaultValue as Float] as T
            String::class -> settingsProvider[name, defaultValue as String] as T
            Boolean::class -> settingsProvider[name, defaultValue as Boolean] as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    override fun <T : Any> getFlow(key: SettingsKey<out T>): Flow<T?> {
        return settingsChangeListener
            .map { get(key) }
            .distinctUntilChanged()
    }

    override fun <T : Any> getFlow(key: SettingsKey<out T>, defaultValue: T): Flow<T> {
        return settingsChangeListener
            .map { get(key, defaultValue) }
            .distinctUntilChanged()
    }
}
