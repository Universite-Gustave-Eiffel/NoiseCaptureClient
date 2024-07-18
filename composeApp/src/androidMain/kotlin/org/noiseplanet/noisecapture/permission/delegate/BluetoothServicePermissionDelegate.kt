package org.noiseplanet.noisecapture.permission.delegate

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.provider.Settings
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.CannotOpenSettingsException
import org.noiseplanet.noisecapture.permission.util.openPage

internal class BluetoothServicePermissionDelegate(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
) : PermissionDelegate {

    override fun getPermissionState(): PermissionState {
        return if (bluetoothAdapter?.isEnabled == true)
            PermissionState.GRANTED else PermissionState.DENIED
    }

    override fun providePermission() {
        openSettingPage()
    }

    override fun openSettingPage() {
        context.openPage(
            action = Settings.ACTION_BLUETOOTH_SETTINGS,
            onError = { throw CannotOpenSettingsException(Permission.BLUETOOTH_SERVICE_ON.name) }
        )
    }
}
