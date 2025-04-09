package org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrum

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp

class SpectrumPlotViewModel : ViewModel(), KoinComponent {

    // - Associated types

    data class AxisSettings(
        val minimumX: Double,
        val maximumX: Double,
        val nominalFrequencies: List<Int>,
    )


    // - Constants

    companion object {

        const val DBA_MIN = 0.0
        const val DBA_MAX = 100.0
    }


    // - Properties

    private val liveAudioService: LiveAudioService by inject()

    // color ramp 0F left side of spectrum, 1F right side of spectrum
    val spectrumColorRamp = NoiseLevelColorRamp.palette.map { (spl, color) ->
        // Map spl index to a value between 0 and 1 based on min/max dB values
        val rampIndex = (spl - DBA_MIN) / (DBA_MAX - DBA_MIN)
        Pair(rampIndex.toFloat(), color)
    }

    val rawSplFlow: Flow<Map<Int, Double>> = liveAudioService
        .getAcousticIndicatorsFlow()
        .map { it.leqsPerThirdOctave }

    val weightedSplFlow: Flow<Map<Int, Double>> = liveAudioService
        .getWeightedSoundPressureLevelFlow()

    val axisSettingsFlow: Flow<AxisSettings> = liveAudioService
        .getAcousticIndicatorsFlow()
        .map { it.leqsPerThirdOctave.keys.toList() }
        .distinctUntilChanged()
        .map {
            AxisSettings(
                minimumX = DBA_MIN,
                maximumX = DBA_MAX,
                nominalFrequencies = it
            )
        }
}
