package org.noise_planet.noisecapture.shared.child

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.bumble.appyx.components.backstack.BackStack
import com.bumble.appyx.navigation.lifecycle.DefaultPlatformLifecycleObserver
import com.bumble.appyx.navigation.lifecycle.Lifecycle
import com.bumble.appyx.navigation.modality.BuildContext
import com.bumble.appyx.navigation.node.Node
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.logger.Logger
import org.noise_planet.noisecapture.shared.FFT_HOP
import org.noise_planet.noisecapture.shared.MeasurementService
import org.noise_planet.noisecapture.shared.ScreenData
import org.noise_planet.noisecapture.shared.WINDOW_TIME
import org.noise_planet.noisecapture.shared.signal.FAST_DECAY_RATE
import org.noise_planet.noisecapture.shared.signal.LevelDisplayWeightedDecay
import org.noise_planet.noisecapture.shared.signal.SpectrumData
import org.noise_planet.noisecapture.shared.ui.SpectrogramBitmap
import org.noise_planet.noisecapture.shared.ui.SpectrogramBitmap.Companion.toComposeColor
import org.noise_planet.noisecapture.shared.ui.asEventFlow
import org.noise_planet.noisecapture.toImageBitmap
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

const val SPECTROGRAM_STRIP_WIDTH = 32
const val REFERENCE_LEGEND_TEXT = " +99s "
const val DEFAULT_SAMPLE_RATE = 48000.0
const val MAXIMUM_CACHED_SPECTRUM = 1280
const val MIN_SHOWN_DBA_VALUE = 5.0
const val MAX_SHOWN_DBA_VALUE = 140.0
val NOISE_LEVEL_FONT_SIZE = TextUnit(50F, TextUnitType.Sp)

