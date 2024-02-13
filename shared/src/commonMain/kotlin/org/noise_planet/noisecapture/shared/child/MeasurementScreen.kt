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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round


const val FFT_SIZE = 2048
const val FFT_HOP = 1024

fun String.hexToArgb(): Int {
    require(length == 7 && startsWith("#")) { "Invalid hex color format." }
    val rgb = substring(1, 3).toInt(16) shl 16 or (substring(3, 5).toInt(16) shl 8) or substring(5, 7).toInt(16)
    return rgb
}

fun String.toComposeColor(): Color {
    return Color(hexToArgb())
}


class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val audioSource: AudioSource) : Node(buildContext) {
    private val spectrumChannel: SpectrumChannel = SpectrumChannel()

    var rangedB = 40.0
    var mindB = 35.0

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

    @Composable
    fun SpectrogramView(spectrumData: FloatArray) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            //drawRect(Color.Black)
            val stepSize = size.height / spectrumData.size
            mindB = min(mindB, spectrumData.filter { fl -> fl.isFinite() }.minOrNull()?.toDouble() ?: mindB)
            rangedB = max(mindB+rangedB,
                spectrumData.filter { fl -> fl.isFinite() }.maxOrNull()?.toDouble()
                    ?: (mindB + rangedB)
            ) - mindB
            drawRect(color = Color.Black)
//            for ((index, magnitude) in spectrumData.withIndex()) {
//                val yPos = ((index / spectrumData.size.toDouble()) * size.height).toFloat()
//                val colorIndex = max(0, min(colorRamp.size - 1, floor(((magnitude - mindB)
//                        / rangedB) * colorRamp.size).toInt()))
//                drawLine(color = colorRamp[colorIndex] , start = Offset(size.width / 2, yPos),
//                    end = Offset(size.width / 2, yPos+stepSize), strokeWidth = stepSize)
//            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        var noiseLevel by remember { mutableStateOf(0.0) }
        var spectrumState by remember {  mutableStateOf(floatArrayOf()) }

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
                        spectrumState = it.spectrum
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

