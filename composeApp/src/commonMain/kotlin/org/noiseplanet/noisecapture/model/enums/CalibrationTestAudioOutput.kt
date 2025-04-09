package org.noiseplanet.noisecapture.model.enums

import kotlinx.serialization.Serializable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_output_alarm_description
import noisecapture.composeapp.generated.resources.calibration_output_alarm_title
import noisecapture.composeapp.generated.resources.calibration_output_dmtf_description
import noisecapture.composeapp.generated.resources.calibration_output_dmtf_title
import noisecapture.composeapp.generated.resources.calibration_output_music_description
import noisecapture.composeapp.generated.resources.calibration_output_music_title
import noisecapture.composeapp.generated.resources.calibration_output_notification_description
import noisecapture.composeapp.generated.resources.calibration_output_notification_title
import noisecapture.composeapp.generated.resources.calibration_output_phonecall_description
import noisecapture.composeapp.generated.resources.calibration_output_phonecall_title
import noisecapture.composeapp.generated.resources.calibration_output_ringtone_description
import noisecapture.composeapp.generated.resources.calibration_output_ringtone_title
import noisecapture.composeapp.generated.resources.calibration_output_system_sound_description
import noisecapture.composeapp.generated.resources.calibration_output_system_sound_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.util.IterableEnum
import org.noiseplanet.noisecapture.util.ShortNameRepresentable
import kotlin.enums.EnumEntries

@Serializable
enum class CalibrationTestAudioOutput : IterableEnum<CalibrationTestAudioOutput>,
                                        ShortNameRepresentable {

    PHONE_CALL {

        override val shortName: StringResource =
            Res.string.calibration_output_phonecall_title
        override val fullName: StringResource =
            Res.string.calibration_output_phonecall_description
    },

    SYSTEM_SOUND {

        override val shortName: StringResource =
            Res.string.calibration_output_system_sound_title
        override val fullName: StringResource =
            Res.string.calibration_output_system_sound_description
    },

    RINGTONE {

        override val shortName: StringResource =
            Res.string.calibration_output_ringtone_title
        override val fullName: StringResource =
            Res.string.calibration_output_ringtone_description
    },

    MUSIC {

        override val shortName: StringResource =
            Res.string.calibration_output_music_title
        override val fullName: StringResource =
            Res.string.calibration_output_music_description
    },

    ALARM {

        override val shortName: StringResource =
            Res.string.calibration_output_alarm_title
        override val fullName: StringResource =
            Res.string.calibration_output_alarm_description
    },

    NOTIFICATION {

        override val shortName: StringResource =
            Res.string.calibration_output_notification_title
        override val fullName: StringResource =
            Res.string.calibration_output_notification_description
    },

    DMTF {

        override val shortName: StringResource =
            Res.string.calibration_output_dmtf_title
        override val fullName: StringResource =
            Res.string.calibration_output_dmtf_description
    };

    override fun entries(): EnumEntries<CalibrationTestAudioOutput> = entries
}
