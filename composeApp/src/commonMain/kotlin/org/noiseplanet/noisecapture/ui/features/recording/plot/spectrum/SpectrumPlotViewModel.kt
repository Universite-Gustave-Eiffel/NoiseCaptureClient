package org.noiseplanet.noisecapture.ui.features.recording.plot.spectrum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed

class SpectrumPlotViewModel : ViewModel(), KoinComponent {

    // - Associated types

    data class AxisSettings(
        val nominalFrequencies: List<Int>,
        val minimumX: Double = DBA_MIN,
        val maximumX: Double = DBA_MAX,
        val xTicksCount: Int = DBA_TICKS_COUNT,
    )

    data class SplData(
        val raw: Double,
        val weighted: Double,
    )


    // - Constants

    companion object {

        const val DBA_MIN = 0.0
        const val DBA_MAX = 100.0
        const val DBA_TICKS_COUNT = 4
    }


    // - Properties

    private val liveAudioService: LiveAudioService by inject()

    // color ramp 0F left side of spectrum, 1F right side of spectrum
    val spectrumColorRamp = NoiseLevelColorRamp.palette.map { (spl, color) ->
        // Map spl index to a value between 0 and 1 based on min/max dB values
        val rampIndex = (spl - DBA_MIN) / (DBA_MAX - DBA_MIN)
        Pair(rampIndex.toFloat(), color)
    }

    val splDataFlow: StateFlow<Map<Int, SplData>> = liveAudioService
        .getLeqRecordsFlow()
        .zip(liveAudioService.getWeightedLeqPerFrequencyBandFlow()) { raw, weighted ->
            raw.leqsPerThirdOctave.mapValues { entry ->
                SplData(entry.value, weighted[entry.key] ?: 0.0)
            }
        }.stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = emptyMap(),
        )

    val axisSettingsFlow: StateFlow<AxisSettings> = liveAudioService
        .getLeqRecordsFlow()
        .map { it.leqsPerThirdOctave.keys.toList() }
        .distinctUntilChanged()
        .map { AxisSettings(nominalFrequencies = it) }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = AxisSettings(nominalFrequencies = emptyList())
        )
}
