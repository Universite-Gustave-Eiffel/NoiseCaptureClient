package org.noise_planet.noisecapture.shared.child

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.launch
import org.koin.core.logger.Logger
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.MeasurementService
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.signal.WindowAnalysis
import org.noise_planet.noisecapture.shared.ui.SpectrogramBitmap
import org.noise_planet.noisecapture.toImageBitmap
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

const val FFT_SIZE = 4096
const val FFT_HOP = 2048
const val WINDOW_TIME = 0.125
const val SPECTROGRAM_STRIP_WIDTH = 32
const val REFERENCE_LEGEND_TEXT = " +99s "
const val DEFAULT_SAMPLE_RATE = 48000.0

class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val audioSource: AudioSource, private val logger: Logger) : Node(buildContext) {
    private var rangedB = 40.0
    private var mindB = 0.0
    private var dbGain = 105.0
    private var measurementService : MeasurementService? = null
    private var fftTool : WindowAnalysis? = null
    private val scaleMode = SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG

    @Composable
    fun spectrogram(spectrumCanvasState : SpectrogramViewModel, bitmapOffset : Int) {
        val textMeasurer = rememberTextMeasurer()
        val frequencyLegendPosition = when (spectrumCanvasState.currentStripData.scaleMode) {
            SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG -> SpectrogramBitmap.frequencyLegendPositionLog
            else -> SpectrogramBitmap.frequencyLegendPositionLinear
        }
        val timeXLabelMeasure = textMeasurer.measure(REFERENCE_LEGEND_TEXT)
        val timeXLabelHeight = timeXLabelMeasure.size.height
        val maxYLabelWidth =
            frequencyLegendPosition.maxOf { frequency ->
                val text = formatFrequency(frequency)
                textMeasurer.measure(text).size.width
            }
        Canvas(modifier = Modifier.fillMaxSize() ) {
            drawRect(color = SpectrogramBitmap.colorRamp[0], size=size)
            val tickLength = 4.dp.toPx()
            val legendHeight = timeXLabelHeight+tickLength
            val canvasSize = IntSize(SPECTROGRAM_STRIP_WIDTH, (size.height - legendHeight).toInt())
            val legendWidth = maxYLabelWidth+tickLength
            spectrumCanvasState.spectrogramCanvasSize = Size(size.width - legendWidth, size.height
                    - legendHeight)
            if(spectrumCanvasState.currentStripData.size != canvasSize) {
                // reset buffer on resize or first draw
                spectrumCanvasState.currentStripData = SpectrogramBitmap.createSpectrogram(
                    canvasSize, scaleMode, spectrumCanvasState.currentStripData.sampleRate)
                spectrumCanvasState.cachedStrips.clear()
            } else {
                if(spectrumCanvasState.currentStripData.sampleRate > 1) {
                    drawImage(
                        spectrumCanvasState.currentStripData.byteArray.toImageBitmap(),
                        topLeft = Offset(
                            size.width - bitmapOffset - legendWidth,
                            0F
                        )
                    )
                    spectrumCanvasState.cachedStrips.reversed()
                        .forEachIndexed { index, imageBitmap ->
                            val bitmapX =
                                size.width - legendWidth - ((index + 1) * SPECTROGRAM_STRIP_WIDTH
                                        + bitmapOffset).toFloat()
                            drawImage(
                                imageBitmap,
                                topLeft = Offset(bitmapX, 0F)
                            )
                        }
                }
            }
        }
    }
    companion object {
        data class LegendElement(val text : AnnotatedString, val textSize : IntSize, val xPos : Float, val textPos : Float)
    }
    private fun makeXLegend(textMeasurer: TextMeasurer, timeValue : Double, legendWidth : Float,
                            timePerPixel : Double) : LegendElement {
        val xPos = (legendWidth - timeValue / timePerPixel).toFloat()
        val legendText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.White)) {
                append("+${timeValue.toInt()}s")
            }
        }
        val textLayout = textMeasurer.measure(legendText)
        val textPos = min(legendWidth-textLayout.size.width,
            max(0F, xPos - textLayout.size.width / 2))
        return LegendElement(legendText, textLayout.size, xPos, textPos)
    }

    private fun recursiveLegendBuild(textMeasurer: TextMeasurer, timeValue : Double, legendWidth : Float,
                                     timePerPixel : Double, minX : Float, maxX : Float, timeValueLeft : Double,
                                     timeValueRight : Double, feedElements: ArrayList<LegendElement>) {
        val legendElement = makeXLegend(textMeasurer, timeValue, legendWidth, timePerPixel)
        if(legendElement.textPos > minX && legendElement.xPos + legendElement.textSize.width / 2 < maxX) {
            feedElements.add(legendElement)
            // left legend, + x seconds
            recursiveLegendBuild(textMeasurer, round(timeValue + (timeValueLeft - timeValue) / 2),
                legendWidth, timePerPixel, minX, legendElement.textPos, timeValueLeft, timeValue,
                feedElements)
            // right legend, - x seconds
            recursiveLegendBuild(textMeasurer, round(timeValue - (timeValue - timeValueRight) / 2),
                legendWidth, timePerPixel, legendElement.textPos + legendElement.textSize.width,
                maxX, timeValue, timeValueRight, feedElements)
        }
    }


    @Composable
    fun spectrogramLegend(scaleMode: SpectrogramBitmap.Companion.SCALE_MODE, sampleRate: Double) {
        val textMeasurer = rememberTextMeasurer()
        val frequencyLegendPosition = when (scaleMode) {
            SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG -> SpectrogramBitmap.frequencyLegendPositionLog
            else -> SpectrogramBitmap.frequencyLegendPositionLinear
        }
        val timeXLabelMeasure = textMeasurer.measure(REFERENCE_LEGEND_TEXT)
        val timeXLabelHeight = timeXLabelMeasure.size.height
        val maxYLabelWidth =
            frequencyLegendPosition.maxOf { frequency ->
                val text = formatFrequency(frequency)
                textMeasurer.measure(text).size.width
            }
        Canvas(modifier = Modifier.fillMaxSize() ) {
            val tickLength = 4.dp.toPx()
            val tickStroke = 2.dp
            val legendHeight = timeXLabelHeight+tickLength
            val legendWidth = maxYLabelWidth+tickLength
            if(sampleRate > 1) {
                // black background of legend
                drawRect(color = SpectrogramBitmap.colorRamp[0], size = Size(legendWidth, size.height),
                    topLeft = Offset(size.width - legendWidth, 0F))
                // draw Y axe labels
                val fMax = sampleRate / 2
                val fMin = frequencyLegendPosition[0].toDouble()
                val r = fMax / fMin
                val sheight = (size.height - legendHeight).toInt()
                frequencyLegendPosition.forEachIndexed { index, frequency ->
                    val text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.White)) {
                            append(formatFrequency(frequency))
                        }
                    }
                    val textSize = textMeasurer.measure(text)
                    val tickHeightPos = when (scaleMode) {
                        SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG -> {
                            sheight - (log10(frequency / fMin) / ((log10(r) / sheight))).toInt()
                        }
                        else -> 0
                    }
                    drawLine(
                        color = Color.White, start = Offset(
                            size.width - legendWidth,
                            tickHeightPos.toFloat() - tickStroke.toPx()/2
                        ),
                        end = Offset(
                            size.width - legendWidth + tickLength,
                            tickHeightPos.toFloat() - tickStroke.toPx()/2
                        ),
                        strokeWidth = tickStroke.toPx()
                    )
                    val textPos = min((size.height - textSize.size.height).toInt(),
                        max(0, tickHeightPos - textSize.size.height / 2))
                    drawText(textMeasurer, text, topLeft = Offset(size.width - legendWidth + tickLength, textPos.toFloat()))
                }
                // draw X axe labels
                val xLegendWidth = (size.width - legendWidth)
                // One pixel per time step
                val timePerPixel = FFT_HOP / sampleRate
                val lastTime = xLegendWidth * timePerPixel
                val legendElements = ArrayList<LegendElement>()
                val rightLegend = makeXLegend(textMeasurer, 0.0, xLegendWidth, timePerPixel)
                val leftLegend =  makeXLegend(textMeasurer, lastTime, xLegendWidth, timePerPixel)
                legendElements.add(leftLegend)
                legendElements.add(rightLegend)
                recursiveLegendBuild(textMeasurer, lastTime / 2, xLegendWidth, timePerPixel,
                    leftLegend.textSize.width.toFloat(),
                    rightLegend.xPos-rightLegend.textSize.width, lastTime, 0.0, legendElements)
                legendElements.forEach {legendElement ->
                    val tickPos = max(tickStroke.toPx() / 2F, min(xLegendWidth-tickStroke.toPx(), legendElement.xPos - tickStroke.toPx() / 2F))
                    drawLine(
                        color = Color.White, start = Offset(
                            tickPos,
                            sheight.toFloat()
                        ),
                        end = Offset(
                            tickPos,
                            sheight + tickLength
                        ),
                        strokeWidth = tickStroke.toPx()
                    )
                    drawText(textMeasurer,legendElement.text, topLeft = Offset(legendElement.textPos, sheight.toFloat() + tickLength))
                }
            }
        }
    }


    fun formatFrequency(frequency: Int): String {
        return if (frequency >= 1000) {
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
        var bitmapOffset by remember { mutableStateOf(0) }
        var noiseLevel by remember { mutableStateOf(0.0) }
        var sampleRate by remember { mutableStateOf( DEFAULT_SAMPLE_RATE ) }
        val spectrumCanvasState by remember { mutableStateOf(
            SpectrogramViewModel(SpectrogramBitmap.SpectrogramDataModel(IntSize(1, 1),
                ByteArray(Int.SIZE_BYTES),0 ,SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG, 1.0), ArrayList(), Size.Zero)) }

        lifecycleScope.launch {
            println("Launch lifecycle")
            audioSource.setup().collect {samples ->
                if(measurementService == null) {
                    measurementService = MeasurementService(samples.sampleRate, dbGain)
                    fftTool = WindowAnalysis(samples.sampleRate, FFT_SIZE, FFT_HOP)
                    sampleRate = samples.sampleRate.toDouble()
                }
                measurementService!!.processSamples(samples).forEach {
                        measurementServiceData->
                    noiseLevel = measurementServiceData.laeq
                }
                val spectrumDataList = fftTool!!.pushSamples(samples.epoch, samples.samples).toList()
                if(spectrumCanvasState.currentStripData.size.width > 1) {
                    var indexToProcess = 0
                    while(indexToProcess < spectrumDataList.size) {
                        val subListSizeToCompleteStrip = min(
                            spectrumCanvasState.currentStripData.size.width -
                                    spectrumCanvasState.currentStripData.offset,
                            spectrumDataList.size - indexToProcess
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
                            continue
                        }
                        spectrumCanvasState.currentStripData.pushSpectrumToSpectrogramData(
                            spectrumDataList.subList(indexToProcess,
                                indexToProcess + subListSizeToCompleteStrip),
                            mindB, rangedB, dbGain)
                        bitmapOffset = spectrumCanvasState.currentStripData.offset
                        indexToProcess += subListSizeToCompleteStrip
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
                Box(Modifier.fillMaxSize()) {
                    spectrogram(spectrumCanvasState, bitmapOffset)
                    spectrogramLegend(scaleMode, sampleRate)
                }
            }
        }
    }
}

data class SpectrogramViewModel(var currentStripData : SpectrogramBitmap.SpectrogramDataModel,
                                val cachedStrips : ArrayList<ImageBitmap>,
                                var spectrogramCanvasSize : Size)