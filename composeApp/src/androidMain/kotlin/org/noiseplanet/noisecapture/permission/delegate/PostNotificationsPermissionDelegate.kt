package org.noiseplanet.noisecapture.permission.delegate

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.PermissionRequestException
import org.noiseplanet.noisecapture.permission.util.checkPermissions
import org.noiseplanet.noisecapture.permission.util.openAppSettingsPage
import org.noiseplanet.noisecapture.permission.util.providePermissions

internal class PostNotificationsPermissionDelegate(
    private val context: Context,
    private val activity: Lazy<Activity>,
) : PermissionDelegate {

    override suspend fun getPermissionState(): PermissionState {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return activity.value.checkPermissions(postNotificationPermissions)
        }
        return PermissionState.GRANTED
    }

    override suspend fun providePermission() {
        activity.value.providePermissions(postNotificationPermissions) {
            throw PermissionRequestException(Permission.POST_NOTIFICATIONS.name)
        }
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(Permission.POST_NOTIFICATIONS)
    }
}

private val postNotificationPermissions: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyList()
    }
