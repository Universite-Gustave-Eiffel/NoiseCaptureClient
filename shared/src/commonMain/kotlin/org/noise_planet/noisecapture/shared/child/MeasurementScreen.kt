package org.noise_planet.noisecapture.shared.child

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.navigation.lifecycle.DefaultPlatformLifecycleObserver
import com.bumble.appyx.navigation.lifecycle.Lifecycle
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.launch
import org.koin.core.logger.Logger
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.MeasurementService
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.ui.SpectrogramBitmap
import org.noise_planet.noisecapture.toImageBitmap
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
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
        val textMeasurer = rememberTextMeasurer()
        val frequencyLegendPosition = when (spectrumCanvasState.currentStripData.scaleMode) {
            SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG -> SpectrogramBitmap.frequencyLegendPositionLog
            else -> SpectrogramBitmap.frequencyLegendPositionLinear
        }
        val maxYLabelWidth =
            frequencyLegendPosition.maxOf { frequency ->
                val text = formatFrequency(frequency)
                textMeasurer.measure(text).size.width
            }
        Canvas(modifier = Modifier.fillMaxSize() ) {
            drawRect(color = Color.Black, size=size)
            val canvasSize = IntSize(SPECTROGRAM_STRIP_WIDTH, size.height.toInt())
            val tickLength = 4.dp.toPx()
            val legendWidth = maxYLabelWidth+tickLength
            spectrumCanvasState.spectrogramCanvasSize = Size(size.width - legendWidth, size.height)
            if(spectrumCanvasState.currentStripData.size != canvasSize) {
                // reset buffer on resize or first draw
                spectrumCanvasState.currentStripData = SpectrogramBitmap.createSpectrogram(
                    canvasSize, SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG, spectrumCanvasState.currentStripData.sampleRate)
                spectrumCanvasState.cachedStrips.clear()
            } else {
                if(spectrumCanvasState.currentStripData.sampleRate > 1) {
                    drawImage(
                        spectrumCanvasState.currentStripData.byteArray.toImageBitmap(),
                        topLeft = Offset(
                            size.width - spectrumCanvasState.currentStripData.offset,
                            0F
                        )
                    )
                    spectrumCanvasState.cachedStrips.reversed()
                        .forEachIndexed { index, imageBitmap ->
                            val bitmapX = size.width - ((index + 1) * SPECTROGRAM_STRIP_WIDTH
                                    + spectrumCanvasState.currentStripData.offset).toFloat()
                            drawImage(
                                imageBitmap,
                                topLeft = Offset(bitmapX, 0F)
                            )
                        }
                    drawRect(color = Color.Black, size = Size(legendWidth, size.height))
                    // draw legend
                    val fMax = spectrumCanvasState.currentStripData.sampleRate / 2
                    val fMin = frequencyLegendPosition[0].toDouble()
                    val r = fMax / fMin
                    val sheight = spectrumCanvasState.currentStripData.size.height
                    frequencyLegendPosition.forEachIndexed { index, frequency ->
                        val text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.White)) {
                                append(formatFrequency(frequency))
                            }
                        }
                        val textSize = textMeasurer.measure(text)
                        val tickHeightPos = when (spectrumCanvasState.currentStripData.scaleMode) {
                            SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG -> {
                                sheight - (log10(frequency / fMin) / ((log10(r) / sheight))).toInt()
                            }

                            else -> 0
                        }
                        drawLine(
                            color = Color.White, start = Offset(
                                legendWidth-tickLength,
                                tickHeightPos.toFloat()
                            ),
                            end = Offset(
                                legendWidth,
                                tickHeightPos.toFloat()
                            ),
                            strokeWidth = 2.dp.toPx()
                        )
                        val textPos = min(sheight - textSize.size.height,
                            max(0, tickHeightPos - textSize.size.height / 2))
                        drawText(textMeasurer, text, topLeft = Offset(legendWidth-textSize.size.width-tickLength, textPos.toFloat()))
                    }
                }
            }
        }
    }


    fun formatFrequency(frequency: Int): String {
        return if (frequency > 1000) {
            if(frequency%1000 > 0) {
                val subKilo = (frequency%1000).toString().trimEnd('0')
                "${frequency/1000}.$subKilo kHz"
            } else {
                "${frequency/1000} kHz"
            }
        } else {
            "$frequency Hz"
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        var noiseLevel by remember { mutableStateOf(0.0) }
        var spectrumCanvasState by remember { mutableStateOf(
            SpectrogramViewModel(SpectrogramBitmap.SpectrogramDataModel(IntSize(1, 1),
                ByteArray(Int.SIZE_BYTES),0 ,SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG, 1.0), ArrayList(), Size.Zero)) }

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
                                if((spectrumCanvasState.cachedStrips.size - 1) *
                                    SPECTROGRAM_STRIP_WIDTH >
                                    spectrumCanvasState.spectrogramCanvasSize.width) {
                                    // remove offscreen bitmaps
                                    spectrumCanvasState.cachedStrips.removeAt(0)
                                }
                                spectrumCanvasState.currentStripData =
                                    SpectrogramBitmap.createSpectrogram(
                                        spectrumCanvasState.currentStripData.size,
                                        spectrumCanvasState.currentStripData.scaleMode,
                                        measurementService!!.sampleRate.toDouble())
                                bitmapChanged = false
                                continue
                            }
                            spectrumCanvasState.currentStripData.pushSpectrumToSpectrogramData(
                                    measurementServiceData.spectrumDataList.subList(indexToProcess,
                                        indexToProcess + subListSizeToCompleteStrip),
                                    mindB, rangedB)
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