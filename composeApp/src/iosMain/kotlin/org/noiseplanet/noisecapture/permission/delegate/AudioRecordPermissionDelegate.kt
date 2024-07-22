package org.noiseplanet.noisecapture.permission.delegate

import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.openNSUrl
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted

internal class AudioRecordPermissionDelegate : PermissionDelegate {

    override fun getPermissionState(): PermissionState {
        return when (AVAudioSession.sharedInstance().recordPermission) {
            AVAudioSessionRecordPermissionGranted -> PermissionState.GRANTED
            AVAudioSessionRecordPermissionDenied -> PermissionState.DENIED
            else -> PermissionState.NOT_DETERMINED
        }
    }

    override fun providePermission() {
        AVAudioSession.sharedInstance().requestRecordPermission { granted ->
            // TODO: Fix logger on iOS
            println("Record permission granted: $granted")
        }
    }

    override fun openSettingPage() {
        openNSUrl("App-prefs:Privacy&path=MICROPHONE")
    }

}
