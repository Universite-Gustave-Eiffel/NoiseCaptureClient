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
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.logger.Logger
import org.noise_planet.noisecapture.AudioSamples
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.MeasurementService
import org.noise_planet.noisecapture.shared.MeasurementServiceData
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.ui.SpectrogramBitmap
import org.noise_planet.noisecapture.toImageBitmap
import kotlin.math.round

const val FFT_SIZE = 4096
const val FFT_HOP = 2048
const val WINDOW_TIME = 0.125

class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val audioSource: AudioSource, private val logger: Logger) : Node(buildContext) {
    private var rangedB = 40.0
    private var mindB = 0.0
    private var measurementService : MeasurementService? = null
    private var spectrogramBitmapData = SpectrogramBitmap.SpectrogramDataModel(IntSize(1, 1), ByteArray(Int.SIZE_BYTES))

    @Composable
    override fun View(modifier: Modifier) {
        var noiseLevel by remember { mutableStateOf(0.0) }
        var spectrumBitmapState by remember { mutableStateOf(ByteArray(1)) }

        lifecycleScope.launch {
            println("Launch lifecycle")
            audioSource.setup().collect {samples ->
                if(measurementService == null) {
                    measurementService = MeasurementService(samples.sampleRate)
                }
                measurementService!!.processSamples(samples).forEach {
                        measurementServiceData->
                    noiseLevel = measurementServiceData.laeq
                    if(spectrogramBitmapData.size.width > 1) {
                        spectrogramBitmapData.pushSpectrumToSpectrogramData(
                            measurementServiceData.spectrumDataList,
                            SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG,
                            mindB, rangedB, measurementService!!.sampleRate.toDouble())
                        spectrumBitmapState = spectrogramBitmapData.byteArray.copyOf()
                    }
                }
            }
        }.invokeOnCompletion {
            println("Release audio")
            audioSource.release()
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

