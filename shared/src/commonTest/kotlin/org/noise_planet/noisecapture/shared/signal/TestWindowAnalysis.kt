package org.noise_planet.noisecapture.shared.signal

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
        val arraySize = 13
        val ones = FloatArray(arraySize) {if(it in 2..arraySize-3) 1f else 0f}
        val windowAnalysis = WindowAnalysis(1, 5, 2)
        val processedWindows = ArrayList<Window>()
        windowAnalysis.pushSamples(0, ones.copyOfRange(0, 9), processedWindows)
        assertEquals(3, processedWindows.size)
        assertEquals(2, windowAnalysis.partialWindows.size)
        windowAnalysis.pushSamples(0, ones.copyOfRange(9, ones.size), processedWindows)
        assertEquals(5, processedWindows.size)
        assertEquals(ones.sum(), processedWindows.map { it.samples.sum() }.sum())
    }

}