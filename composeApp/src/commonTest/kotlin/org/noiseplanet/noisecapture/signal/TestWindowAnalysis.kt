package org.noiseplanet.noisecapture.signal

import kotlinx.coroutines.test.runTest
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsProcessing
import org.noiseplanet.noisecapture.audio.AudioSamples
import org.noiseplanet.noisecapture.audio.WINDOW_TIME
import org.noiseplanet.noisecapture.audio.signal.FAST_DECAY_RATE
import org.noiseplanet.noisecapture.audio.signal.FrequencyBand
import org.noiseplanet.noisecapture.audio.signal.FrequencyBand.Companion.emptyFrequencyBands
import org.noiseplanet.noisecapture.audio.signal.LevelDisplayWeightedDecay
import org.noiseplanet.noisecapture.audio.signal.window.SpectrumData
import org.noiseplanet.noisecapture.audio.signal.window.SpectrumDataProcessing
import org.noiseplanet.noisecapture.audio.signal.window.Window
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class TestWindowAnalysis {

    @Test
    fun testHannWindow() {
        val expected = floatArrayOf(
            0f, 0.0954915f, 0.3454915f, 0.6545085f, 0.9045085f, 1f,
            0.9045085f, 0.6545085f, 0.3454915f, 0.0954915f, 0f
        )
        val windowAnalysis = SpectrumDataProcessing(44100, expected.size, 1)

        expected.forEachIndexed { index, value ->
            assertEquals(value, windowAnalysis.hannWindow?.get(index) ?: 0.0F, 1e-8f)
        }
    }

    @Test
    fun testOverlapWindows() {
        val arraySize = 13
        val ones = FloatArray(arraySize) { if (it in 2..arraySize - 3) 1f else 0f }
        val windowAnalysis = SpectrumDataProcessing(1, 5, 2)
        val processedWindows = ArrayList<Window>()
        windowAnalysis.pushSamples(0, ones, processedWindows).toList()
        assertEquals(5, processedWindows.size)
        assertEquals(ones.sum(), processedWindows.map { it.samples.sum() }.sum())
    }

    @Test
    fun testOverlapWindowsSegments() {
        for (arraySize in 9..13) {
            val ones = FloatArray(arraySize) { if (it in 2..arraySize - 3) 1f else 0f }
            val windowAnalysis = SpectrumDataProcessing(1, 5, 2)
            val processedWindows = ArrayList<Window>()
            windowAnalysis.pushSamples(
                (arraySize * 0.6).toLong(),
                ones.copyOfRange(0, (arraySize * 0.6).toInt()),
                processedWindows
            ).toList()
            windowAnalysis.pushSamples(
                ones.size.toLong(),
                ones.copyOfRange((arraySize * 0.6).toInt(), ones.size),
                processedWindows
            ).toList()
            windowAnalysis.pushSamples(
                (ones.size + windowAnalysis.samplesUntilWindow).toLong(),
                FloatArray(windowAnalysis.samplesUntilWindow),
                processedWindows
            ).toList()
            assertEquals(ones.sum(), processedWindows.map { it.samples.sum() }.sum())
            // reconstruct the array
            val sum = windowAnalysis.reconstructOriginalSignal(processedWindows)
            ones.forEachIndexed { index, value ->
                assertEquals(value, sum[index], 1e-8f)
            }
        }
    }

    @Test
    fun testOverlapWindowsSmallPush() {
        val arraySize = 13
        val ones = FloatArray(arraySize) { if (it in 2..arraySize - 3) 1f else 0f }
        // val ones = FloatArray(arraySize) {it.toFloat()}
        val windowAnalysis = SpectrumDataProcessing(1, 5, 2)
        val processedWindows = ArrayList<Window>()
        val step = 3
        for (i in ones.indices step step) {
            windowAnalysis.pushSamples(
                0,
                ones.copyOfRange(i, min(i + step, ones.size)),
                processedWindows
            ).toList()
        }
        windowAnalysis.pushSamples(
            (ones.size + windowAnalysis.samplesUntilWindow).toLong(),
            FloatArray(windowAnalysis.samplesUntilWindow),
            processedWindows
        ).toList()
        assertEquals(ones.sum(), processedWindows.map { it.samples.sum() }.sum())
        val sum = windowAnalysis.reconstructOriginalSignal(processedWindows)
        ones.forEachIndexed { index, value ->
            assertEquals(value, sum[index], 1e-8f)
        }
    }

    @Test
    fun testSinusSTFFTRectangularWindow() = runTest {
        val sampleRate = 32768
        val expectedLevel = 94.0
        val peak = 10.0.pow(expectedLevel / 20.0) * sqrt(2.0)
        val sum: (Float, Float) -> Float = { x: Float, y: Float -> x + y }
        val frequencyPeaks = doubleArrayOf(1000.0)
        var signal = FloatArray(sampleRate)
        // sum multiple sinusoidal signals
        frequencyPeaks.forEach { frequencyPeak ->
            signal = signal.zip(
                TestFFT.generateSinusoidalFloatSignal(
                    frequencyPeak,
                    sampleRate.toDouble(), 1.0
                ) { peak.toFloat() },
                sum
            ).toFloatArray()
        }

        val bufferSize = (sampleRate * 0.1).toInt()
        var cursor = 0
        val wa = SpectrumDataProcessing(sampleRate, 4096, 4096, applyHannWindow = false)
        val spectrumDataArray = ArrayList<SpectrumData>()
        while (cursor < signal.size) {
            val windowSize = min(bufferSize, signal.size - cursor)
            val windowBuffer = signal.copyOfRange(cursor, cursor + windowSize)
            val epoch = (((cursor + windowSize) / sampleRate.toDouble()) * 1000).toLong()
            spectrumDataArray.addAll(wa.pushSamples(epoch, windowBuffer))
            cursor += windowSize
        }
        val hertzPerCell = wa.windowSize / sampleRate.toDouble()
        spectrumDataArray.forEach { spectrumData ->
            assertEquals(
                expectedLevel,
                spectrumData.spectrum[(hertzPerCell * frequencyPeaks[0]).toInt()].toDouble(),
                0.01
            )
        }
    }

    @Test
    fun testSinusSTFFTHannWindowThirdOctave() = runTest {
        val sampleRate = 32768
        val expectedLevel = 94.0
        val peak = 10.0.pow(expectedLevel / 20.0) * sqrt(2.0)
        val sum: (Float, Float) -> Float = { x: Float, y: Float -> x + y }
        val frequencyPeaks = doubleArrayOf(1000.0)
        var signal = FloatArray(sampleRate)
        // sum multiple sinusoidal signals
        frequencyPeaks.forEach { frequencyPeak ->
            signal = signal.zip(
                TestFFT.generateSinusoidalFloatSignal(
                    frequencyPeak,
                    sampleRate.toDouble(), 1.0
                ) { peak.toFloat() },
                sum
            ).toFloatArray()
        }

        val bufferSize = (sampleRate * 0.1).toInt()
        var cursor = 0
        val windowSize = (sampleRate * 0.125).toInt()
        val wa = SpectrumDataProcessing(sampleRate, windowSize, windowSize / 2)
        val spectrumDataArray = ArrayList<SpectrumData>()
        while (cursor < signal.size) {
            val windowSize = min(bufferSize, signal.size - cursor)
            val windowBuffer = signal.copyOfRange(cursor, cursor + windowSize)
            val epoch = (((cursor + windowSize) / sampleRate.toDouble()) * 1000).toLong()
            spectrumDataArray.addAll(wa.pushSamples(epoch, windowBuffer))
            cursor += windowSize
        }
        spectrumDataArray.forEachIndexed { index, spectrumData ->
            val thirdOctaveSquare = spectrumData.thirdOctaveProcessing(
                50.0,
                12000.0,
                octaveWindow = OctaveWindow.RECTANGULAR
            ).asList()
            val thirdOctaveFractional = spectrumData.thirdOctaveProcessing(
                50.0,
                12000.0,
                octaveWindow = OctaveWindow.FRACTIONAL
            ).asList()
            val indexOf1000Hz =
                thirdOctaveSquare.indexOfFirst { t -> t.midFrequency.toInt() == 1000 }
            assertEquals(
                expectedLevel,
                thirdOctaveSquare[indexOf1000Hz].spl,
                0.01,
                message = "[$index]"
            )
            assertEquals(
                expectedLevel,
                thirdOctaveFractional[indexOf1000Hz].spl,
                0.01,
                message = "[$index]"
            )
        }
    }

    @Test
    fun testFastLevelDecayRateTest() {
        // generate noise levels each 125 ms
        val cutoffTime = 5
        val timeInterval = 0.125
        val levels =
            DoubleArray((8 / timeInterval).toInt()) { t -> if (t * timeInterval < cutoffTime) 94.0 else -99.0 }
        // fast level should reach input noise in 0.6 seconds
        // fast level decay should be at the rate of 34.7 dB/s
        val levelDisplayWeightedDecay = LevelDisplayWeightedDecay(FAST_DECAY_RATE, timeInterval)
        var previousValue = 0.0
        levels.forEachIndexed { index, level ->
            val dbValue = levelDisplayWeightedDecay.getWeightedValue(level)
            if (index * timeInterval >= cutoffTime) {
                assertEquals(FAST_DECAY_RATE, (dbValue - previousValue) / timeInterval, 0.01)
            }
            previousValue = dbValue
        }
    }

    @Test
    fun testAndroidSpecNoiseLevel() = runTest {
        val sampleRate = 48000
        val expectedLevel = 90.0
        val peak = (2500 * sqrt(2.0)).toInt().toShort()

        val angularFrequency = 2.0 * PI * 1000 / sampleRate
        val signal = FloatArray((sampleRate * WINDOW_TIME).toInt()) {
            (sin(it * angularFrequency).toFloat() * peak) / 32768F
        }

        val acousticIndicatorProcessing = AcousticIndicatorsProcessing(sampleRate)
        val processed = acousticIndicatorProcessing.processSamples(
            AudioSamples(0, signal, sampleRate)
        )
        val averageLeq = processed.map { indicators -> indicators.leq }.average()
        assertEquals(expectedLevel, averageLeq, 0.01)
    }
}

