package org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrum

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.noiseplanet.noisecapture.services.LiveAudioService
import org.noiseplanet.noisecapture.util.toComposeColor
import kotlin.math.max

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

        // TODO: Move this somewhere it can be shared between views like a themes file or smth
        val noiseColorRampSpl: List<Pair<Float, Color>> = listOf(
            Pair(75F, "#FF0000".toComposeColor()), // >= 75 dB
            Pair(65F, "#FF8000".toComposeColor()), // >= 65 dB
            Pair(55F, "#FFFF00".toComposeColor()), // >= 55 dB
            Pair(45F, "#99FF00".toComposeColor()), // >= 45 dB
            Pair(Float.NEGATIVE_INFINITY, "#00FF00".toComposeColor())
        ) // < 45 dB
    }

    // color ramp 0F left side of spectrum
    // 1F right side of spectrum
    val spectrumColorRamp = List(noiseColorRampSpl.size) { index ->
        val pair = noiseColorRampSpl[noiseColorRampSpl.size - 1 - index]
        val linearIndex = max(0.0, ((pair.first - DBA_MIN) / (DBA_MAX - DBA_MIN)))
        Pair(linearIndex.toFloat(), pair.second)
    }.toTypedArray()

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
