package org.noiseplanet.noisecapture.permission.delegate

import android.Manifest
import android.app.Activity
import android.content.Context
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.PermissionRequestException
import org.noiseplanet.noisecapture.permission.util.checkPermissions
import org.noiseplanet.noisecapture.permission.util.openAppSettingsPage
import org.noiseplanet.noisecapture.permission.util.providePermissions

internal class AudioRecordPermissionDelegate(
    private val context: Context,
    private val activity: Lazy<Activity>,
) : PermissionDelegate {

    override fun getPermissionState(): PermissionState {
        return checkPermissions(context, audioRecordPermissions)
    }

    override fun providePermission() {
        activity.value.providePermissions(audioRecordPermissions) {
            throw PermissionRequestException(Permission.RECORD_AUDIO.name)
        }
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(Permission.RECORD_AUDIO)
    }
}


private val audioRecordPermissions: List<String> =
    listOf(Manifest.permission.RECORD_AUDIO)
