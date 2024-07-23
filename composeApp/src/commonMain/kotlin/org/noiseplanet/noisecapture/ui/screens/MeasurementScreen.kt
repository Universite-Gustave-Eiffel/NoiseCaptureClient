//
//
// TODO: Split this file!!
//
//
@file:Suppress("TooManyFunctions")

package org.noiseplanet.noisecapture.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.eventFlow
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.audio.ANDROID_GAIN
import org.noiseplanet.noisecapture.audio.WINDOW_TIME
import org.noiseplanet.noisecapture.audio.signal.FAST_DECAY_RATE
import org.noiseplanet.noisecapture.audio.signal.LevelDisplayWeightedDecay
import org.noiseplanet.noisecapture.audio.signal.SpectrumData
import org.noiseplanet.noisecapture.measurements.FFT_HOP
import org.noiseplanet.noisecapture.measurements.MeasurementService
import org.noiseplanet.noisecapture.ui.components.SpectrogramBitmap
import org.noiseplanet.noisecapture.ui.components.SpectrogramBitmap.Companion.toComposeColor
import org.noiseplanet.noisecapture.util.toImageBitmap
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

const val SPECTROGRAM_STRIP_WIDTH = 32
const val REFERENCE_LEGEND_TEXT = " +99s "
const val DEFAULT_SAMPLE_RATE = 48000.0
const val MIN_SHOWN_DBA_VALUE = 5.0
const val MAX_SHOWN_DBA_VALUE = 140.0
const val MIN_SHOWN_DBA_VALUE_SPECTRUM = 5.0
const val MAX_SHOWN_DBA_VALUE_SPECTRUM = 90.0
val NOISE_LEVEL_FONT_SIZE = TextUnit(50F, TextUnitType.Sp)
val SPECTRUM_PLOT_SQUARE_WIDTH = 10.dp
val SPECTRUM_PLOT_SQUARE_OFFSET = 1.dp
const val MIN_FREQUENCY_SPECTRUM = 100.0
const val MAX_FREQUENCY_SPECTRUM = 16000.0


