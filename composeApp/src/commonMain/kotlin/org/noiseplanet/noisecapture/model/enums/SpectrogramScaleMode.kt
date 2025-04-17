package org.noiseplanet.noisecapture.model.enums

import kotlinx.serialization.Serializable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.spectrogram_scale_mode_linear_description
import noisecapture.composeapp.generated.resources.spectrogram_scale_mode_linear_title
import noisecapture.composeapp.generated.resources.spectrogram_scale_mode_logarithmic_description
import noisecapture.composeapp.generated.resources.spectrogram_scale_mode_logarithmic_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.util.IterableEnum
import org.noiseplanet.noisecapture.util.ShortNameRepresentable
import kotlin.enums.EnumEntries


@Serializable
enum class SpectrogramScaleMode : IterableEnum<SpectrogramScaleMode>, ShortNameRepresentable {

    SCALE_LINEAR {

        override val fullName: StringResource =
            Res.string.spectrogram_scale_mode_linear_description
        override val shortName: StringResource =
            Res.string.spectrogram_scale_mode_linear_title
    },

    SCALE_LOG {

        override val fullName: StringResource =
            Res.string.spectrogram_scale_mode_logarithmic_description
        override val shortName: StringResource =
            Res.string.spectrogram_scale_mode_logarithmic_title
    };

    override fun entries(): EnumEntries<SpectrogramScaleMode> = entries
}
