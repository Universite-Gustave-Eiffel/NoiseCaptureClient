package org.noiseplanet.noisecapture.audio

import org.noiseplanet.noisecapture.audio.signal.SpectrumChannel
import org.noiseplanet.noisecapture.audio.signal.get44100HZ
import org.noiseplanet.noisecapture.audio.signal.get48000HZ
import org.noiseplanet.noisecapture.audio.signal.window.SamplesWindowing
import org.noiseplanet.noisecapture.model.dao.LeqRecord
import org.noiseplanet.noisecapture.util.roundTo
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Calculates acoustic indicators from raw incoming audio samples.
 *
 * @param sampleRate Incoming audio data sample rate
 * @param compensationGain Gain compensation
 */
class AcousticIndicatorsProcessing(
    val sampleRate: Int,
    val compensationGain: Double,
) {

    // - Constants

    companion object {

        const val WINDOW_TIME_SECONDS = 0.125

        // https://source.android.com/docs/compatibility/12/android-12-cdd.pdf
        // Android 12
        // Last updated: October 4, 2021
        // SHOULD set audio input sensitivity such that a 1000 Hz sinusoidal tone source played at
        // 90 dB Sound Pressure Level (SPL) yields a response with RMS of 2500 for 16 bit-samples
        // (or -22.35 dB Full Scale for floating point/double precision samples) for each and every
        // microphone used to record the voice recognition audio source.
        // TODO: Make this platform dependent
        const val BASE_COMPENSATION_GAIN = -(-22.35 - 90)
    }


    // - Properties

    private val totalGain = BASE_COMPENSATION_GAIN + compensationGain

    private val samplesWindowing = SamplesWindowing(
        windowSize = (sampleRate * WINDOW_TIME_SECONDS).toInt(),
        memoryStrategy = SamplesWindowing.MemoryStrategy.BUFFER_REFERENCE,
    )
    private val spectrumChannel: SpectrumChannel = SpectrumChannel().apply {
        this.loadConfiguration(
            when (sampleRate) {
                48000 -> get48000HZ()
                else -> get44100HZ()
            }
        )
    }


    // - Public functions

    /**
     * Given a window of audio samples, calculate acoustic indicators (LEq, LAEq, LEq per frequency band...)
     *
     * @param audioSamples Incoming audio samples.
     * @return Processed acoustic indicators.
     */
    suspend fun processSamples(audioSamples: AudioSamples): List<LeqRecord> {
        val windows = samplesWindowing.pushSamples(audioSamples)

        return windows.map { audioSamples ->
            val rms = sqrt(
                audioSamples.samples.fold(0.0) { acc, sample ->
                    acc + sample * sample
                } / audioSamples.samples.size
            )
            val leq = totalGain + 20 * log10(rms)
            val laeq = totalGain + spectrumChannel.processSamplesWeightA(audioSamples.samples)

            val thirdOctave = spectrumChannel.processSamples(audioSamples.samples)
            val thirdOctaveGain = 10 * log10(10.0.pow(totalGain / 10.0) / thirdOctave.size)
            val leqsPerThirdOctave = spectrumChannel.getNominalFrequencies()
                .zip(thirdOctave.map {
                    // Clip values to -999dB to avoid -Inf in JSON exports
                    max(it + thirdOctaveGain, -999.0).roundTo(1)
                }).toMap()

            LeqRecord(
                timestamp = audioSamples.timestamp,
                // Clip values to -999dB to avoid -Inf in JSON exports
                lzeq = max(leq, -999.0).roundTo(1),
                laeq = max(laeq, -999.0).roundTo(1),
                leqsPerThirdOctave = leqsPerThirdOctave,
            )
        }
    }

    /**
     * Flushes internal data.
     */
    fun flush() {
        samplesWindowing.flush()
    }
}
