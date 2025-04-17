package org.noiseplanet.noisecapture.model.enums

import kotlinx.serialization.Serializable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_windowing_mode_hann_description
import noisecapture.composeapp.generated.resources.measurement_windowing_mode_hann_title
import noisecapture.composeapp.generated.resources.measurement_windowing_mode_rect_description
import noisecapture.composeapp.generated.resources.measurement_windowing_mode_rect_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.util.IterableEnum
import org.noiseplanet.noisecapture.util.ShortNameRepresentable
import kotlin.enums.EnumEntries

@Serializable
enum class MeasurementWindowingMode : IterableEnum<MeasurementWindowingMode>,
                                      ShortNameRepresentable {

    HANN {

        override val fullName: StringResource =
            Res.string.measurement_windowing_mode_hann_description
        override val shortName: StringResource =
            Res.string.measurement_windowing_mode_hann_title
    },

    RECTANGULAR {

        override val fullName: StringResource =
            Res.string.measurement_windowing_mode_rect_description
        override val shortName: StringResource =
            Res.string.measurement_windowing_mode_rect_title
    };

    override fun entries(): EnumEntries<MeasurementWindowingMode> = entries
}
