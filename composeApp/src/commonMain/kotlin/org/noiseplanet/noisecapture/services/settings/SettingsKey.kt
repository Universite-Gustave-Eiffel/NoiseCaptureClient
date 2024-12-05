package org.noiseplanet.noisecapture.services.settings

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import org.noiseplanet.noisecapture.model.AcousticsKnowledgeLevel
import org.noiseplanet.noisecapture.model.CalibrationTestAudioOutput
import org.noiseplanet.noisecapture.model.MeasurementWindowingMode
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
    data object SettingWindowingMode : SettingsKey<MeasurementWindowingMode>(
        MeasurementWindowingMode.serializer(),
        defaultValue = MeasurementWindowingMode.HANN,
    )

    data object SettingLimitMeasurementDuration : SettingsKey<Boolean>(
        Boolean.serializer(),
        defaultValue = false,
    )

    data object SettingMaxMeasurementDuration : SettingsKey<UInt>(
        UInt.serializer(),
        defaultValue = 30u,
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

    data object SettingCalibrationCountdown : SettingsKey<UInt>(
        UInt.serializer(),
        defaultValue = 4u,
    )

    data object SettingCalibrationDuration : SettingsKey<UInt>(
        UInt.serializer(),
        defaultValue = 6u,
    )

    data object SettingTestSignalAudioOutput : SettingsKey<CalibrationTestAudioOutput>(
        CalibrationTestAudioOutput.serializer(),
        defaultValue = CalibrationTestAudioOutput.PHONE_CALL,
    )

    // Map
    data object SettingMapMaxMeasurementsCount : SettingsKey<UInt>(
        UInt.serializer(),
        defaultValue = 500u,
    )
}
