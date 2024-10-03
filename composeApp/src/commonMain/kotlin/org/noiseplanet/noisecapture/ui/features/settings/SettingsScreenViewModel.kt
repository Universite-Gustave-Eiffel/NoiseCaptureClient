package org.noiseplanet.noisecapture.ui.features.settings

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.settings_calibration_countdown_description
import noisecapture.composeapp.generated.resources.settings_calibration_countdown_title
import noisecapture.composeapp.generated.resources.settings_calibration_duration_description
import noisecapture.composeapp.generated.resources.settings_calibration_duration_title
import noisecapture.composeapp.generated.resources.settings_calibration_gain_correction_description
import noisecapture.composeapp.generated.resources.settings_calibration_gain_correction_title
import noisecapture.composeapp.generated.resources.settings_calibration_output_description
import noisecapture.composeapp.generated.resources.settings_calibration_output_title
import noisecapture.composeapp.generated.resources.settings_general_automatic_transfer_description
import noisecapture.composeapp.generated.resources.settings_general_automatic_transfer_title
import noisecapture.composeapp.generated.resources.settings_general_disclaimer_description
import noisecapture.composeapp.generated.resources.settings_general_disclaimer_title
import noisecapture.composeapp.generated.resources.settings_general_notification_description
import noisecapture.composeapp.generated.resources.settings_general_notification_title
import noisecapture.composeapp.generated.resources.settings_general_tooltips_description
import noisecapture.composeapp.generated.resources.settings_general_tooltips_title
import noisecapture.composeapp.generated.resources.settings_general_wifi_only_description
import noisecapture.composeapp.generated.resources.settings_general_wifi_only_title
import noisecapture.composeapp.generated.resources.settings_map_measurements_count_description
import noisecapture.composeapp.generated.resources.settings_map_measurements_count_title
import noisecapture.composeapp.generated.resources.settings_measurements_limit_duration_description
import noisecapture.composeapp.generated.resources.settings_measurements_limit_duration_title
import noisecapture.composeapp.generated.resources.settings_measurements_max_duration_description
import noisecapture.composeapp.generated.resources.settings_measurements_max_duration_title
import noisecapture.composeapp.generated.resources.settings_measurements_spectrogram_mode_description
import noisecapture.composeapp.generated.resources.settings_measurements_spectrogram_mode_title
import noisecapture.composeapp.generated.resources.settings_measurements_windowing_description
import noisecapture.composeapp.generated.resources.settings_measurements_windowing_title
import noisecapture.composeapp.generated.resources.settings_section_calibration
import noisecapture.composeapp.generated.resources.settings_section_general
import noisecapture.composeapp.generated.resources.settings_section_map
import noisecapture.composeapp.generated.resources.settings_section_measurements
import noisecapture.composeapp.generated.resources.settings_section_user_profile
import noisecapture.composeapp.generated.resources.settings_user_acoustics_knowledge_description
import noisecapture.composeapp.generated.resources.settings_user_acoustics_knowledge_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.services.SettingsKey
import org.noiseplanet.noisecapture.services.UserSettingsService
import org.noiseplanet.noisecapture.ui.features.settings.item.SettingsItemViewModel

class SettingsScreenViewModel(
    private val settingsService: UserSettingsService,
) : ViewModel() {

    val settingsItems: Map<StringResource, List<SettingsItemViewModel<*>>> = mapOf(

        // TODO: Fill in with actual settings values instead of placeholders

        Pair(
            Res.string.settings_section_user_profile, listOf(
                SettingsItemViewModel(
                    title = Res.string.settings_user_acoustics_knowledge_title,
                    description = Res.string.settings_user_acoustics_knowledge_description,
                    settingKey = SettingsKey.UserAcousticsKnowledge,
                    settingsService = settingsService,
                    isFirstInSection = true,
                    isLastInSection = true,
                ),
            )
        ),
        Pair(
            Res.string.settings_section_general, listOf(
                SettingsItemViewModel(
                    title = Res.string.settings_general_tooltips_title,
                    description = Res.string.settings_general_tooltips_description,
                    settingKey = SettingsKey.TooltipsEnabled,
                    settingsService = settingsService,
                    isFirstInSection = true,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_general_disclaimer_title,
                    description = Res.string.settings_general_disclaimer_description,
                    settingKey = SettingsKey.DisclaimersEnabled,
                    settingsService = settingsService,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_general_notification_title,
                    description = Res.string.settings_general_notification_description,
                    settingKey = SettingsKey.NotificationEnabled,
                    settingsService = settingsService,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_general_automatic_transfer_title,
                    description = Res.string.settings_general_automatic_transfer_description,
                    settingKey = SettingsKey.AutomaticTransferEnabled,
                    settingsService = settingsService,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_general_wifi_only_title,
                    description = Res.string.settings_general_wifi_only_description,
                    settingKey = SettingsKey.TransferOverWifiOnly,
                    settingsService = settingsService,
                    isLastInSection = true,
                ),
            )
        ),
        Pair(
            Res.string.settings_section_measurements, listOf(
                SettingsItemViewModel(
                    title = Res.string.settings_measurements_windowing_title,
                    description = Res.string.settings_measurements_windowing_description,
                    settingKey = SettingsKey.WindowingMode,
                    settingsService = settingsService,
                    isFirstInSection = true,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_measurements_limit_duration_title,
                    description = Res.string.settings_measurements_limit_duration_description,
                    settingKey = SettingsKey.LimitMeasurementDuration,
                    settingsService = settingsService,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_measurements_max_duration_title,
                    description = Res.string.settings_measurements_max_duration_description,
                    settingKey = SettingsKey.MaxMeasurementDuration,
                    settingsService = settingsService,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_measurements_spectrogram_mode_title,
                    description = Res.string.settings_measurements_spectrogram_mode_description,
                    settingKey = SettingsKey.SpectrogramScaleMode,
                    settingsService = settingsService,
                    isLastInSection = true
                ),
            )
        ),
        Pair(
            Res.string.settings_section_calibration, listOf(
                SettingsItemViewModel(
                    title = Res.string.settings_calibration_gain_correction_title,
                    description = Res.string.settings_calibration_gain_correction_description,
                    settingKey = SettingsKey.SignalGainCorrection,
                    settingsService = settingsService,
                    isFirstInSection = true,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_calibration_countdown_title,
                    description = Res.string.settings_calibration_countdown_description,
                    settingKey = SettingsKey.CalibrationCountdown,
                    settingsService = settingsService,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_calibration_duration_title,
                    description = Res.string.settings_calibration_duration_description,
                    settingKey = SettingsKey.CalibrationDuration,
                    settingsService = settingsService,
                ),
                SettingsItemViewModel(
                    title = Res.string.settings_calibration_output_title,
                    description = Res.string.settings_calibration_output_description,
                    settingKey = SettingsKey.TestSignalAudioOutput,
                    settingsService = settingsService,
                    isLastInSection = true
                ),
            )
        ),
        Pair(
            Res.string.settings_section_map, listOf(
                SettingsItemViewModel(
                    title = Res.string.settings_map_measurements_count_title,
                    description = Res.string.settings_map_measurements_count_description,
                    settingKey = SettingsKey.MapMaxMeasurementsCount,
                    settingsService = settingsService,
                    isFirstInSection = true,
                    isLastInSection = true,
                ),
            )
        )
    )
}
