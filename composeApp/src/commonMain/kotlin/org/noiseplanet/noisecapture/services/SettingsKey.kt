package org.noiseplanet.noisecapture.services

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import org.noiseplanet.noisecapture.model.AcousticsKnowledgeLevel
import org.noiseplanet.noisecapture.model.SpectrogramScaleMode

/**
 * User settings keys. Each value must be serializable.
 *
 * For now [defaultValue] is enforced. If we need nullable setting values in the future
 * we may need to make it optional.
 */
sealed class SettingsKey<T>(val serializer: KSerializer<T>, val defaultValue: T) {

    // User profile
    data object SettingUserAcousticsKnowledge : SettingsKey<AcousticsKnowledgeLevel>(
        AcousticsKnowledgeLevel.serializer(),
        defaultValue = AcousticsKnowledgeLevel.BEGINNER,
    )

    // General
    data object SettingTooltipsEnabled : SettingsKey<Boolean>(
        Boolean.serializer(),
        defaultValue = true,
    )

    data object SettingDisclaimersEnabled : SettingsKey<Boolean>(
        Boolean.serializer(),
        defaultValue = true,
    )

    data object SettingNotificationEnabled : SettingsKey<Boolean>(
        Boolean.serializer(),
        defaultValue = true,
    )

    data object SettingAutomaticTransferEnabled : SettingsKey<Boolean>(
        Boolean.serializer(),
        defaultValue = true,
    )

    data object SettingTransferOverWifiOnly : SettingsKey<Boolean>(
        Boolean.serializer(),
        defaultValue = true,
    )

    // Measurements
    data object SettingWindowingMode : SettingsKey<String>(
        String.serializer(),
        defaultValue = "TODO",
    ) // TODO: Create enum

    data object SettingLimitMeasurementDuration : SettingsKey<Boolean>(
        Boolean.serializer(),
        defaultValue = false,
    )

    data object SettingMaxMeasurementDuration : SettingsKey<Int>(
        Int.serializer(),
        defaultValue = 30,
    )

    data object SettingSpectrogramScaleMode : SettingsKey<SpectrogramScaleMode>(
        SpectrogramScaleMode.serializer(),
        defaultValue = SpectrogramScaleMode.SCALE_LOG,
    )

    // Calibration
    data object SettingSignalGainCorrection : SettingsKey<Double>(
        Double.serializer(),
        defaultValue = 0.0,
    )

    data object SettingCalibrationCountdown : SettingsKey<Int>(
        Int.serializer(),
        defaultValue = 4,
    )

    data object SettingCalibrationDuration : SettingsKey<Int>(
        Int.serializer(),
        defaultValue = 6,
    )

    data object SettingTestSignalAudioOutput : SettingsKey<String>(
        String.serializer(),
        defaultValue = "TODO",
    ) // TODO: Create enum

    // Map
    data object SettingMapMaxMeasurementsCount : SettingsKey<Int>(
        Int.serializer(),
        defaultValue = 500,
    )
}
