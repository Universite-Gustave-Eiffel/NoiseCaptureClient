package org.noise_planet.noisecapture.shared.signal

import kotlinx.coroutines.test.runTest
import org.noise_planet.noisecapture.AudioSamples
import org.noise_planet.noisecapture.shared.AcousticIndicatorsProcessing
import org.noise_planet.noisecapture.shared.FFT_HOP
import org.noise_planet.noisecapture.shared.FFT_SIZE
import org.noise_planet.noisecapture.shared.WINDOW_TIME
import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestWindowAnalysis {

    @Test
    fun testHannWindow() {
        val expected = floatArrayOf(0f       , 0.0954915f, 0.3454915f, 0.6545085f, 0.9045085f, 1f       ,
            0.9045085f, 0.6545085f, 0.3454915f, 0.0954915f, 0f)
        val windowAnalysis = WindowAnalysis(44100, expected.size, 1)

        expected.forEachIndexed { index, value ->
            assertEquals(value, windowAnalysis.hannWindow?.get(index) ?: 0.0F, 1e-8f)
        }
    }

    @Test
    fun testOverlapWindows() {
        val arraySize = 13
        val ones = FloatArray(arraySize) {if(it in 2..arraySize-3) 1f else 0f}
        val windowAnalysis = WindowAnalysis(1, 5, 2)
        val processedWindows = ArrayList<Window>()
        windowAnalysis.pushSamples(0, ones, processedWindows).toList()
        assertEquals(5, processedWindows.size)
        assertEquals(ones.sum(), processedWindows.map { it.samples.sum() }.sum())
    }

    @Test
    fun testOverlapWindowsSegments() {
        for(arraySize in 9..13) {
            val ones = FloatArray(arraySize) { if (it in 2..arraySize - 3) 1f else 0f }
            val windowAnalysis = WindowAnalysis(1, 5, 2)
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
        val ones = FloatArray(arraySize) {if(it in 2..arraySize-3) 1f else 0f}
        //val ones = FloatArray(arraySize) {it.toFloat()}
        val windowAnalysis = WindowAnalysis(1, 5, 2)
        val processedWindows = ArrayList<Window>()
        val step = 3
        for(i in ones.indices step step) {
            windowAnalysis.pushSamples(0, ones.copyOfRange(i, min(i + step, ones.size)), processedWindows).toList()
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
        val peak = 10.0.pow(expectedLevel/20.0)* sqrt(2.0)
        val sum: (Float, Float) -> Float = { x: Float, y: Float -> x + y }
        val frequencyPeaks = doubleArrayOf(1000.0)
        var signal = FloatArray(sampleRate)
        // sum multiple sinusoidal signals
        frequencyPeaks.forEach { frequencyPeak ->
            signal = signal.zip(TestFFT.generateSinusoidalFloatSignal(frequencyPeak,
                sampleRate.toDouble(), 1.0){peak.toFloat()}, sum).toFloatArray()}

        val bufferSize = (sampleRate * 0.1).toInt()
        var cursor = 0
        val wa = WindowAnalysis(sampleRate, 4096, 4096, applyHannWindow = false)
        val spectrumDataArray = ArrayList<SpectrumData>()
        while (cursor < signal.size) {
            val windowSize = min(bufferSize, signal.size - cursor)
            val windowBuffer = signal.copyOfRange(cursor, cursor+windowSize)
            val epoch = (((cursor+windowSize)/sampleRate.toDouble())*1000).toLong()
            spectrumDataArray.addAll(wa.pushSamples(epoch, windowBuffer))
            cursor += windowSize
        }
        val hertzPerCell = wa.windowSize/sampleRate.toDouble()
        spectrumDataArray.forEach { spectrumData ->
            assertEquals(expectedLevel,
                spectrumData.spectrum[(hertzPerCell*frequencyPeaks[0]).toInt()].toDouble(),
                0.01)
        }
    }


    @Test
    fun testSinusSTFFTHannWindowThirdOctave() = runTest {
        val sampleRate = 32768
        val expectedLevel = 94.0
        val peak = 10.0.pow(expectedLevel/20.0)* sqrt(2.0)
        val sum: (Float, Float) -> Float = { x: Float, y: Float -> x + y }
        val frequencyPeaks = doubleArrayOf(1000.0)
        var signal = FloatArray(sampleRate)
        // sum multiple sinusoidal signals
        frequencyPeaks.forEach { frequencyPeak ->
            signal = signal.zip(TestFFT.generateSinusoidalFloatSignal(frequencyPeak,
                sampleRate.toDouble(), 1.0){peak.toFloat()}, sum).toFloatArray()}

        val bufferSize = (sampleRate * 0.1).toInt()
        var cursor = 0
        val wa = WindowAnalysis(sampleRate, 4096, 2048)
        val spectrumDataArray = ArrayList<SpectrumData>()
        while (cursor < signal.size) {
            val windowSize = min(bufferSize, signal.size - cursor)
            val windowBuffer = signal.copyOfRange(cursor, cursor+windowSize)
            val epoch = (((cursor+windowSize)/sampleRate.toDouble())*1000).toLong()
            spectrumDataArray.addAll(wa.pushSamples(epoch, windowBuffer))
            cursor += windowSize
        }
        spectrumDataArray.forEachIndexed { index, spectrumData ->
            val thirdOctaveSquare = spectrumData.thirdOctaveProcessing(50.0, 12000.0,
                octaveWindow = SpectrumData.OCTAVE_WINDOW.RECTANGULAR).asList()
            val thirdOctaveFractional = spectrumData.thirdOctaveProcessing(50.0,
                12000.0, octaveWindow = SpectrumData.OCTAVE_WINDOW.FRACTIONAL).asList()
            val indexOf1000Hz = thirdOctaveSquare.indexOfFirst { t -> t.midFrequency.toInt() == 1000 }
            assertEquals(expectedLevel, thirdOctaveSquare[indexOf1000Hz].spl, 0.01, message = "[$index]")
            assertEquals(expectedLevel, thirdOctaveFractional[indexOf1000Hz].spl, 0.01, message = "[$index]")
        }
    }

    @Test
    fun testFastLevelDecayRateTest() {
        // generate noise levels each 125 ms
        val cutoffTime = 5
        val timeInterval=0.125
        val levels = DoubleArray((8/timeInterval).toInt()) { t -> if(t*timeInterval < cutoffTime) 94.0 else -99.0 }
        // fast level should reach input noise in 0.6 seconds
        // fast level decay should be at the rate of 34.7 dB/s
        val levelDisplayWeightedDecay = LevelDisplayWeightedDecay(FAST_DECAY_RATE, timeInterval)
        var previousValue = 0.0
        levels.forEachIndexed { i , it->
            val dbValue = levelDisplayWeightedDecay.getWeightedValue(it)
            if(i*timeInterval >= cutoffTime) {
                assertEquals(FAST_DECAY_RATE, (dbValue-previousValue)/timeInterval, 0.01)
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
        val signal = FloatArray((sampleRate* WINDOW_TIME).toInt()) {
            (sin(it * angularFrequency).toFloat() * peak) / 32768F
        }

        val acousticIndicatorProcessing = AcousticIndicatorsProcessing(sampleRate)
        val processed = acousticIndicatorProcessing.processSamples(
            AudioSamples(0, signal,
            AudioSamples.ErrorCode.OK, sampleRate)
        )
        val averageLeq = processed.map{indicators -> indicators.leq}.average()
        assertEquals(expectedLevel, averageLeq, 0.01)
    }
}
