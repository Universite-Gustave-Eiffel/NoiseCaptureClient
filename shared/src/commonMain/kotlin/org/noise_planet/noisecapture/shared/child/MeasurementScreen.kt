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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.launch
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.signal.SpectrumChannel
import org.noise_planet.noisecapture.shared.signal.get44100HZ
import org.noise_planet.noisecapture.shared.signal.get48000HZ
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime
import kotlin.time.measureTimedValue


const val FFT_SIZE = 2048
const val FFT_HOP = 2048
const val WINDOW_TIME = 0.125
const val MAX_SPECTRUM = 320 // max spectrum displayed in the spectrogram
const val SKIP_FFT_CELLS_LOG = 20 // skip low frequency in log spectrum to avoid squeezed rendering
val EMPTY_BITMAP = ImageBitmap(1, 1)

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

data class SpectrogramDataModel(val size: IntSize, val byteArray: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SpectrogramDataModel

        if (size != other.size) return false
        return byteArray.contentEquals(other.byteArray)
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}


class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val audioSource: AudioSource) : Node(buildContext) {
    private val spectrumChannel: SpectrumChannel = SpectrumChannel()
    var rangedB = 40.0
    var mindB = 0
    var spectrogramBitmapData = SpectrogramDataModel(IntSize(1, 1), ByteArray(Int.SIZE_BYTES))

    fun <Color> List<Pair<Float, Color>>.filterConsecutiveEqual(): Array<Pair<Float, Color>> {
        if(size <= 2) {
            return this.toTypedArray()
        }
        val result = ArrayList<Pair<Float, Color>>()
        var firstItem : Pair<Float, Color> = get(0)
        var secondItem : Pair<Float, Color> = get(1)
        result.add(firstItem)
        this.takeLast(size - 2).forEachIndexed { index, item ->
            if (!(secondItem.second == firstItem.second && secondItem.second == item.second)) {
                result.add(secondItem)
                firstItem = secondItem
                secondItem = item
            }
        }
        result.add(get(size-1))
        return result.toTypedArray()
    }

    fun spectrumToBrush(spectrum : FloatArray,
                     scaleMode : SCALE_MODE) : Brush {
        require(spectrum.isNotEmpty())
        // map spectrum value with brush ratio and rendering color
        val maxNonRatio = when(scaleMode) {
            SCALE_MODE.SCALE_LOG -> log10(spectrum.size.toFloat())
            else -> spectrum.size.toFloat()
        }
        val skip = when(scaleMode) { SCALE_MODE.SCALE_LOG -> SKIP_FFT_CELLS_LOG else -> 0}
        val skipOffset = log10(spectrum.size/(skip+1).toFloat())  / maxNonRatio
        val rescaleOffset = (1/skipOffset)
        var minRatio = Float.MAX_VALUE
        var maxRatio = 0F
        val spectrumColor = spectrum.takeLast(spectrum.size - skip).mapIndexed { index, magnitude ->
            val colorIndex = max( 0, min( colorRamp.size - 1,
                floor(((magnitude - mindB) / rangedB) * colorRamp.size).toInt() ))
            val verticalRatio : Float = when(scaleMode) {
                SCALE_MODE.SCALE_LOG -> {
                    (skipOffset - ((log10(spectrum.size/(index+skip+1).toFloat())) / maxNonRatio)
                            ) * rescaleOffset
                }
                else -> index.toFloat() / maxNonRatio
            }
            minRatio = min(minRatio, verticalRatio)
            maxRatio = max(maxRatio, verticalRatio)
            verticalRatio to colorRamp[colorIndex]
        }.filterConsecutiveEqual()
        return Brush.verticalGradient(colorStops = spectrumColor)
    }

    @Composable
    override fun View(modifier: Modifier) {
        var noiseLevel by remember { mutableStateOf(0.0) }
        val spectrumBitmapState by remember { mutableStateOf(EMPTY_BITMAP) }

        lifecycleScope.launch {
            println("Launch lifecycle")
            audioSource.setup()
            spectrumChannel.loadConfiguration(
                when (audioSource.getSampleRate()) {
                    48000 -> get48000HZ()
                    else -> get44100HZ()
                }
            )
            val windowLength = (audioSource.getSampleRate() * WINDOW_TIME).toInt()
            val windowData = FloatArray(windowLength)
            val windowTime = (windowData.size/audioSource.getSampleRate().toDouble()).seconds
            var windowDataCursor = 0
            //val windowAnalysis = WindowAnalysis(audioSource.getSampleRate(), FFT_SIZE, FFT_HOP)
            audioSource.samples.collect { samples ->
                val gain = (10.0.pow(105/20.0)).toFloat()
                var samplesProcessed = 0
                while (samplesProcessed < samples.samples.size) {
                    while (windowDataCursor < windowLength &&
                        samplesProcessed < samples.samples.size) {
                        val remainingToProcess = min(
                            windowLength - windowDataCursor,
                            samples.samples.size - samplesProcessed
                        )
                        for (i in 0..<remainingToProcess) {
                            windowData[i + windowDataCursor] =
                                samples.samples[i + samplesProcessed] * gain
                        }
                        windowDataCursor += remainingToProcess
                        samplesProcessed += remainingToProcess
                    }
                    if (windowDataCursor == windowLength) {
                        // window complete
                        var thirdOctave : DoubleArray
                        val processingTime = measureTime {
                            noiseLevel = spectrumChannel.processSamplesWeightA(windowData)
                            thirdOctave = spectrumChannel.processSamples(windowData)
                            //windowAnalysis.pushSamples(samples.epoch, windowData).forEach {
                            //   spectrumDataToProcess.tryEmit(it)
                            //}
                        }
                        println("Processed $windowTime of audio in $processingTime")
                        windowDataCursor = 0
                    }
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
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasSize = IntSize(size.width.toInt(), size.height.toInt())
                    if(spectrogramBitmapData.size != canvasSize) {
                        // reset buffer on resize or first draw
                        spectrogramBitmapData = SpectrogramDataModel(canvasSize,
                            ByteArray(Int.SIZE_BYTES * canvasSize.width * canvasSize.height))
                    } else {
                        drawImage(spectrumBitmapState)
                    }
                }
            }
        }
    }

}

