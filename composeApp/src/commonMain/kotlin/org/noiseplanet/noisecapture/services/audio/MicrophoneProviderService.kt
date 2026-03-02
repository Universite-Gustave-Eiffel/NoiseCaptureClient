package org.noiseplanet.noisecapture.services.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.MicrophoneCalibrationProfile
import org.noiseplanet.noisecapture.model.dao.MicrophoneInfo
import org.noiseplanet.noisecapture.model.dao.MicrophoneType
import org.noiseplanet.noisecapture.services.storage.StorageService
import org.noiseplanet.noisecapture.services.storage.injectStorageService
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.jvm.JvmStatic

/**
 * Cross platform interface that abstracts getting available input sources and letting the user
 * manually select their preferred microphone.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class MicrophoneProviderService : KoinComponent {

    // - Constants

    companion object {

        @JvmStatic
        protected val DEFAULT_MICROPHONE = MicrophoneInfo(
            id = "builtin",
            label = "Microphone",
            type = MicrophoneType.BUILTIN
        )
    }


    // - Properties

    protected val logger: Logger by injectLogger()
    private val calibrationService: StorageService<MicrophoneCalibrationProfile>
        by injectStorageService()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _availableInputs = MutableStateFlow<List<MicrophoneInfo>>(emptyList())
    private val _preferredInput = MutableStateFlow<MicrophoneInfo?>(null)

    /**
     * Holds the audio device currently selected by the user, or null if system device
     * should be used instead.
     */
    private var userSelectedDevice: MicrophoneInfo? = null

    /**
     * Lists all the currently available input sources.
     */
    val availableInputs: StateFlow<List<MicrophoneInfo>> = _availableInputs

    /**
     * Input device that should be used by [org.noiseplanet.noisecapture.audio.AudioSource]
     * if available.
     */
    val preferredInput: StateFlow<MicrophoneInfo?> = _preferredInput

    /**
     * Calibration profile of the currently used microphone, if found in local storage.
     */
    val currentCalibrationProfile: StateFlow<MicrophoneCalibrationProfile?> = _preferredInput
        .flatMapLatest {
            it?.let { mic ->
                if (calibrationService.get(mic.calibrationProfileIdentifier()) == null) {
                    calibrationService.set(
                        mic.calibrationProfileIdentifier(),
                        MicrophoneCalibrationProfile(mic),
                    )
                }
                calibrationService.subscribeOne(mic.calibrationProfileIdentifier())
            } ?: flowOf(null)
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )


    // - Public functions

    /**
     * Manually select an input source that should be used by [org.noiseplanet.noisecapture.audio.AudioSource].
     *
     * @param targetDevice Target microphone to be used as input source.
     */
    suspend fun setPreferredInput(targetDevice: MicrophoneInfo) {
        if (targetDevice.id !in _availableInputs.value.map { it.id }) {
            logger.warning("Trying to select unavailable input source: $targetDevice")
            return
        }
        userSelectedDevice = targetDevice
        refresh()
    }

    /**
     * Stores the given calibration profile.
     *
     * @param calibrationProfile Microphone calibration profile.
     */
    suspend fun saveCalibrationProfile(calibrationProfile: MicrophoneCalibrationProfile) {
        calibrationService.set(
            uuid = calibrationProfile.uuid,
            newValue = calibrationProfile
        )
    }


    // - Protected functions

    /**
     * Scans and parses currently available input sources.
     */
    protected abstract suspend fun getCurrentlyAvailableInputs(): List<MicrophoneInfo>

    protected abstract suspend fun getDefaultInput(): MicrophoneInfo?

    /**
     * Refreshes the currently available input sources, current active device and user selected
     * device if needed.
     */
    protected suspend fun refresh() {
        // Refresh the list of available devices
        val currentDevices = getCurrentlyAvailableInputs()
        _availableInputs.tryEmit(currentDevices)

        // If no device is available, set user device and active device to null
        if (currentDevices.isEmpty()) {
            userSelectedDevice = null
            _preferredInput.tryEmit(null)
        }

        // Get default input based on platform implementation
        val defaultInput = getDefaultInput() ?: DEFAULT_MICROPHONE

        userSelectedDevice?.id?.let { selectedDeviceId ->
            // If user manually selected an input device and it is not available anymore, switch to
            // the next default input source
            if (selectedDeviceId !in currentDevices.map { it.id }) {
                userSelectedDevice = null
                _preferredInput.tryEmit(defaultInput)
            } else if (selectedDeviceId != _preferredInput.value?.id) {
                // If available (and not already in use), use it as active input source
                _preferredInput.tryEmit(userSelectedDevice)
            }
        } ?: run {
            // If no input source was manually selected, use the default one
            _preferredInput.tryEmit(defaultInput)
        }
        // If manually selected input source is still available, don't change the active device
    }
}
