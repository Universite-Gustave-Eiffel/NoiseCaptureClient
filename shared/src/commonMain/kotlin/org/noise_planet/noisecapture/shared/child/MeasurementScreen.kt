package org.noise_planet.noisecapture.shared.child

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.signal.SpectrumChannel
import org.noise_planet.noisecapture.shared.signal.WindowAnalysis
import org.noise_planet.noisecapture.shared.signal.get44100HZ
import org.noise_planet.noisecapture.shared.signal.get48000HZ
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round


const val FFT_SIZE = 2048
const val FFT_HOP = 1024
const val MAX_SPECTRUM = 320 // max spectrum displayed in the spectrogram
fun parseColor(colorString: String): Int {
    var color = colorString.substring(1).toLong(16)
    if (colorString.length == 7) {
        // Set the alpha value
        color = color or 0x00000000ff000000L
    } else if (colorString.length != 9) {
        throw IllegalArgumentException("Unknown color")
    }
    return color.toInt()
}

fun String.toComposeColor(): Color {
    return Color(parseColor(this))
}

enum class SCALE_MODE {
    SCALE_LINEAR,
    SCALE_LOG
}

val colorRamp = arrayOf(
    "#303030".toComposeColor(),
    "#2D3C2D".toComposeColor(),
    "#2A482A".toComposeColor(),
    "#275427".toComposeColor(),
    "#246024".toComposeColor(),
    "#216C21".toComposeColor(),
    "#3F8E19".toComposeColor(),
    "#61A514".toComposeColor(),
    "#82BB0F".toComposeColor(),
    "#A4D20A".toComposeColor(),
    "#C5E805".toComposeColor(),
    "#E7FF00".toComposeColor(),
    "#EBD400".toComposeColor(),
    "#EFAA00".toComposeColor(),
    "#F37F00".toComposeColor(),
    "#F75500".toComposeColor(),
    "#FB2A00".toComposeColor(),
)

data class SpectrogramModel(val values: List<Brush>)


class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val audioSource: AudioSource) : Node(buildContext) {
    private val spectrumChannel: SpectrumChannel = SpectrumChannel()

    var rangedB = 0.0
    var mindB = Double.MAX_VALUE


    fun pushSpectrum(prepend : List<Brush>, spectrum : FloatArray,
                     scaleMode : SCALE_MODE) : List<Brush> {
        if (spectrum.isEmpty()) {
            return prepend
        }
        // Update dynamic range dB->Color index
        mindB = min(mindB, spectrum.filter { fl -> fl.isFinite() }.minOrNull()?.toDouble() ?: mindB)
        rangedB = max(mindB+rangedB,
            spectrum.filter { fl -> fl.isFinite() }.maxOrNull()?.toDouble()
                ?: (mindB + rangedB)
        ) - mindB
        // map spectrum value with brush ratio and rendering color
        val spectrumColor = spectrum.mapIndexed { index, magnitude ->
            val colorIndex = max( 0, min( colorRamp.size - 1,
                floor(((magnitude - mindB) / rangedB) * colorRamp.size).toInt() ))
            val verticalRatio = when(scaleMode) {
                SCALE_MODE.SCALE_LOG -> log10(spectrum.size/(index+1).toFloat())
                else -> spectrum.size/index.toFloat()
            }
            verticalRatio to colorRamp[colorIndex]
        }.toTypedArray()
        return prepend.subList(max(0,prepend.size-MAX_SPECTRUM), prepend.size)
            .plus(Brush.verticalGradient(colorStops = spectrumColor))
    }

    @Composable
    fun SpectrogramView(spectrumData: SpectrogramModel) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val xStep = size.width/spectrumData.values.size
            spectrumData.values.forEachIndexed() { index, brush ->
                drawRect(brush = brush, size= Size(xStep, size.height),
                    topLeft = Offset(index * xStep, 0F))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        var noiseLevel by remember { mutableStateOf(0.0) }
        var spectrumState by remember { mutableStateOf(SpectrogramModel(listOf())) }

        lifecycleScope.launch {
            audioSource.setup()
            spectrumChannel.loadConfiguration(
                when (audioSource.getSampleRate()) {
                    48000 -> get48000HZ()
                    else -> get44100HZ()
                }
            )
            val windowAnalysis = WindowAnalysis(audioSource.getSampleRate(), FFT_SIZE, FFT_HOP)
            audioSource.samples.collect { samples ->
                val gain = (10.0.pow(105/20.0)).toFloat()
                val samplesWithGain = samples.samples.map()
                {it*gain}.toFloatArray()
                val spl = spectrumChannel.processSamplesWeightA(samplesWithGain)
                noiseLevel = spl
                windowAnalysis.pushSamples(
                    Clock.System.now().toEpochMilliseconds(), samplesWithGain)
                    .forEach {
                        spectrumState = SpectrogramModel(pushSpectrum(spectrumState.values,
                            it.spectrum, SCALE_MODE.SCALE_LINEAR))
                }
            }
        }.invokeOnCompletion {
            println("On completion $it subs ${audioSource.samples.subscriptionCount.value}")
            if(audioSource.samples.subscriptionCount.value == 0) {
                audioSource.release()
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text("${round(noiseLevel * 100)/100} dB(A)")
                SpectrogramView(spectrumData = spectrumState)
            }
        }
    }

}

