package org.noise_planet.noisecapture.shared.signal

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min

const val SPECTRUM_REPLAY = 10
const val SPECTRUM_CACHE = 10
class WindowAnalysis(val sampleRate : Int, val windowSize : Int, val windowHop : Int) {
    val spectrum = MutableSharedFlow<SpectrumData>(replay = SPECTRUM_REPLAY,
        extraBufferCapacity = SPECTRUM_CACHE, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val partialWindows = ArrayList<Window>()
    val hannWindow = FloatArray(windowSize) {(0.5 * (1 - cos(2 * PI * it / (windowSize - 1)))).toFloat()}
    //val hannWindow = FloatArray(windowSize) {1f}

    private fun processCachedWindows(processedWindows: MutableList<Window>? = null) {
        while(!partialWindows.isEmpty()) {
            val window = partialWindows[0]
            if(window.cursor >= window.samples.size) {
                processWindow(window)
                partialWindows.removeAt(0)
                processedWindows?.add(window)
            } else {
                break
            }
        }
    }

    /**
     * @see <a href="https://www.dsprelated.com/freebooks/sasp/Filling_FFT_Input_Buffer.html">Filling the FFT Input Buffer</a>
     */
    fun pushSamples(epoch: Long, samples: FloatArray, processedWindows: MutableList<Window>? = null) {
        // feed partial windows
        var hopOffset = 0
        if(!partialWindows.isEmpty()) {
            hopOffset = partialWindows.last().cursor % windowHop
        }
        for(window in partialWindows) {
            val loopTo = min(window.samples.size-window.cursor, samples.size)
            for(i in 0..< loopTo) {
                window.samples[i+window.cursor] = samples[i] * hannWindow[i+window.cursor]
            }
            window.cursor += loopTo
        }
        processCachedWindows(processedWindows)
        // Process new windows
        while(hopOffset < samples.size) {
            val window = Window((epoch+(hopOffset/sampleRate.toDouble())*1000).toLong(), 0, FloatArray(windowSize))
            val loopTo = min(window.samples.size, samples.size-hopOffset)
            for(i in 0..< loopTo) {
                window.samples[i] = samples[i + hopOffset] * hannWindow[i]
            }
            window.cursor += loopTo - window.cursor
            if(window.cursor < windowSize) {
                partialWindows.add(window)
            } else {
                processWindow(window)
                processedWindows?.add(window)
            }
            hopOffset += windowHop
        }
    }

    private fun processWindow(window: Window) {

    }
}

data class Window(val epoch : Long, var cursor : Int, val samples : FloatArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Window

        if (epoch != other.epoch) return false
        if (cursor != other.cursor) return false
        if (!samples.contentEquals(other.samples)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = epoch.hashCode()
        result = 31 * result + cursor
        result = 31 * result + samples.contentHashCode()
        return result
    }
}

data class SpectrumData(val epoch : Long, val spectrum : FloatArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SpectrumData

        if (epoch != other.epoch) return false
        if (!spectrum.contentEquals(other.spectrum)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = epoch.hashCode()
        result = 31 * result + spectrum.contentHashCode()
        return result
    }
}