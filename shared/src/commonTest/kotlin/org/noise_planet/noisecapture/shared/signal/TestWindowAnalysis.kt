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
        val ones = FloatArray(10) { 1f }
        val windowAnalysis = WindowAnalysis(64, 5, 2)
        val processedWindow = ArrayList<Window>()
        windowAnalysis.pushSamples(0, ones, processedWindow)
        assertEquals(3, processedWindow.size)
        assertEquals(2, windowAnalysis.windows.size)
        // zero padding at the end
        windowAnalysis.pushSamples(0, FloatArray(3), processedWindow)
        assertEquals(5, processedWindow.size)

    }

}