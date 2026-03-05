package org.noiseplanet.noisecapture.signal

import org.noiseplanet.noisecapture.audio.AudioSamples
import org.noiseplanet.noisecapture.audio.signal.window.SamplesWindowing
import org.noiseplanet.noisecapture.audio.signal.window.Window
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SamplesWindowingTest {

    @Test
    fun testSamplesNeverFillWindowBuffer() {
        val windows = mutableListOf<Window>()
        val samples = FloatArray(800) { Random.nextFloat() }

        val windowing = SamplesWindowing(1_000)
        windows += windowing.pushSamples(AudioSamples(0, samples, 48_000))

        // Not enough samples were pushed so no window should have been created
        assertTrue(windows.isEmpty())
    }

    @Test
    fun testSamplesSmallerThanWindowSize() {
        val windows = mutableListOf<Window>()
        val samples = FloatArray(800) { Random.nextFloat() }

        val windowing = SamplesWindowing(1_000)

        windows += windowing.pushSamples(AudioSamples(0, samples, 48_000))
        windows += windowing.pushSamples(AudioSamples(0, samples, 48_000))

        // Expect 1 window with samples + 200 first samples
        // (the last 600 aren't enough for a second window)
        assertEquals(1, windows.size)
        assertContentEquals(samples + samples.take(200), windows[0].samples)
    }

    @Test
    fun testSamplesSameSizeAsWindow() {
        val windows = mutableListOf<Window>()
        val samples = FloatArray(1_000) { Random.nextFloat() }

        val windowing = SamplesWindowing(1_000)

        windows += windowing.pushSamples(AudioSamples(0, samples, 48_000))
        windows += windowing.pushSamples(AudioSamples(0, samples, 48_000))

        // Expect 2 windows, each containing all the 1_000 samples
        assertEquals(2, windows.size)
        assertContentEquals(samples, windows[0].samples)
        assertContentEquals(samples, windows[1].samples)
    }

    @Test
    fun testSamplesHalfOfWindowSize() {
        val windows = mutableListOf<Window>()
        val samples = FloatArray(500) { Random.nextFloat() }

        val windowing = SamplesWindowing(1_000)

        windows += windowing.pushSamples(AudioSamples(0, samples, 48_000))
        windows += windowing.pushSamples(AudioSamples(0, samples, 48_000))

        // Expect 1 window containing the samples twice
        assertEquals(1, windows.size)
        assertContentEquals(samples + samples, windows[0].samples)
    }

    @Test
    fun testSamplesLargerThanWindowSize() {
        val windows = mutableListOf<Window>()
        val samples = FloatArray(1_500) { Random.nextFloat() }

        val windowing = SamplesWindowing(1_000)

        windows += windowing.pushSamples(AudioSamples(0, samples, 48_000))
        windows += windowing.pushSamples(AudioSamples(0, samples, 48_000))

        // Expect 3 windows:
        // Window 1 contains samples [0:999]
        // Window 2 contains samples [1_000:1_499] + [0:499]
        // Window 3 contains samples [500:1_499]
        assertEquals(3, windows.size)
        assertContentEquals(samples.take(1_000), windows[0].samples.toList())
        assertContentEquals(samples.takeLast(500) + samples.take(500), windows[1].samples.toList())
        assertContentEquals(samples.takeLast(1_000), windows[2].samples.toList())
    }

    @Test
    fun testMemoryStrategy() {
        val windows = mutableListOf<Window>()
        val samples = FloatArray(1_500) { Random.nextFloat() }

        val windowingWithCopy = SamplesWindowing(
            windowSize = 1_000,
            memoryStrategy = SamplesWindowing.MemoryStrategy.BUFFER_COPY,
        )
        val windowingWithReference = SamplesWindowing(
            windowSize = 1_000,
            memoryStrategy = SamplesWindowing.MemoryStrategy.BUFFER_REFERENCE,
        )

        // With BUFFER_COPY, output window should contain the first 1_000 samples
        windows += windowingWithCopy.pushSamples(AudioSamples(0, samples, 48_000))
        assertContentEquals(samples.take(1_000), windows[0].samples.toList())

        // With BUFFER_REFERENCE, output window should contain the last 500 samples,
        // then samples [500:999] because the first 500 samples of the window have been overridden
        windows += windowingWithReference.pushSamples(AudioSamples(0, samples, 48_000))
        assertContentEquals(
            samples.takeLast(500) + samples.slice(500..999),
            windows[1].samples.toList()
        )
    }

    @Test
    fun testWindowTimestamps() {
        val windows = mutableListOf<Window>()
        val samples = FloatArray(1_500) { Random.nextFloat() }

        val windowing = SamplesWindowing(windowSize = 1_000)

        windows += windowing.pushSamples(AudioSamples(1_500, samples, 1_000))
        windows += windowing.pushSamples(AudioSamples(3_000, samples, 1_000))

        // Expect 3 windows with timestamps 1000, 2000 and 3000 respectively (with sample rate = 1kHz-
        assertEquals(3, windows.size)
        assertEquals(1_000, windows[0].timestamp)
        assertEquals(2_000, windows[1].timestamp)
        assertEquals(3_000, windows[2].timestamp)
    }
}
