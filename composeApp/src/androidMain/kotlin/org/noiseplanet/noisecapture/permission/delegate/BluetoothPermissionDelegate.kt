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

internal class BluetoothPermissionDelegate(
    private val context: Context,
    private val activity: Lazy<Activity>,
) : PermissionDelegate {

    override suspend fun getPermissionState(): PermissionState {
        return activity.value.checkPermissions(bluetoothPermissions)
    }

    override suspend fun providePermission() {
        activity.value.providePermissions(bluetoothPermissions) {
            throw PermissionRequestException(Permission.BLUETOOTH.name)
        }
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(Permission.BLUETOOTH)
    }
}

private val bluetoothPermissions: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else listOf(Manifest.permission.BLUETOOTH)