enum class OctaveWindow {
    RECTANGULAR,
    FRACTIONAL
}

/**
 * @see <a href="https://www.ap.com/technical-library/deriving-fractional-octave-spectra-from-the-fft-with-apx/">ref</a>
 * Class 0 filter is 0.15 dB error according to IEC 61260
 *
 * @param firstFrequencyBand Skip bands up to specified frequency
 * @param lastFrequencyBand Skip bands higher than this frequency
 * @param base Octave base 10 or base 2
 * @param octaveWindow Rectangular association of frequency band or fractional close to done by a filter
 */
@Suppress("NestedBlockDepth")
fun SpectrumData.thirdOctaveProcessing(
    firstFrequencyBand: Double,
    lastFrequencyBand: Double,
    base: FrequencyBand.BaseMethod = FrequencyBand.BaseMethod.B10,
    bandDivision: Double = 3.0,
    octaveWindow: OctaveWindow = OctaveWindow.FRACTIONAL,
): Array<FrequencyBand> {
    val freqByCell: Double = (spectrum.size.toDouble() * 2) / sampleRate
    val thirdOctave = emptyFrequencyBands(
        firstFrequencyBand, lastFrequencyBand, base, bandDivision,
    )

    if (octaveWindow == OctaveWindow.FRACTIONAL) {
        for (band in thirdOctave) {
            for (cellIndex in spectrum.indices) {
                val f = (cellIndex + 1) / freqByCell
                val division =
                    (f / band.midFrequency - band.midFrequency / f) * 1.507 * bandDivision
                val cellGain = sqrt(1.0 / (1.0 + division.pow(6)))
                val fg = 10.0.pow(spectrum[cellIndex] / 10.0) * cellGain
                if (fg.isFinite()) {
                    band.spl += fg
                }
            }
        }
        for (band in thirdOctave) {
            band.spl = 10 * log10(band.spl)
        }
    } else {
        for (band in thirdOctave) {
            val minCell = max(0, floor(band.minFrequency * freqByCell).toInt())
            val maxCell = min(spectrum.size, ceil(band.maxFrequency * freqByCell).toInt())
            var rms = 0.0
            for (cellIndex in minCell..<maxCell) {
                val fg = 10.0.pow(spectrum[cellIndex] / 10.0)
                if (fg.isFinite()) {
                    rms += fg
                }
            }
            band.spl = 10 * log10(rms)
        }
    }
    return thirdOctave
}
