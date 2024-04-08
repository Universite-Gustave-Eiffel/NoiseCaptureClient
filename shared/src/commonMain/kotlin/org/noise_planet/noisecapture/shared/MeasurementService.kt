package org.noise_planet.noisecapture.shared

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.koin.core.logger.Logger
import org.noise_planet.noisecapture.AudioSource
import org.noise_planet.noisecapture.shared.signal.SpectrumData
import org.noise_planet.noisecapture.shared.signal.WindowAnalysis
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

typealias AcousticIndicatorsCallback = (acousticIndicatorsData: AcousticIndicatorsData) -> Unit
typealias SpectrumDataCallback = (spectrumData: SpectrumData) -> Unit

const val FFT_SIZE = 4096
const val FFT_HOP = 2048

class MeasurementService(private val audioSource: AudioSource, private val logger: Logger) {
    var storageObservers = mutableListOf<(property: KProperty<*>, oldValue: Boolean, newValue: Boolean) -> Unit>()
    private var storageActivated : Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        storageObservers.forEach {
            it(property, oldValue, newValue)
        }
    }
    private var acousticIndicatorsProcessing : AcousticIndicatorsProcessing? = null
    private var fftTool : WindowAnalysis? = null
    private var onAcousticIndicatorsData: AcousticIndicatorsCallback? = null
    private var onSpectrumData: SpectrumDataCallback? = null
    private var audioJob : Job? = null
    private var dbGain = 105.0


    @OptIn(DelicateCoroutinesApi::class)
    private fun startAudioRecord() {
        if(audioJob?.isActive == true) {
            return
        }
        audioJob = GlobalScope.launch {
            audioSource.setup().collect {audioSamples ->
                if(onSpectrumData != null) {
                    if(fftTool == null) {
                        fftTool = WindowAnalysis(audioSamples.sampleRate, FFT_SIZE, FFT_HOP)
                    }
                    fftTool?.pushSamples(audioSamples.epoch, audioSamples.samples)?.forEach { spectrumData ->
                        onSpectrumData?.let { callback -> callback(spectrumData) }
                    }
                }
                if(onAcousticIndicatorsData != null || storageActivated) {
                    if(acousticIndicatorsProcessing == null) {
                        acousticIndicatorsProcessing = AcousticIndicatorsProcessing(
                            audioSamples.sampleRate, dbGain)
                    }
                    acousticIndicatorsProcessing?.processSamples(audioSamples)?.forEach {
                        acousticIndicators ->
                        if(onAcousticIndicatorsData != null) {
                            onAcousticIndicatorsData?.let { callback -> callback(acousticIndicators) }
                        }
                        if(storageActivated) {

                        }
                    }
                }
            }
        }
    }

    private fun stopAudioRecord() {
        audioJob?.cancel()
        audioSource.release()
    }

    /**
     * Start collecting measurements to be forwarded to observers
     */
    fun collectAudioIndicators() : Flow<AcousticIndicatorsData> = callbackFlow {
        setAudioIndicatorsObserver { trySend(it) }
        awaitClose {
            resetAudioIndicatorsObserver()
        }
    }

    fun collectSpectrumData() : Flow<SpectrumData> = callbackFlow {
        setSpectrumDataObserver { trySend(it) }
        awaitClose {
            resetSpectrumDataObserver()
        }
    }

    fun setSpectrumDataObserver(onSpectrumData : SpectrumDataCallback) {
        this.onSpectrumData = onSpectrumData
        startAudioRecord()
    }

    fun canReleaseAudio() : Boolean {
        return !storageActivated &&
                onAcousticIndicatorsData == null &&
                onAcousticIndicatorsData == null
    }

    fun resetSpectrumDataObserver() {
        onSpectrumData = null
        if(canReleaseAudio()) {
            stopAudioRecord()
        }
    }

    fun setAudioIndicatorsObserver(onAcousticIndicatorsData: AcousticIndicatorsCallback) {
        startAudioRecord()
        this.onAcousticIndicatorsData = onAcousticIndicatorsData
    }

    fun resetAudioIndicatorsObserver() {
        onAcousticIndicatorsData = null
        if(canReleaseAudio()) {
            stopAudioRecord()
        }
    }

    /**
     * Store measurements in database
     */
    fun startStorage() {
        storageActivated = true
    }

    fun stopStorage() {
        storageActivated = false
    }

}