@Composable
fun MeasurementScreen(
    measurementService: MeasurementService = koinInject(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
//    logger: Logger = koinInject(),
) {
    val noiseColorRampSpl: List<Pair<Float, Color>> = listOf(
        Pair(75F, "#FF0000".toComposeColor()), // >= 75 dB
        Pair(65F, "#FF8000".toComposeColor()), // >= 65 dB
        Pair(55F, "#FFFF00".toComposeColor()), // >= 55 dB
        Pair(45F, "#99FF00".toComposeColor()), // >= 45 dB
        Pair(Float.NEGATIVE_INFINITY, "#00FF00".toComposeColor())
    ) // < 45 dB

    fun getColorIndex(noiseLevel: Double) = when {
        noiseLevel > 75.0 -> 0
        noiseLevel > 65 -> 1
        noiseLevel > 55 -> 2
        noiseLevel > 45 -> 3
        else -> 4
    }

    val scaleMode = SpectrogramBitmap.Companion.ScaleMode.SCALE_LOG
    val spectrumCanvasState = SpectrogramViewModel(
        SpectrogramBitmap.SpectrogramDataModel(
            IntSize(1, 1),
            ByteArray(Int.SIZE_BYTES),
            0,
            SpectrogramBitmap.Companion.ScaleMode.SCALE_LOG,
            1.0
        ),
        ArrayList(),
        Size.Zero
    )

    var bitmapOffset by remember { mutableStateOf(0) }
    var noiseLevel by remember { mutableStateOf(0.0) }
    var sampleRate by remember { mutableStateOf(DEFAULT_SAMPLE_RATE) }
    var spectrumDataState by remember {
        mutableStateOf(
            SpectrumPlotData(
                DoubleArray(0),
                DoubleArray(0)
            )
        )
    }
    var indicatorCollectJob: Job? = null
    var spectrumCollectJob: Job? = null
    val launchMeasurementJob = fun() {
        indicatorCollectJob = lifecycleOwner.lifecycleScope.launch {
            val levelDisplay = LevelDisplayWeightedDecay(FAST_DECAY_RATE, WINDOW_TIME)
            var levelDisplayBands: Array<LevelDisplayWeightedDecay>? = null
            measurementService.collectAudioIndicators().collect {
                if (levelDisplayBands == null) {
                    levelDisplayBands = Array(it.nominalFrequencies.size) {
                        LevelDisplayWeightedDecay(
                            FAST_DECAY_RATE,
                            WINDOW_TIME
                        )
                    }
                }
                noiseLevel = levelDisplay.getWeightedValue(it.laeq)
                val splWeightedArray = DoubleArray(it.nominalFrequencies.size) { index ->
                    levelDisplayBands!![index].getWeightedValue(it.thirdOctave[index])
                }
                spectrumDataState = SpectrumPlotData(it.thirdOctave, splWeightedArray)
            }
        }
        spectrumCollectJob = lifecycleOwner.lifecycleScope.launch {
            println("Launch spectrum lifecycle")
            measurementService.collectSpectrumData().collect() { spectrumData ->
                sampleRate = spectrumData.sampleRate.toDouble()
                if (spectrumCanvasState.currentStripData.size.width > 1) {
                    bitmapOffset = processSpectrum(spectrumCanvasState, spectrumData)
                }
            }
        }
    }
    launchMeasurementJob()
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.eventFlow.collect { event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                indicatorCollectJob?.cancel()
                spectrumCollectJob?.cancel()
            } else if (event == Lifecycle.Event.ON_RESUME &&
                (indicatorCollectJob == null || indicatorCollectJob?.isActive == false)
            ) {
                launchMeasurementJob()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints {
            if (maxWidth > maxHeight) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxWidth(.5F)) {
                        measurementHeader(noiseLevel, noiseColorRampSpl)
                    }
                    Column(modifier = Modifier) {
                        measurementPager(
                            bitmapOffset,
                            sampleRate,
                            spectrumDataState,
                            spectrumCanvasState,
                            scaleMode,
                            noiseColorRampSpl
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    measurementHeader(noiseLevel, noiseColorRampSpl)
                    measurementPager(
                        bitmapOffset,
                        sampleRate,
                        spectrumDataState,
                        spectrumCanvasState,
                        scaleMode,
                        noiseColorRampSpl,
                    )
                }
            }
        }
    }
}

@Composable
fun spectrogramLegend(
    scaleMode: SpectrogramBitmap.Companion.ScaleMode,
    sampleRate: Double,
) {
    var preparedLegendBitmap =
        LegendBitmap(
            ImageBitmap(1, 1),
            Size(0F, 0F),
            Size(0F, 0F),
            Size(0F, 0F)
        )
    val colorScheme = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (preparedLegendBitmap.imageSize != size) {
            preparedLegendBitmap = buildLegendBitmap(
                size,
                Density(density),
                scaleMode,
                sampleRate,
                textMeasurer,
                colorScheme
            )
        }
        drawImage(preparedLegendBitmap.imageBitmap)
    }
}

@Composable
fun buildNoiseLevelText(
    noiseLevel: Double,
    noiseColorRampSpl: List<Pair<Float, Color>>,
): AnnotatedString = buildAnnotatedString {
    val inRangeNoise = noiseLevel > MIN_SHOWN_DBA_VALUE && noiseLevel < MAX_SHOWN_DBA_VALUE
    val colorIndex = noiseColorRampSpl.indexOfFirst { pair -> pair.first < noiseLevel }
    withStyle(
        style = SpanStyle(
            color = if (inRangeNoise) noiseColorRampSpl[colorIndex].second else MaterialTheme.colorScheme.onPrimary,
            fontSize = NOISE_LEVEL_FONT_SIZE,
            baselineShift = BaselineShift.None
        )
    ) {
        when {
            inRangeNoise -> append("${round(noiseLevel * 10) / 10}")
            else -> append("-")
        }
    }
}

