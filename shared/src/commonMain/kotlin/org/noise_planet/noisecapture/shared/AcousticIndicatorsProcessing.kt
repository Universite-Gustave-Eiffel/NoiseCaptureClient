package org.noise_planet.noisecapture.shared

import kotlinx.coroutines.runBlocking
import org.noise_planet.noisecapture.AudioSamples
import org.noise_planet.noisecapture.shared.signal.SpectrumChannel
import org.noise_planet.noisecapture.shared.signal.get44100HZ
import org.noise_planet.noisecapture.shared.signal.get48000HZ
import kotlin.math.log10
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
const val ANDROID_GAIN = -(-22.35-90)

class AcousticIndicatorsProcessing(val sampleRate: Int, val dbGain : Double = ANDROID_GAIN) {
    private var windowLength = (sampleRate * WINDOW_TIME).toInt()
    private var windowData = FloatArray(windowLength)
    private var windowDataCursor = 0
    private val nominalFrequencies : List<Double>

    private val spectrumChannel: SpectrumChannel = SpectrumChannel().apply {
        this.loadConfiguration(
            when (sampleRate) {
                48000 -> get48000HZ()
                else -> get44100HZ()
            }
        )
        this@AcousticIndicatorsProcessing.nominalFrequencies = this.getNominalFrequency()
    }



    fun processSamples(samples: AudioSamples): List<AcousticIndicatorsData> {
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
                val rms = sqrt(windowData.fold(0.0) {acc, sample -> acc + sample*sample } / windowData.size)
                val leq = dbGain + 20 * log10(rms)
                val laeq = dbGain + spectrumChannel.processSamplesWeightA(windowData)
                //val thirdOctave = DoubleArray(0)
                runBlocking {
                    val thirdOctave = spectrumChannel.processSamples(windowData)
                    acousticIndicatorsDataList.add(
                        AcousticIndicatorsData(
                            samples.epoch,
                            leq,
                            laeq,
                            rms,
                            thirdOctave,
                            nominalFrequencies
                        )
                    )
                }
                windowDataCursor = 0
            }
        }
        return acousticIndicatorsDataList
    }
}

data class AcousticIndicatorsData(val epoch : Long, val leq: Double, val laeq: Double,
                                  val rms : Double, val thirdOctave : DoubleArray,
                                  val nominalFrequencies : List<Double>)