class MeasurementScreen(buildContext: BuildContext, val backStack: BackStack<ScreenData>,
                        private val measurementService: MeasurementService, private val logger: Logger) : Node(buildContext), DefaultPlatformLifecycleObserver {
    private var rangedB = 40.0
    private var mindB = 0.0
    private var dbGain = 105.0

    val noiseColorRamp = arrayOf(
        "#FF0000".toComposeColor(),
        "#FF8000".toComposeColor(),
        "#FFFF00".toComposeColor(),
        "#99FF00".toComposeColor(),
        "#00FF00".toComposeColor()
    )

    fun getColorIndex(noiseLevel: Double) = when {
        noiseLevel > 75.0 -> 0
        noiseLevel > 65 -> 1
        noiseLevel > 55 -> 2
        noiseLevel > 45 -> 3
        else -> 4
    }


    private val scaleMode = SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG
    val spectrumCanvasState  = SpectrogramViewModel(SpectrogramBitmap.SpectrogramDataModel(IntSize(1, 1),
        ByteArray(Int.SIZE_BYTES),0 ,SpectrogramBitmap.Companion.SCALE_MODE.SCALE_LOG, 1.0), ArrayList(), Size.Zero)

    var preparedLegendBitmap = LegendBitmap( ImageBitmap(1, 1), Size(0F, 0F), Size(0F, 0F), Size(0F, 0F) )

    @Composable
    fun spectrogram(spectrumCanvasState : SpectrogramViewModel, bitmapOffset : Int) {
        Canvas(modifier = Modifier.fillMaxSize() ) {
            val canvasSize = IntSize(SPECTROGRAM_STRIP_WIDTH, (size.height - preparedLegendBitmap.bottomLegendSize.height).toInt())
            drawRect(color = SpectrogramBitmap.colorRamp[0], size=Size(size.width - preparedLegendBitmap.rightLegendSize.width, canvasSize.height.toFloat()))
            spectrumCanvasState.spectrogramCanvasSize = Size(size.width - preparedLegendBitmap.rightLegendSize.width, size.height
                    - preparedLegendBitmap.bottomLegendSize.height)
            if(spectrumCanvasState.currentStripData.size.height != canvasSize.height) {
                // reset buffer on resize or first draw
                spectrumCanvasState.currentStripData = SpectrogramBitmap.createSpectrogram(
                    canvasSize, scaleMode, spectrumCanvasState.currentStripData.sampleRate)
                spectrumCanvasState.cachedStrips.clear()
            } else {
                if(spectrumCanvasState.currentStripData.sampleRate > 1) {
                    drawImage(
                        spectrumCanvasState.currentStripData.byteArray.toImageBitmap(),
                        topLeft = Offset(
                            size.width - bitmapOffset - preparedLegendBitmap.rightLegendSize.width,
                            0F
                        )
                    )
                    spectrumCanvasState.cachedStrips.reversed()
                        .forEachIndexed { index, imageBitmap ->
                            val bitmapX =
                                size.width - preparedLegendBitmap.rightLegendSize.width - ((index + 1) * SPECTROGRAM_STRIP_WIDTH
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
            withStyle(style = SpanStyle()) {
                append("+${round(timeValue).toInt()}s")
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
            recursiveLegendBuild(textMeasurer, timeValue + (timeValueLeft - timeValue) / 2,
                legendWidth, timePerPixel, minX, legendElement.textPos, timeValueLeft, timeValue,
                feedElements, depth + 1)
            // right legend, - x seconds
            recursiveLegendBuild(textMeasurer, timeValue - (timeValue - timeValueRight) / 2,
                legendWidth, timePerPixel, legendElement.textPos + legendElement.textSize.width,
                maxX, timeValue, timeValueRight, feedElements, depth + 1)
        }
    }

    fun buildLegendBitmap(size: Size, density: Density, scaleMode: SpectrogramBitmap.Companion.SCALE_MODE,
                               sampleRate: Double, textMeasurer: TextMeasurer, colors: Colors): LegendBitmap {
        val drawScope = CanvasDrawScope()
        val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
        val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

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
        var bottomLegendSize = Size(0F, 0F)
        var rightLegendSize = Size(0F, 0F)
        drawScope.draw(
            density = density,
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = size,
        ) {

            val tickLength = 4.dp.toPx()
            val tickStroke = 2.dp
            val legendHeight = timeXLabelHeight+tickLength
            val legendWidth = maxYLabelWidth+tickLength
            bottomLegendSize = Size(size.width-legendWidth, legendHeight)
            rightLegendSize = Size(legendWidth, size.height - legendHeight)
            if(sampleRate > 1) {
                // draw Y axe labels
                val fMax = sampleRate / 2
                val fMin = frequencyLegendPosition[0].toDouble()
                val sheight = (size.height - legendHeight).toInt()
                frequencyLegendPosition.forEachIndexed { index, frequency ->
                    val text = buildAnnotatedString {
                        withStyle(style = SpanStyle()) {
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
                        color = colors.onPrimary, start = Offset(
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
                val rightLegend = makeXLegend(textMeasurer, 0.0, xLegendWidth, timePerPixel, -1)
                val leftLegend =  makeXLegend(textMeasurer, lastTime, xLegendWidth, timePerPixel, -1)
                legendElements.add(leftLegend)
                legendElements.add(rightLegend)
                recursiveLegendBuild(textMeasurer, lastTime / 2, xLegendWidth, timePerPixel,
                    leftLegend.textSize.width.toFloat(),
                    rightLegend.xPos-rightLegend.textSize.width, lastTime, 0.0, legendElements, 0)
                // find depth index with maximum number of elements (to generate same intervals on legend)
                val legendDepthCount = IntArray(legendElements.maxOf { it.depth }+1) { 0 }
                legendElements.forEach {
                    if(it.depth >= 0) {
                        legendDepthCount[it.depth] += 1
                    }
                }
                legendElements.removeAll {
                    it.depth > 0 && legendDepthCount[it.depth] != (2.0.pow(it.depth)).toInt()
                }
                legendElements.forEach {legendElement ->
                    val tickPos = max(tickStroke.toPx() / 2F, min(xLegendWidth-tickStroke.toPx(), legendElement.xPos - tickStroke.toPx() / 2F))
                    drawLine(
                        color = colors.onPrimary, start = Offset(
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
        return LegendBitmap(bitmap, size, bottomLegendSize, rightLegendSize)
    }


    @Composable
    fun spectrogramLegend(scaleMode: SpectrogramBitmap.Companion.SCALE_MODE, sampleRate: Double) {
        val colors = MaterialTheme.colors
        val textMeasurer = rememberTextMeasurer()
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (preparedLegendBitmap.imageSize != size) {
                preparedLegendBitmap = buildLegendBitmap(
                    size, Density(density), scaleMode,
                    sampleRate, textMeasurer, colors
                )
            }
            drawImage(preparedLegendBitmap.imageBitmap)
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

    fun buildNoiseLevelText(noiseLevel : Double) : AnnotatedString = buildAnnotatedString {
        if(noiseLevel > MIN_SHOWN_DBA_VALUE && noiseLevel < MAX_SHOWN_DBA_VALUE) {
            withStyle(style = SpanStyle(color = noiseColorRamp[getColorIndex(noiseLevel)], fontSize = NOISE_LEVEL_FONT_SIZE)) {
                append("${round(noiseLevel * 10) / 10}")
            }
        } else {
            withStyle(style = SpanStyle()) {
                append("-")
            }
        }
    }

    @Composable
    fun vueMeter(modifier: Modifier, settings: VueMeterSettings, value : Double) {
        val color = MaterialTheme.colors
        val textMeasurer = rememberTextMeasurer()
        Box(modifier = modifier) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // x axis labels
                var maxHeight = 0
                settings.xLabels.forEach { value ->
                    val textLayoutResult = textMeasurer.measure(buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontSize = TextUnit(
                                    10F,
                                    TextUnitType.Sp
                                )
                            )
                        )
                        { append("$value") }
                    })
                    maxHeight = max(textLayoutResult.size.height, maxHeight)
                    val labelRatio =
                        (value - settings.minimum) / (settings.maximum - settings.minimum)
                    val xPosition = min(
                        size.width - textLayoutResult.size.width,
                        max(
                            0F,
                            (size.width * labelRatio - textLayoutResult.size.width / 2).toFloat()
                        )
                    )
                    drawText(textLayoutResult, topLeft = Offset(xPosition, 0F))
                }
                val barHeight = size.height - maxHeight
                drawRoundRect(
                    color = color.background,
                    topLeft = Offset(0F, maxHeight.toFloat()),
                    cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
                    size = Size(size.width, barHeight)
                )
                val valueRatio = (value-settings.minimum)/(settings.maximum-settings.minimum)
                drawRoundRect(
                    color = noiseColorRamp[getColorIndex(value)],
                    topLeft = Offset(0F, maxHeight.toFloat()),
                    cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
                    size = Size((size.width * valueRatio).toFloat(), barHeight)
                )
            }
        }
    }




    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun View(modifier: Modifier) {
        var bitmapOffset by remember { mutableStateOf(0) }
        var noiseLevel by remember { mutableStateOf(0.0) }
        var sampleRate by remember { mutableStateOf( DEFAULT_SAMPLE_RATE ) }
        var indicatorCollectJob : Job? = null
        var spectrumCollectJob : Job? = null
        val launchMeasurementJob = fun () {
            indicatorCollectJob = lifecycleScope.launch {
                val levelDisplay = LevelDisplayWeightedDecay(FAST_DECAY_RATE, WINDOW_TIME)
                measurementService.collectAudioIndicators().collect {
                    noiseLevel = levelDisplay.getWeightedValue(it.laeq)
                }
            }
            spectrumCollectJob = lifecycleScope.launch {
                println("Launch spectrum lifecycle")
                val unprocessedSpectrum = ArrayDeque<SpectrumData>()
                measurementService.collectSpectrumData().collect() {
                    sampleRate = it.sampleRate.toDouble()
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
        }
        launchMeasurementJob()
        lifecycleScope.launch {
            lifecycle.asEventFlow().collect { event ->
                if(event == Lifecycle.Event.ON_PAUSE) {
                    indicatorCollectJob?.cancel()
                    spectrumCollectJob?.cancel()
                } else if(event == Lifecycle.Event.ON_RESUME &&
                    (indicatorCollectJob == null || indicatorCollectJob?.isActive == false)) {
                    launchMeasurementJob()
                }
            }
        }
        val animationScope = rememberCoroutineScope()

        val rightRoundedSquareShape: Shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 40.dp,
            bottomStart = 0.dp,
            bottomEnd = 40.dp
        )
        val vueMeterSettings = VueMeterSettings(20.0,120.0,
            IntArray(6){v->((v+ 1)*20.0).toInt()})
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.primary
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                            Surface(
                                Modifier.padding(top = 20.dp, bottom = 10.dp).weight(1F),
                                color = MaterialTheme.colors.background,
                                shape = rightRoundedSquareShape,
                                elevation = 10.dp
                            ) {
                                Box() {
                                    Text(
                                        buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    fontSize = TextUnit(
                                                        18F,
                                                        TextUnitType.Sp
                                                    )
                                                )
                                            )
                                            { append("dB(A)") }
                                        },
                                        modifier = Modifier.align(Alignment.BottomStart)
                                            .padding(start = 10.dp)
                                    )
                                    Text(buildNoiseLevelText(noiseLevel), modifier = Modifier.padding(end = 20.dp).align(Alignment.CenterEnd))
                                }
                            }
                        Row(
                            Modifier.align(Alignment.CenterVertically),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(MeasurementStatistics("Min", "-"),
                                MeasurementStatistics("Avg", "-"),
                                MeasurementStatistics("Max", "-")).forEach {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
                                    Text(it.label)
                                    Text(it.value)
                                }
                            }
                        }
                    }

                vueMeter(Modifier.fillMaxWidth().height(50.dp).padding(start = 30.dp, end = 30.dp),
                    vueMeterSettings,
                    noiseLevel
                )
                val pagerState = rememberPagerState(pageCount = { MeasurementTabState.entries.size })

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TabRow(selectedTabIndex = pagerState.currentPage) {
                        MeasurementTabState.entries.forEach { entry ->
                            Tab(
                                text = { Text(MEASUREMENT_TAB_LABEL[entry.ordinal]) },
                                selected = pagerState.currentPage == entry.ordinal,
                                onClick = { animationScope.launch {pagerState.animateScrollToPage(entry.ordinal)} }
                            )
                        }
                    }
                    HorizontalPager(state = pagerState) {page->
                        when (MeasurementTabState.entries[page]) {
                            MeasurementTabState.SPECTROGRAM -> Box(Modifier.fillMaxSize()) {
                                spectrogram(spectrumCanvasState, bitmapOffset)
                                spectrogramLegend(scaleMode, sampleRate)
                            }

                            else -> Surface(Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {Text(
                                text = "Text tab ${MEASUREMENT_TAB_LABEL[page]} selected",
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

enum class MeasurementTabState { SPECTRUM, SPECTROGRAM, MAP}
val MEASUREMENT_TAB_LABEL = listOf("Spectrum", "Spectrogram", "Map")
data class LegendBitmap(val imageBitmap: ImageBitmap, val imageSize: Size, val bottomLegendSize: Size, val rightLegendSize: Size)

data class MeasurementStatistics(val label : String, val value : String)

data class VueMeterSettings(val minimum : Double, val maximum : Double, val xLabels : IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as VueMeterSettings

        if (minimum != other.minimum) return false
        if (maximum != other.maximum) return false
        if (!xLabels.contentEquals(other.xLabels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = minimum.hashCode()
        result = 31 * result + maximum.hashCode()
        result = 31 * result + xLabels.contentHashCode()
        return result
    }
}