@Composable
fun spectrumPlot(
    modifier: Modifier,
    settings: SpectrumSettings,
    values: SpectrumPlotData,
    noiseColorRampSpl: List<Pair<Float, Color>>,
) {
    val surfaceColor = MaterialTheme.colorScheme.onSurface
    // color ramp 0F left side of spectrum
    // 1F right side of spectrum
    val spectrumColorRamp = List(noiseColorRampSpl.size) { index ->
        val pair = noiseColorRampSpl[noiseColorRampSpl.size - 1 - index]
        val linearIndex = max(
            0.0, ((pair.first - settings.minimumX) /
                (settings.maximumX - settings.minimumX))
        )
        Pair(linearIndex.toFloat(), pair.second)
    }.toTypedArray()
    Canvas(modifier) {
        val barHeight = size.height / values.spl.size - SPECTRUM_PLOT_SQUARE_OFFSET.toPx()
        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(
                SPECTRUM_PLOT_SQUARE_WIDTH.toPx(),
                SPECTRUM_PLOT_SQUARE_OFFSET.toPx()
            )
        )
        val weightedBarWidth = 10.dp.toPx()
        values.spl.forEachIndexed { index, spl ->
            val barYOffset = (barHeight + SPECTRUM_PLOT_SQUARE_OFFSET.toPx()) * index
            val splRatio = (spl - settings.minimumX) / (settings.maximumX - settings.minimumX)
            val splWeighted = max(spl, values.splWeighted[index])
            val splWeightedRatio =
                (splWeighted - settings.minimumX) / (settings.maximumX - settings.minimumX)
            val splGradient =
                Brush.horizontalGradient(*spectrumColorRamp, startX = 0F, endX = size.width)
            drawLine(
                brush = splGradient, start = Offset(0F, barYOffset + barHeight / 2),
                end = Offset((size.width * splRatio).toFloat(), barYOffset + barHeight / 2),
                strokeWidth = barHeight, pathEffect = pathEffect
            )
            drawRect(
                color = surfaceColor,
                topLeft = Offset(
                    (size.width * splWeightedRatio).toFloat() - weightedBarWidth,
                    barYOffset
                ),
                size = Size(weightedBarWidth, barHeight)
            )
        }
    }
}

@Composable
fun vueMeter(
    modifier: Modifier,
    settings: VueMeterSettings,
    value: Double,
    noiseColorRampSpl: List<Pair<Float, Color>>,
) {
    val color = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
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
                max(0.0, (value - settings.minimum) / (settings.maximum - settings.minimum))
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
        val valueRatio = (value - settings.minimum) / (settings.maximum - settings.minimum)
        val colorIndex = noiseColorRampSpl.indexOfFirst { pair -> pair.first < value }
        drawRoundRect(
            color = noiseColorRampSpl[colorIndex].second,
            topLeft = Offset(0F, maxHeight.toFloat()),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
            size = Size((size.width * valueRatio).toFloat(), barHeight)
        )
    }
}

