package org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrum

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.noiseplanet.noisecapture.services.liveaudio.LiveAudioService
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp

class SpectrumPlotViewModel(
    private val liveAudioService: LiveAudioService,
) : ViewModel() {

    data class AxisSettings(
        val minimumX: Double,
        val maximumX: Double,
        val nominalFrequencies: List<Double>,
    )

    companion object {

        const val DBA_MIN = 0.0
        const val DBA_MAX = 100.0
    }

    // color ramp 0F left side of spectrum, 1F right side of spectrum
    val spectrumColorRamp = NoiseLevelColorRamp.palette.map { (spl, color) ->
        // Map spl index to a value between 0 and 1 based on min/max dB values
        val rampIndex = (spl - DBA_MIN) / (DBA_MAX - DBA_MIN)
        Pair(rampIndex.toFloat(), color)
    }

    val rawSplFlow: Flow<DoubleArray> = liveAudioService
        .getAcousticIndicatorsFlow()
        .map { it.thirdOctave }

    val weightedSplFlow: Flow<DoubleArray> = liveAudioService
        .getWeightedSoundPressureLevelFlow()

    val axisSettingsFlow: Flow<AxisSettings> = liveAudioService
        .getAcousticIndicatorsFlow()
        .map { it.nominalFrequencies }
        .distinctUntilChanged()
        .map {
            AxisSettings(
                minimumX = DBA_MIN,
                maximumX = DBA_MAX,
                nominalFrequencies = it
            )
        }
}
