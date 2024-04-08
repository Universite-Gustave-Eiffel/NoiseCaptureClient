package org.noise_planet.noisecapture.shared.child

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.bumble.appyx.navigation.lifecycle.DefaultPlatformLifecycleObserver
import com.bumble.appyx.navigation.lifecycle.Lifecycle
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.koin.core.logger.Logger
import org.noise_planet.noisecapture.shared.FFT_HOP
import org.noise_planet.noisecapture.shared.MeasurementService
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.signal.SpectrumData
import org.noise_planet.noisecapture.shared.ui.SpectrogramBitmap
import org.noise_planet.noisecapture.toImageBitmap
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

const val SPECTROGRAM_STRIP_WIDTH = 32
const val REFERENCE_LEGEND_TEXT = " +99s "
const val DEFAULT_SAMPLE_RATE = 48000.0
const val MAXIMUM_CACHED_SPECTRUM = 128

class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val measurementService: MeasurementService, private val logger: Logger) : Node(buildContext), DefaultPlatformLifecycleObserver {
    private var rangedB = 40.0
    private var mindB = 0.0
    private var dbGain = 105.0
    private val scaleMode = SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG

    @Composable
    fun spectrogram(spectrumCanvasState : SpectrogramViewModel, bitmapOffset : Int) {
        val textMeasurer = rememberTextMeasurer()
        val timeXLabelMeasure = textMeasurer.measure(REFERENCE_LEGEND_TEXT)
        val timeXLabelHeight = timeXLabelMeasure.size.height
        val text = formatFrequency(20000)
        val maxYLabelWidth = textMeasurer.measure(text).size.width
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

    private fun makeXLegend(textMeasurer: TextMeasurer, timeValue : Double, legendWidth : Float,
                            timePerPixel : Double, depth: Int) : LegendElement {
        val xPos = (legendWidth - timeValue / timePerPixel).toFloat()
        val legendText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.White)) {
                append("+${timeValue.toInt()}s")
            }
        }
        val textLayout = textMeasurer.measure(legendText)
        val textPos = min(legendWidth-textLayout.size.width,
            max(0F, xPos - textLayout.size.width / 2))
        return LegendElement(legendText, textLayout.size, xPos, textPos, depth)
    }

    private fun recursiveLegendBuild(textMeasurer: TextMeasurer, timeValue : Double, legendWidth : Float,
                                     timePerPixel : Double, minX : Float, maxX : Float, timeValueLeft : Double,
                                     timeValueRight : Double, feedElements: ArrayList<LegendElement>, depth: Int) {
        val legendElement = makeXLegend(textMeasurer, timeValue, legendWidth, timePerPixel, depth)
        if(legendElement.textPos > minX && legendElement.xPos + legendElement.textSize.width / 2 < maxX) {
            feedElements.add(legendElement)
            // left legend, + x seconds
            recursiveLegendBuild(textMeasurer, round(timeValue + (timeValueLeft - timeValue) / 2),
                legendWidth, timePerPixel, minX, legendElement.textPos, timeValueLeft, timeValue,
                feedElements, depth + 1)
            // right legend, - x seconds
            recursiveLegendBuild(textMeasurer, round(timeValue - (timeValue - timeValueRight) / 2),
                legendWidth, timePerPixel, legendElement.textPos + legendElement.textSize.width,
                maxX, timeValue, timeValueRight, feedElements, depth + 1)
        }
    }


    @Composable
    fun spectrogramLegend(scaleMode: SpectrogramBitmap.Companion.SCALE_MODE, sampleRate: Double) {
        val textMeasurer = rememberTextMeasurer()
        var frequencyLegendPosition = when (scaleMode) {
            SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG -> SpectrogramBitmap.frequencyLegendPositionLog
            else -> SpectrogramBitmap.frequencyLegendPositionLinear
        }
        frequencyLegendPosition = frequencyLegendPosition.filter { f -> f < sampleRate / 2 }.toIntArray()
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
                            sheight - (log10(frequency / fMin) / ((log10(fMax / fMin) / sheight))).toInt()
                        }
                        else -> (sheight - frequency / fMax * sheight).toInt()
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
                val rightLegend = makeXLegend(textMeasurer, 0.0, xLegendWidth, timePerPixel, 0)
                val leftLegend =  makeXLegend(textMeasurer, lastTime, xLegendWidth, timePerPixel, 0)
                legendElements.add(leftLegend)
                legendElements.add(rightLegend)
                recursiveLegendBuild(textMeasurer, lastTime / 2, xLegendWidth, timePerPixel,
                    leftLegend.textSize.width.toFloat(),
                    rightLegend.xPos-rightLegend.textSize.width, lastTime, 0.0, legendElements, 0)
                // find depth index with maximum number of elements (to generate same intervals on legend)
                val legendDepthCount = IntArray(legendElements.maxOf { it.depth }+1) { 0 }
                legendElements.forEach {
                    legendDepthCount[it.depth] += 1
                }
                val maxElementsDepth = legendDepthCount.foldIndexed(Pair(-1,0)) { index, acc, i ->
                    if(acc.second < i) Pair(index, i) else acc
                }
                legendElements.removeAll {
                    it.depth > maxElementsDepth.first
                }
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


    override fun onStop() {
        println("Appyx onStop")
    }

    override fun onPause() {
        println("Appyx onPause")
    }

    override fun onResume() {
        println("Appyx onResume")
    }

    override fun onStart() {
        println("Appyx onStart")
    }

    fun processSpectrum(spectrumCanvasState: SpectrogramViewModel, it : SpectrumData) : Int {
        spectrumCanvasState.currentStripData.pushSpectrumToSpectrogramData(
            it, mindB, rangedB,
            dbGain
        )
        if (spectrumCanvasState.currentStripData.offset == SPECTROGRAM_STRIP_WIDTH) {
            // spectrogram band complete, store bitmap
            spectrumCanvasState.cachedStrips.add(
                spectrumCanvasState.currentStripData.byteArray.toImageBitmap()
            )
            if ((spectrumCanvasState.cachedStrips.size - 1) *
                SPECTROGRAM_STRIP_WIDTH >
                spectrumCanvasState.spectrogramCanvasSize.width
            ) {
                // remove offscreen bitmaps
                spectrumCanvasState.cachedStrips.removeAt(0)
            }
            spectrumCanvasState.currentStripData =
                SpectrogramBitmap.createSpectrogram(
                    spectrumCanvasState.currentStripData.size,
                    spectrumCanvasState.currentStripData.scaleMode,
                    it.sampleRate.toDouble()
                )
        }
        return spectrumCanvasState.currentStripData.offset
    }

    @Composable
    override fun View(modifier: Modifier) {
        var bitmapOffset by remember { mutableStateOf(0) }
        var noiseLevel by remember { mutableStateOf(0.0) }
        var sampleRate by remember { mutableStateOf( DEFAULT_SAMPLE_RATE ) }
        val composableScope = rememberCoroutineScope()
        val spectrumCanvasState by remember { mutableStateOf(
            SpectrogramViewModel(SpectrogramBitmap.SpectrogramDataModel(IntSize(1, 1),
                ByteArray(Int.SIZE_BYTES),0 ,SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG, 1.0), ArrayList(), Size.Zero)) }
        composableScope.launch {
            measurementService.collectAudioIndicators().collect {
                noiseLevel = it.laeq
            }
        }
        composableScope.launch {
            println("Launch spectrum lifecycle")
            val unprocessedSpectrum = ArrayDeque<SpectrumData>()
            measurementService.collectSpectrumData().collect() {
                if (spectrumCanvasState.currentStripData.size.width > 1) {
                    while (!unprocessedSpectrum.isEmpty()) {
                        bitmapOffset = processSpectrum(spectrumCanvasState,
                            unprocessedSpectrum.removeFirst())
                    }
                    bitmapOffset = processSpectrum(spectrumCanvasState, it)
                } else {
                    unprocessedSpectrum.add(it)
                    if(unprocessedSpectrum.size > MAXIMUM_CACHED_SPECTRUM) {
                        unprocessedSpectrum.removeFirst()
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text("${round(noiseLevel * 100)/100} dB(A)")
                var state by remember { mutableStateOf(MeasurementTabState.SPECTRUM) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TabRow(selectedTabIndex = state.ordinal) {
                        MeasurementTabState.entries.forEach { entry ->
                            Tab(
                                text = { Text(MEASUREMENT_TAB_LABEL[entry.ordinal]) },
                                selected = state == entry,
                                onClick = { state = entry }
                            )
                        }
                    }
                    AnimatedContent(targetState = state, transitionSpec = {
                        val direction = if(targetState.ordinal > initialState.ordinal) AnimatedContentTransitionScope.SlideDirection.Left else AnimatedContentTransitionScope.SlideDirection.Right
                        val delay = 400
                        slideIntoContainer(animationSpec = tween(delay),
                            towards = direction) togetherWith
                                slideOutOfContainer(animationSpec = tween(delay),
                                    towards = direction) }) {
                        when (it) {
                            MeasurementTabState.SPECTROGRAM -> Box(Modifier.fillMaxSize()) {
                                spectrogram(spectrumCanvasState, bitmapOffset)
                                spectrogramLegend(scaleMode, sampleRate)
                            }

                            else -> Box(Modifier.fillMaxSize()) {Text(
                                text = "Text tab ${MEASUREMENT_TAB_LABEL[it.ordinal]} selected",
                                style = MaterialTheme.typography.body1
                            )}
                        }
                    }
                }
            }
        }
    }
}

data class SpectrogramViewModel(var currentStripData : SpectrogramBitmap.SpectrogramDataModel,
                                val cachedStrips : ArrayList<ImageBitmap>,
                                var spectrogramCanvasSize : Size)

data class LegendElement(val text : AnnotatedString, val textSize : IntSize, val xPos : Float,
                         val textPos : Float, val depth : Int)


fun Lifecycle.asPlatformFlow(observer : DefaultPlatformLifecycleObserver): Flow<Lifecycle.State> =
    callbackFlow {
        addObserver(observer)
        awaitClose { removeObserver(observer) }
    }

enum class MeasurementTabState { SPECTRUM, SPECTROGRAM, MAP}
val MEASUREMENT_TAB_LABEL = listOf("Spectrum", "Spectrogram", "Map")