package org.noise_planet.noisecapture.shared.signal

import kotlinx.coroutines.test.runTest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class TestWindowAnalysis {

    @Test
    fun testHannWindow() {
        val expected = floatArrayOf(0f       , 0.0954915f, 0.3454915f, 0.6545085f, 0.9045085f, 1f       ,
            0.9045085f, 0.6545085f, 0.3454915f, 0.0954915f, 0f)
        val windowAnalysis = WindowAnalysis(44100, expected.size, 1)

        expected.forEachIndexed { index, value ->
            assertEquals(value, windowAnalysis.hannWindow[index], 1e-8f)
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
    fun testSTFTSinus() = runTest {
        val sampleRate = 32768
        val expectedLevel = 94
        val peak = 10.0.pow(expectedLevel/20.0)*sqrt(2.0)
        val sum: (Float, Float) -> Float = { x: Float, y: Float -> x + y }
        var signal = generateSinusoidalFloatSignal(1000.0, sampleRate.toDouble(), 1.0)
            .zip(generateSinusoidalFloatSignal(1600.0, sampleRate.toDouble(), 1.0), sum)
            .toFloatArray().zip(
                generateSinusoidalFloatSignal(4000.0, sampleRate.toDouble(), 1.0), sum
            ).toFloatArray().zip(
                generateSinusoidalFloatSignal(125.0, sampleRate.toDouble(), 1.0), sum
            ).toFloatArray()
        signal = signal.map(fun(it: Float): Float {
            return (peak * it).toFloat()
        }).toFloatArray()

        fftFloat(signal.size / 2, signal)
        for (i in signal.indices) {
            signal[i] = signal[i] / signal.size
        }
        //val fr = realFFTFloat(signal).map {
        //    it / signal.size
        //}
        println(signal.joinToString())

//        val windowSize = (sampleRate * 0.125).toInt()
//        val hopSize = windowSize / 2
//        val windowAnalysis = WindowAnalysis(sampleRate, windowSize, hopSize)
//        // Sum of STFFT should be equal to the FFT of the whole signal
//        var sum = 0.0
//        var windows = 0
//        val rmsSignal = sqrt(signal.map { it * it }.average())
//        println("rmsSignal=$rmsSignal")
//        val fftWindow = FloatArray(nextPowerOfTwo(signal.size))
//        signal.copyInto(fftWindow, 0, 0, signal.size)
//        //signal.copyInto(fftWindow, 0, signal.size / 2, signal.size)
//        //signal.copyInto(fftWindow, fftWindow.size - signal.size / 2, 0, signal.size / 2)
//        val spectrum = realFFTFloat(fftWindow)
//        var sumWhole = sqrt((spectrum.mapIndexed { index, value ->
//            if(index < spectrum.size / 2) { value*value } else{ 0.0.toFloat() } }.sum().toDouble()) / ((signal.size / 2)))
//        //var sumWhole = sqrt(spectrum.map {it * it}.sum() / signal.size)
//        //sumWhole = sqrt(sumWhole / fftWindow.size)
//        println("sumWhole=$sumWhole")
//        windowAnalysis.pushSamples(1125, signal).forEach { window ->
//            sum += window.spectrum.map { it * it }.sum().toDouble()
//            windows += 1
//        }
//        val fftWindow = FloatArray(nextPowerOfTwo(signal.size))
//        signal.copyInto(fftWindow, 0, signal.size / 2, signal.size)
//        signal.copyInto(fftWindow, fftWindow.size - signal.size / 2, 0, signal.size / 2)
//        val spectrum = realFFTFloat(fftWindow)
//        val spectrumData = SpectrumData(1000, spectrum)
//        var sumWhole = sqrt(spectrumData.spectrum.map { it * it }.sum() / signal.size).toDouble()
//        val thirdOctave = spectrumData.thirdOctaveProcessing(sampleRate, 125.0, 16000.0).asList()
//        println("rmsSignal=$rmsSignal\nsum=$sum\nsumWhole=$sumWhole\nthirdOctave=${thirdOctave.size}")
//        assertEquals(sumWhole, sum, 1e-6)
    }
}
