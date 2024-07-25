package org.noiseplanet.noisecapture.permission.delegate

import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.openNSUrl
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBManagerAuthorizationAllowedAlways
import platform.CoreBluetooth.CBManagerAuthorizationDenied
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerAuthorizationRestricted

internal class BluetoothPermissionDelegate : PermissionDelegate {

    override suspend fun getPermissionState(): PermissionState {
        return when (CBCentralManager.authorization) {
            CBManagerAuthorizationNotDetermined -> PermissionState.NOT_DETERMINED
            CBManagerAuthorizationAllowedAlways, CBManagerAuthorizationRestricted -> PermissionState.GRANTED
            CBManagerAuthorizationDenied -> PermissionState.DENIED
            else -> PermissionState.NOT_DETERMINED
        }
    }

    override suspend fun providePermission() {
        CBCentralManager().authorization()
    }

    override fun openSettingPage() {
        openNSUrl("App-Prefs:Privacy&path=BLUETOOTH")
    }
}