@Composable
fun measurementHeader(
    noiseLevel: Double,
    noiseColorRampSpl: List<Pair<Float, Color>>,
) {
    val rightRoundedSquareShape: Shape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 40.dp,
        bottomStart = 0.dp,
        bottomEnd = 40.dp
    )
    val vueMeterSettings = VueMeterSettings(20.0, 120.0,
        IntArray(6) { v -> ((v + 1) * 20.0).toInt() })
    Column() {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                Modifier.padding(top = 20.dp, bottom = 10.dp).weight(1F),
                color = MaterialTheme.colorScheme.background,
                shape = rightRoundedSquareShape,
                shadowElevation = 10.dp
//                elevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontSize = TextUnit(
                                        18F,
                                        TextUnitType.Sp
                                    ),
                                )
                            ) { append("dB(A)") }
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Text(
                        buildNoiseLevelText(noiseLevel, noiseColorRampSpl),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            Row(
                Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    MeasurementStatistics("Min", "-"),
                    MeasurementStatistics("Avg", "-"),
                    MeasurementStatistics("Max", "-")
                ).forEach {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(it.label)
                        Text(it.value)
                    }
                }
            }
        }

        vueMeter(
            Modifier.fillMaxWidth().height(50.dp).padding(start = 30.dp, end = 30.dp),
            vueMeterSettings,
            noiseLevel,
            noiseColorRampSpl
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun measurementPager(
    bitmapOffset: Int,
    sampleRate: Double,
    spectrumData: SpectrumPlotData,
    spectrumCanvasState: SpectrogramViewModel,
    scaleMode: SpectrogramBitmap.Companion.ScaleMode,
    noiseColorRampSpl: List<Pair<Float, Color>>,
) {

    val animationScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { MeasurementTabState.entries.size })
    val spectrumSettings =
        SpectrumSettings(MIN_SHOWN_DBA_VALUE_SPECTRUM, MAX_SHOWN_DBA_VALUE_SPECTRUM)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            MeasurementTabState.entries.forEach { entry ->
                Tab(
                    text = { Text(MEASUREMENT_TAB_LABEL[entry.ordinal]) },
                    selected = pagerState.currentPage == entry.ordinal,
                    onClick = { animationScope.launch { pagerState.animateScrollToPage(entry.ordinal) } }
                )
            }
        }
        HorizontalPager(state = pagerState) { page ->
            when (MeasurementTabState.entries[page]) {
                MeasurementTabState.SPECTROGRAM -> Box(Modifier.fillMaxSize()) {
                    spectrogram(spectrumCanvasState, bitmapOffset)
                    spectrogramLegend(scaleMode, sampleRate)
                }

                MeasurementTabState.SPECTRUM -> spectrumPlot(
                    Modifier.fillMaxSize(),
                    spectrumSettings,
                    spectrumData,
                    noiseColorRampSpl
                )

                else -> Surface(
                    Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(
                        text = "Text tab ${MEASUREMENT_TAB_LABEL[page]} selected",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun spectrogram(
    spectrumCanvasState: SpectrogramViewModel,
    bitmapOffset: Int,
    scaleMode: SpectrogramBitmap.Companion.ScaleMode = SpectrogramBitmap.Companion.ScaleMode.SCALE_LOG,
) {

    var preparedLegendBitmap = LegendBitmap(
        ImageBitmap(1, 1),
        Size(0F, 0F),
        Size(0F, 0F),
        Size(0F, 0F)
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasSize = IntSize(
            SPECTROGRAM_STRIP_WIDTH,
            (size.height - preparedLegendBitmap.bottomLegendSize.height).toInt()
        )
        drawRect(
            color = SpectrogramBitmap.colorRamp[0],
            size = Size(
                size.width - preparedLegendBitmap.rightLegendSize.width,
                canvasSize.height.toFloat()
            )
        )
        spectrumCanvasState.spectrogramCanvasSize = Size(
            size.width - preparedLegendBitmap.rightLegendSize.width, size.height
                - preparedLegendBitmap.bottomLegendSize.height
        )
        if (spectrumCanvasState.currentStripData.size.height != canvasSize.height) {
            // reset buffer on resize or first draw
            // TODO: Use logger
            println(
                "Clear ${spectrumCanvasState.cachedStrips.size} strips" +
                    " ${spectrumCanvasState.currentStripData.size.height} != ${canvasSize.height}"
            )
            spectrumCanvasState.currentStripData = SpectrogramBitmap.createSpectrogram(
                canvasSize, scaleMode, spectrumCanvasState.currentStripData.sampleRate
            )
            spectrumCanvasState.cachedStrips.clear()
        } else {
            if (spectrumCanvasState.currentStripData.sampleRate > 1) {
                drawImage(
                    spectrumCanvasState.currentStripData.byteArray.toImageBitmap(),
                    topLeft = Offset(
                        size.width - bitmapOffset - preparedLegendBitmap.rightLegendSize.width,
                        0F
                    )
                )
                spectrumCanvasState.cachedStrips.reversed()
                    .forEachIndexed { index, imageBitmap ->
                        val bitmapX = size.width -
                            preparedLegendBitmap.rightLegendSize.width -
                            ((index + 1) * SPECTROGRAM_STRIP_WIDTH + bitmapOffset).toFloat()
                        drawImage(
                            imageBitmap,
                            topLeft = Offset(bitmapX, 0F)
                        )
                    }
            }
        }
    }
}

data class SpectrogramViewModel(
    var currentStripData: SpectrogramBitmap.SpectrogramDataModel,
    val cachedStrips: ArrayList<ImageBitmap>,
    var spectrogramCanvasSize: Size,
)

data class LegendElement(
    val text: AnnotatedString, val textSize: IntSize, val xPos: Float,
    val textPos: Float, val depth: Int,
)

enum class MeasurementTabState { SPECTRUM,
    SPECTROGRAM,
    MAP
}

val MEASUREMENT_TAB_LABEL = listOf("Spectrum", "Spectrogram", "Map")

data class LegendBitmap(
    val imageBitmap: ImageBitmap,
    val imageSize: Size,
    val bottomLegendSize: Size,
    val rightLegendSize: Size,
)

data class MeasurementStatistics(val label: String, val value: String)

data class SpectrumSettings(val minimumX: Double, val maximumX: Double)

data class SpectrumPlotData(val spl: DoubleArray, val splWeighted: DoubleArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SpectrumPlotData

        if (!spl.contentEquals(other.spl)) return false
        if (!splWeighted.contentEquals(other.splWeighted)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = spl.contentHashCode()
        result = 31 * result + splWeighted.contentHashCode()
        return result
    }
}

data class VueMeterSettings(val minimum: Double, val maximum: Double, val xLabels: IntArray) {

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

fun makeXLegend(
    textMeasurer: TextMeasurer,
    timeValue: Double,
    legendWidth: Float,
    timePerPixel: Double,
    depth: Int,
): LegendElement {
    val xPos = (legendWidth - timeValue / timePerPixel).toFloat()
    val legendText = buildAnnotatedString {
        withStyle(style = SpanStyle()) {
            append("+${round(timeValue).toInt()}s")
        }
    }
    val textLayout = textMeasurer.measure(legendText)
    val textPos = min(
        legendWidth - textLayout.size.width,
        max(0F, xPos - textLayout.size.width / 2)
    )
    return LegendElement(legendText, textLayout.size, xPos, textPos, depth)
}

@Suppress("LongParameterList")
fun recursiveLegendBuild(
    textMeasurer: TextMeasurer,
    timeValue: Double,
    legendWidth: Float,
    timePerPixel: Double,
    minX: Float,
    maxX: Float,
    timeValueLeft: Double,
    timeValueRight: Double,
    feedElements: ArrayList<LegendElement>,
    depth: Int,
) {
    val legendElement = makeXLegend(textMeasurer, timeValue, legendWidth, timePerPixel, depth)
    if (legendElement.textPos > minX && legendElement.xPos + legendElement.textSize.width / 2 < maxX) {
        feedElements.add(legendElement)
        // left legend, + x seconds
        recursiveLegendBuild(
            textMeasurer,
            timeValue + (timeValueLeft - timeValue) / 2,
            legendWidth,
            timePerPixel,
            minX,
            legendElement.textPos,
            timeValueLeft,
            timeValue,
            feedElements,
            depth + 1
        )
        // right legend, - x seconds
        recursiveLegendBuild(
            textMeasurer,
            timeValue - (timeValue - timeValueRight) / 2,
            legendWidth,
            timePerPixel,
            legendElement.textPos + legendElement.textSize.width,
            maxX,
            timeValue,
            timeValueRight,
            feedElements,
            depth + 1
        )
    }
}


fun buildLegendBitmap(
    size: Size,
    density: Density,
    scaleMode: SpectrogramBitmap.Companion.ScaleMode,
    sampleRate: Double,
    textMeasurer: TextMeasurer,
    colorScheme: ColorScheme,
): LegendBitmap {
    val drawScope = CanvasDrawScope()
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)

    var frequencyLegendPosition = when (scaleMode) {
        SpectrogramBitmap.Companion.ScaleMode.SCALE_LOG -> SpectrogramBitmap.frequencyLegendPositionLog
        else -> SpectrogramBitmap.frequencyLegendPositionLinear
    }
    frequencyLegendPosition =
        frequencyLegendPosition.filter { f -> f < sampleRate / 2 }.toIntArray()
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
        val legendHeight = timeXLabelHeight + tickLength
        val legendWidth = maxYLabelWidth + tickLength
        bottomLegendSize = Size(size.width - legendWidth, legendHeight)
        rightLegendSize = Size(legendWidth, size.height - legendHeight)
        if (sampleRate > 1) {
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
                    SpectrogramBitmap.Companion.ScaleMode.SCALE_LOG -> {
                        sheight - (log10(frequency / fMin) / ((log10(fMax / fMin) / sheight))).toInt()
                    }

                    else -> (sheight - frequency / fMax * sheight).toInt()
                }
                drawLine(
                    color = colorScheme.onPrimary,
                    start = Offset(
                        size.width - legendWidth,
                        tickHeightPos.toFloat() - tickStroke.toPx() / 2
                    ),
                    end = Offset(
                        size.width - legendWidth + tickLength,
                        tickHeightPos.toFloat() - tickStroke.toPx() / 2
                    ),
                    strokeWidth = tickStroke.toPx()
                )
                val textPos = min(
                    (size.height - textSize.size.height).toInt(),
                    max(0, tickHeightPos - textSize.size.height / 2)
                )
                drawText(
                    textMeasurer,
                    text,
                    topLeft = Offset(size.width - legendWidth + tickLength, textPos.toFloat())
                )
            }
            // draw X axe labels
            val xLegendWidth = (size.width - legendWidth)
            // One pixel per time step
            val timePerPixel = FFT_HOP / sampleRate
            val lastTime = xLegendWidth * timePerPixel
            val legendElements = ArrayList<LegendElement>()
            val rightLegend = makeXLegend(textMeasurer, 0.0, xLegendWidth, timePerPixel, -1)
            val leftLegend = makeXLegend(textMeasurer, lastTime, xLegendWidth, timePerPixel, -1)
            legendElements.add(leftLegend)
            legendElements.add(rightLegend)
            recursiveLegendBuild(
                textMeasurer, lastTime / 2, xLegendWidth, timePerPixel,
                leftLegend.textSize.width.toFloat(),
                rightLegend.xPos - rightLegend.textSize.width, lastTime, 0.0, legendElements, 0
            )
            // find depth index with maximum number of elements (to generate same intervals on legend)
            val legendDepthCount = IntArray(legendElements.maxOf { it.depth } + 1) { 0 }
            legendElements.forEach {
                if (it.depth >= 0) {
                    legendDepthCount[it.depth] += 1
                }
            }
            legendElements.removeAll {
                it.depth > 0 && legendDepthCount[it.depth] != (2.0.pow(it.depth)).toInt()
            }
            legendElements.forEach { legendElement ->
                val tickPos = max(
                    tickStroke.toPx() / 2F,
                    min(
                        xLegendWidth - tickStroke.toPx(),
                        legendElement.xPos - tickStroke.toPx() / 2F
                    )
                )
                drawLine(
                    color = colorScheme.onPrimary, start = Offset(
                        tickPos,
                        sheight.toFloat()
                    ),
                    end = Offset(
                        tickPos,
                        sheight + tickLength
                    ),
                    strokeWidth = tickStroke.toPx()
                )
                drawText(
                    textMeasurer,
                    legendElement.text,
                    topLeft = Offset(legendElement.textPos, sheight.toFloat() + tickLength)
                )
            }
        }
    }
    return LegendBitmap(bitmap, size, bottomLegendSize, rightLegendSize)
}

fun formatFrequency(frequency: Int): String {
    return if (frequency >= 1000) {
        if (frequency % 1000 > 0) {
            val subKilo = (frequency % 1000).toString().trimEnd('0')
            "${frequency / 1000}.$subKilo kHz"
        } else {
            "${frequency / 1000} kHz"
        }
    } else {
        "$frequency Hz"
    }
}

fun processSpectrum(
    spectrumCanvasState: SpectrogramViewModel,
    data: SpectrumData,
    rangedB: Double = 40.0,
    mindB: Double = 0.0,
    dbGain: Double = ANDROID_GAIN,
): Int {
    spectrumCanvasState
        .currentStripData
        .pushSpectrumToSpectrogramData(data, mindB, rangedB, dbGain)
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
                data.sampleRate.toDouble()
            )
    }
    return spectrumCanvasState.currentStripData.offset
}
