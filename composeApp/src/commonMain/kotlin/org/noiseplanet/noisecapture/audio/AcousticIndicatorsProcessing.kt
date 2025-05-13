package org.noiseplanet.noisecapture.audio

import org.noiseplanet.noisecapture.audio.signal.SpectrumChannel
import org.noiseplanet.noisecapture.audio.signal.get44100HZ
import org.noiseplanet.noisecapture.audio.signal.get48000HZ
import org.noiseplanet.noisecapture.util.roundTo
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

const val WINDOW_TIME = 0.125

// https://source.android.com/docs/compatibility/12/android-12-cdd.pdf
// Android 12
// Last updated: October 4, 2021
// SHOULD set audio input sensitivity such that a 1000 Hz sinusoidal tone source played at
// 90 dB Sound Pressure Level (SPL) yields a response with RMS of 2500 for 16 bit-samples
// (or -22.35 dB Full Scale for floating point/double precision samples) for each and every
// microphone used to record the voice recognition audio source.
const val ANDROID_GAIN = -(-22.35 - 90)

/**
 * TODO: Document this class!
 */
class AcousticIndicatorsProcessing(val sampleRate: Int, val dbGain: Double = ANDROID_GAIN) {

    private var windowLength = (sampleRate * WINDOW_TIME).toInt()
    private var windowData = FloatArray(windowLength)
    private var windowDataCursor = 0
    private val nominalFrequencies: List<Int>

    private val spectrumChannel: SpectrumChannel = SpectrumChannel().apply {
        this.loadConfiguration(
            when (sampleRate) {
                48000 -> get48000HZ()
                else -> get44100HZ()
            }
        )
        nominalFrequencies = this.getNominalFrequency()
    }

    suspend fun processSamples(samples: AudioSamples): List<AcousticIndicatorsData> {
        val acousticIndicatorsDataList = ArrayList<AcousticIndicatorsData>()
        var samplesProcessed = 0
        while (samplesProcessed < samples.samples.size) {
            while (windowDataCursor < windowLength &&
                samplesProcessed < samples.samples.size
            ) {
                val remainingToProcess = min(
                    windowLength - windowDataCursor,
                    samples.samples.size - samplesProcessed
                )
                for (i in 0..<remainingToProcess) {
                    windowData[i + windowDataCursor] =
                        samples.samples[i + samplesProcessed]
                }
                windowDataCursor += remainingToProcess
                samplesProcessed += remainingToProcess
            }
            if (windowDataCursor == windowLength) {
                // window complete
                val rms =
                    sqrt(windowData.fold(0.0) { acc, sample ->
                        acc + sample * sample
                    } / windowData.size)
                val leq = dbGain + 20 * log10(rms)
                val laeq = dbGain + spectrumChannel.processSamplesWeightA(windowData)
                val thirdOctave = spectrumChannel.processSamples(windowData)
                val thirdOctaveGain = 10 * log10(10.0.pow(dbGain / 10.0) / thirdOctave.size)
                val leqsPerThirdOctave = nominalFrequencies
                    .zip(thirdOctave.map {
                        // Clip values to -999dB to avoid -Inf in JSON exports
                        max(it + thirdOctaveGain, -999.0).roundTo(1)
                    }).toMap()
                acousticIndicatorsDataList.add(
                    // TODO: Adapt this to directly return LeqRecords
                    AcousticIndicatorsData(
                        samples.epoch,
                        // Clip values to -999dB to avoid -Inf in JSON exports
                        max(leq, -999.0).roundTo(1),
                        max(laeq, -999.0).roundTo(1),
                        rms.roundTo(1),
                        leqsPerThirdOctave,
                    )
                )
                windowDataCursor = 0
            }
        }
        return acousticIndicatorsDataList
    }
}


data class AcousticIndicatorsData(
    val epoch: Long,
    val leq: Double,
    val laeq: Double,
    val rms: Double,
    val leqsPerThirdOctave: Map<Int, Double>,
)
