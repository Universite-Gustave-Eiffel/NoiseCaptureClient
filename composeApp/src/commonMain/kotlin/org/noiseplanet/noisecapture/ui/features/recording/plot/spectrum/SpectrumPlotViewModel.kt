package org.noiseplanet.noisecapture.ui.features.recording.plot.spectrum

import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.ui.components.plot.AxisTick
import org.noiseplanet.noisecapture.ui.components.plot.PlotAxisSettings
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed
import org.noiseplanet.noisecapture.util.toFrequencyString

class SpectrumPlotViewModel : ViewModel(), KoinComponent {

    // - Associated types

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

    val axisSettingsFlow: StateFlow<PlotAxisSettings> = liveAudioService
        .getLeqRecordsFlow()
        .map { it.leqsPerThirdOctave.keys.toList() }
        .distinctUntilChanged()
        .map { frequencies ->
            PlotAxisSettings(
                xTicks = (0..DBA_TICKS_COUNT).map { tick ->
                    val tickValue = (DBA_MAX - DBA_MIN) / DBA_TICKS_COUNT * tick
                    AxisTick(
                        value = tickValue,
                        label = "${tickValue.toInt()} dB"
                    )
                },
                yTicks = frequencies.map { freq ->
                    AxisTick(
                        value = freq.toDouble(),
                        label = freq.toFrequencyString(),
                    )
                },
                showYTickMarks = false,
                yAxisLayoutDirection = LayoutDirection.Rtl,
            )
        }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = PlotAxisSettings()
        )
}
