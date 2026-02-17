package org.noiseplanet.noisecapture.audio.signal.window

import org.noiseplanet.noisecapture.audio.AudioSamples
import kotlin.math.min

/**
 * Utility class that produces sample windows of fixed sizes when feeding in samples arrays
 * of variable sizes.
 *
 * @param windowSize Output windows size, in samples
 * @param memoryStrategy How the window buffer should be passed down. By default, buffer is copied
 *                       in a new array, but if output window is intended to be processed immediately,
 *                       a reference to the buffer can be passed instead for better performance.
 */
class SamplesWindowing(
    val windowSize: Int,
    private val memoryStrategy: MemoryStrategy = MemoryStrategy.BUFFER_COPY,
) {
    // - Associated types

    enum class MemoryStrategy {
        BUFFER_COPY,
        BUFFER_REFERENCE
    }


    // - Properties

    /**
     * Internal samples buffer, filled progressively with incoming samples
     */
    private var buffer = FloatArray(windowSize)

    /**
     * Current buffer offset, in samples. Indicates the position at which incoming samples
     * will be pushed into the buffer
     */
    private var bufferCursor: Int = 0


    // - Public functions

    /**
     * Pushes new samples for windowing.
     *
     * @param audioSamples Audio samples with associated timestamp and sample rate.
     * @return Windowed audio samples, with timestamp associated to each window
     */
    fun pushSamples(audioSamples: AudioSamples): List<Window> = sequence {
        // Tracks the current offset in the incoming samples array
        var samplesCursor = 0

        // Iterate until all samples have been processed
        while (samplesCursor < audioSamples.samples.size) {
            // How much room is still available in the buffer before next window is reached
            val bufferAvailableSpace = windowSize - bufferCursor
            // Position of samples cursor after copy (i.e. copy end index)
            val samplesCursorAfterCopy = min(
                audioSamples.samples.size,
                samplesCursor + bufferAvailableSpace
            )

            // Copy the target slice of samples into internal buffer
            audioSamples.samples.copyInto(
                destination = buffer,
                destinationOffset = bufferCursor,
                startIndex = samplesCursor,
                endIndex = samplesCursorAfterCopy,
            )

            // Update buffer and samples cursor
            bufferCursor += samplesCursorAfterCopy - samplesCursor
            samplesCursor = samplesCursorAfterCopy

            // If buffer is full, create and emit the new window, then reset buffer cursor
            if (bufferCursor == windowSize) {
                // Window timestamp is calculated based on the timestamp of the last sample minus
                // the equivalent duration of the remaining samples at the given sample rate
                val remainingSamples = audioSamples.samples.size - samplesCursor
                val remainingTime = remainingSamples / audioSamples.sampleRate.toDouble() * 1000.0
                val windowTimestamp = audioSamples.timestamp - remainingTime.toLong()

                val window = Window(
                    timestamp = windowTimestamp,
                    samples = when (memoryStrategy) {
                        MemoryStrategy.BUFFER_COPY -> buffer.copyOf()
                        MemoryStrategy.BUFFER_REFERENCE -> buffer
                    }
                )
                yield(window)
                bufferCursor = 0
            }
        }
    }.toList()

    /**
     * Flushes the internal buffer.
     */
    fun flush() {
        buffer = FloatArray(windowSize)
        bufferCursor = 0
    }
}
