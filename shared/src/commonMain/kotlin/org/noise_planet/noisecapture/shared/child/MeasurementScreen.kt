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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.launch
import org.koin.core.logger.Logger
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.MeasurementService
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.ui.SpectrogramBitmap
import org.noise_planet.noisecapture.toImageBitmap
import kotlin.math.min
import kotlin.math.round

const val FFT_SIZE = 4096
const val FFT_HOP = 2048
const val WINDOW_TIME = 0.125
const val SPECTROGRAM_STRIP_WIDTH = 32

class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val audioSource: AudioSource, private val logger: Logger) : Node(buildContext) {
    private var rangedB = 40.0
    private var mindB = 0.0
    private var measurementService : MeasurementService? = null

    @Composable
    fun spectrogram(spectrumCanvasState : SpectrogramViewModel) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = IntSize(SPECTROGRAM_STRIP_WIDTH, size.height.toInt())
            spectrumCanvasState.spectrogramCanvasSize = size
            if(spectrumCanvasState.currentStripData.size != canvasSize) {
                // reset buffer on resize or first draw
                spectrumCanvasState.currentStripData = SpectrogramBitmap.createSpectrogram(canvasSize)
                spectrumCanvasState.cachedStrips.clear()
            } else {
                drawImage(spectrumCanvasState.currentStripData.byteArray.toImageBitmap(),
                    topLeft = Offset(size.width - spectrumCanvasState.currentStripData.offset, 0F))
                spectrumCanvasState.cachedStrips.reversed().forEachIndexed { index, imageBitmap ->
                    val bitmapX = size.width - ((index + 1) * SPECTROGRAM_STRIP_WIDTH
                            + spectrumCanvasState.currentStripData.offset).toFloat()
                    drawImage(imageBitmap,
                        topLeft = Offset(bitmapX, 0F))
                }
            }
        }
    }


    @Composable
    override fun View(modifier: Modifier) {
        var noiseLevel by remember { mutableStateOf(0.0) }
        var spectrumCanvasState by remember { mutableStateOf(
            SpectrogramViewModel(SpectrogramBitmap.SpectrogramDataModel(IntSize(1, 1),
                ByteArray(Int.SIZE_BYTES)), ArrayList(), Size.Zero)) }

        lifecycleScope.launch {
            println("Launch lifecycle")
            audioSource.setup().collect {samples ->
                if(measurementService == null) {
                    measurementService = MeasurementService(samples.sampleRate)
                }
                measurementService!!.processSamples(samples).forEach {
                        measurementServiceData->
                    noiseLevel = measurementServiceData.laeq
                    if(spectrumCanvasState.currentStripData.size.width > 1) {
                        var indexToProcess = 0
                        var bitmapChanged = false
                        while(indexToProcess < measurementServiceData.spectrumDataList.size) {
                            val subListSizeToCompleteStrip = min(
                                spectrumCanvasState.currentStripData.size.width -
                                        spectrumCanvasState.currentStripData.offset,
                                measurementServiceData.spectrumDataList.size - indexToProcess
                            )
                            if(subListSizeToCompleteStrip == 0) {
                                // spectrogram band complete, store bitmap
                                spectrumCanvasState.cachedStrips.add(
                                    spectrumCanvasState.currentStripData.byteArray.toImageBitmap())
                                if((spectrumCanvasState.cachedStrips.size - 1) * SPECTROGRAM_STRIP_WIDTH > spectrumCanvasState.spectrogramCanvasSize.width) {
                                    // remove offscreen bitmaps
                                    spectrumCanvasState.cachedStrips.removeAt(0)
                                }
                                spectrumCanvasState.currentStripData = SpectrogramBitmap.createSpectrogram(spectrumCanvasState.currentStripData.size)
                                bitmapChanged = false
                                continue
                            }
                            spectrumCanvasState.currentStripData.pushSpectrumToSpectrogramData(
                                    measurementServiceData.spectrumDataList.subList(indexToProcess,
                                        indexToProcess + subListSizeToCompleteStrip),
                                    SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG,
                                    mindB, rangedB, measurementService!!.sampleRate.toDouble()
                                )
                            bitmapChanged = true
                            indexToProcess += subListSizeToCompleteStrip
                        }
                        if(bitmapChanged) {
                            spectrumCanvasState = SpectrogramViewModel(
                                spectrumCanvasState.currentStripData,
                                spectrumCanvasState.cachedStrips,
                                spectrumCanvasState.spectrogramCanvasSize)
                        }
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
                spectrogram(spectrumCanvasState)
            }
        }
    }
}

data class SpectrogramViewModel(var currentStripData : SpectrogramBitmap.SpectrogramDataModel,
                                val cachedStrips : ArrayList<ImageBitmap>,
                                var spectrogramCanvasSize : Size)