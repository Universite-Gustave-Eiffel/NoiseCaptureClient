package org.noise_planet.noisecapture.shared.signal

import kotlinx.coroutines.test.runTest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
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
        windowAnalysis.pushSamples(0, ones, processedWindows)
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
            )
            windowAnalysis.pushSamples(
                ones.size.toLong(),
                ones.copyOfRange((arraySize * 0.6).toInt(), ones.size),
                processedWindows
            )
            windowAnalysis.pushSamples(
                (ones.size + windowAnalysis.samplesUntilWindow).toLong(),
                FloatArray(windowAnalysis.samplesUntilWindow),
                processedWindows
            )
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
            windowAnalysis.pushSamples(0, ones.copyOfRange(i, min(i + step, ones.size)), processedWindows)
        }
        windowAnalysis.pushSamples(
            (ones.size + windowAnalysis.samplesUntilWindow).toLong(),
            FloatArray(windowAnalysis.samplesUntilWindow),
            processedWindows
        )
        assertEquals(ones.sum(), processedWindows.map { it.samples.sum() }.sum())
        val sum = windowAnalysis.reconstructOriginalSignal(processedWindows)
        ones.forEachIndexed { index, value ->
            assertEquals(value, sum[index], 1e-8f)
        }
    }

    @Test
    fun testSTFTSinus() = runTest {
        val sampleRate = 32000
        // create a 1000 Hz signal with hann function window
        val signal = generateSinusoidalFloatSignal(
            1000.0, sampleRate.toDouble(),
            1.0
        ) { (0.5 * (1 - cos(2 * PI * it / (32000 - 1)))).toFloat() }
        val windowSize = (sampleRate * 0.125).toInt()
        val hopSize = windowSize / 2
        val windowAnalysis = WindowAnalysis(sampleRate, windowSize, hopSize)
        val fftWindows = windowAnalysis.pushSamples(1000, signal).toList()
        println(fftWindows.size)
    }
}
