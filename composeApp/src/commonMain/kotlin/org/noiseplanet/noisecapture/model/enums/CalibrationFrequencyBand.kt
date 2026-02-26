package org.noiseplanet.noisecapture.model.enums

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_frequencies_whole_spectrum_title
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.util.toFrequencyString

@Serializable
enum class CalibrationFrequencyBand(val centerFrequency: Int) {

    WHOLE_SPECTRUM(centerFrequency = 0),

    HZ_125(centerFrequency = 125),
    HZ_250(centerFrequency = 250),
    HZ_500(centerFrequency = 500),
    KHZ_1(centerFrequency = 1_000),
    KHZ_4(centerFrequency = 4_000),
    KHZ_8(centerFrequency = 8_000),
    KHZ_16(centerFrequency = 16_000);

    val label: String
        @Composable get() = when (this) {
            WHOLE_SPECTRUM -> stringResource(Res.string.calibration_frequencies_whole_spectrum_title)
            else -> centerFrequency.toFrequencyString()
        }
}
