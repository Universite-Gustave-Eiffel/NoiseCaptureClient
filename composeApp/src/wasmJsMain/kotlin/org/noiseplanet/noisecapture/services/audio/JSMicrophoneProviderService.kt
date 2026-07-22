package org.noiseplanet.noisecapture.services.audio

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.MicrophoneInfo
import org.noiseplanet.noisecapture.model.dao.MicrophoneType
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.w3c.dom.mediacapture.AUDIOINPUT
import org.w3c.dom.mediacapture.MediaDeviceKind
import org.w3c.dom.mediacapture.MediaStreamConstraints

@OptIn(ExperimentalWasmJsInterop::class)
class JSMicrophoneProviderService : MicrophoneProviderService(), KoinComponent {

    // - Properties

    private val permissionService: PermissionService by inject()

    private val scope = CoroutineScope(Dispatchers.Default)


    // - Lifecycle

    init {
        window.navigator.mediaDevices.ondevicechange = {
            // Refresh available input sources whenever available devices change
            scope.launch { refresh() }
        }

        scope.launch {
            permissionService.getPermissionStateFlow(permission = Permission.RECORD_AUDIO)
                .collect { permissionState ->
                    // Refresh available input sources whenever audio record permission is granted
                    if (permissionState == PermissionState.GRANTED) {
                        refresh()
                    }
                }
        }
    }


    // - Protected functions

    override suspend fun getCurrentlyAvailableInputs(): List<MicrophoneInfo> {
        val permissionState = permissionService.getPermissionState(Permission.RECORD_AUDIO)
        if (permissionState != PermissionState.GRANTED) {
            logger.warning("Audio record permission not granted")
            return emptyList()
        }

        window.navigator.mediaDevices
            .getUserMedia(MediaStreamConstraints(audio = true.toJsBoolean()))
            .await()

        return window.navigator.mediaDevices.enumerateDevices()
            .await()
            .toList()
            .filter { it.kind == MediaDeviceKind.Companion.AUDIOINPUT }
            .map {
                MicrophoneInfo(
                    id = it.deviceId,
                    label = it.label,
                    type = MicrophoneType.UNKNOWN
                )
            }
            .ifEmpty { listOf(DEFAULT_MICROPHONE) }
    }

    override suspend fun getDefaultInput(): MicrophoneInfo? {
        val mediaStream = window.navigator.mediaDevices
            .getUserMedia(MediaStreamConstraints(audio = true.toJsBoolean()))
            .await()
        val audioTrack = mediaStream.getAudioTracks().toList().firstOrNull() ?: return null

        return availableInputs.value.firstOrNull {
            it.label == audioTrack.label
        }
    }
}
