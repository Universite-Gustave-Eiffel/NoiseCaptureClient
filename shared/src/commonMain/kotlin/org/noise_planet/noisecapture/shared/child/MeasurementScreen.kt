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
import androidx.compose.ui.unit.IntSize
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.launch
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.signal.SpectrumChannel
import org.noise_planet.noisecapture.shared.signal.WindowAnalysis
import org.noise_planet.noisecapture.shared.signal.get44100HZ
import org.noise_planet.noisecapture.shared.signal.get48000HZ
import org.noise_planet.noisecapture.shared.ui.SpectrogramBitmap
import org.noise_planet.noisecapture.toImageBitmap
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime


const val FFT_SIZE = 4096
const val FFT_HOP = 2048
const val WINDOW_TIME = 0.125

class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val audioSource: AudioSource) : Node(buildContext) {
    private val spectrumChannel: SpectrumChannel = SpectrumChannel()
    private var rangedB = 40.0
    private var mindB = 0.0
    private var spectrogramBitmapData = SpectrogramBitmap.SpectrogramDataModel(IntSize(1, 1), ByteArray(Int.SIZE_BYTES))

    @Composable
    override fun View(modifier: Modifier) {
        var noiseLevel by remember { mutableStateOf(0.0) }
        var spectrumBitmapState by remember { mutableStateOf(ByteArray(1)) }

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
            val windowAnalysis = WindowAnalysis(audioSource.getSampleRate(), FFT_SIZE, FFT_HOP)
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
                            noiseLevel = 10 * log10(windowData.fold(0.0)
                            { acc, fl -> acc + fl*fl } / windowData.size)
                            //noiseLevel = spectrumChannel.processSamplesWeightA(windowData)
                            //thirdOctave = spectrumChannel.processSamples(windowData)
                            windowAnalysis.pushSamples(samples.epoch, windowData).forEach {
                                if(spectrogramBitmapData.size.height > 1) {
                                    spectrogramBitmapData.pushSpectrumToSpectrogramData(it,
                                        SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG,
                                        mindB, rangedB, audioSource.getSampleRate().toDouble())
                                    spectrumBitmapState = spectrogramBitmapData.byteArray.copyOf()
                                }
                            }
                        }
                        println("Processed $windowTime of audio in $processingTime (${spectrogramBitmapData.size})")
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
                        spectrogramBitmapData = SpectrogramBitmap.createSpectrogram(canvasSize)
                    } else {
                        if(spectrumBitmapState.size > 1) {
                            drawImage(spectrumBitmapState.toImageBitmap())
                        }
                    }
                }
            }
        }
    }

}

