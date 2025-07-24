package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.openNSUrl
import org.noiseplanet.noisecapture.util.injectLogger
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted


internal class AudioRecordPermissionDelegate : PermissionDelegate, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()

    private val permissionMutableStateFlow = MutableStateFlow(PermissionState.NOT_DETERMINED)
    override val permissionStateFlow: StateFlow<PermissionState> = permissionMutableStateFlow


    // - Public functions

    override fun checkPermissionState() {
        // TODO: This property is marked deprecated for iOS 17+ but Kotlin interop still
        //       seems to be working fine. If needed, add a version check.
        // https://developer.apple.com/documentation/avfaudio/avaudiosession/recordpermission-swift.property
        val state = when (AVAudioSession.sharedInstance().recordPermission) {
            AVAudioSessionRecordPermissionGranted -> PermissionState.GRANTED
            AVAudioSessionRecordPermissionDenied -> PermissionState.DENIED
            else -> PermissionState.NOT_DETERMINED
        }
        permissionMutableStateFlow.tryEmit(state)
    }

    override fun providePermission() {
        // TODO: This method is marked deprecated for iOS 17+ but Kotlin interop still
        //       seems to be working fine. If needed, add a version check.
        // https://developer.apple.com/documentation/avfaudio/avaudiosession/requestrecordpermission(_:)
        AVAudioSession.sharedInstance().requestRecordPermission { granted ->
            logger.debug("Record permission granted: $granted")
        }
    }

    override fun canOpenSettings(): Boolean = true

    override fun openSettingPage() {
        openNSUrl("App-prefs:Privacy&path=MICROPHONE")
    }
}
