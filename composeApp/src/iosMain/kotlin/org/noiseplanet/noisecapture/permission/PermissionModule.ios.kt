package org.noiseplanet.noisecapture.permission

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noiseplanet.noisecapture.permission.delegate.BluetoothPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.BluetoothServicePermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.LocationBackgroundPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.LocationForegroundPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.LocationServicePermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate

internal actual fun platformPermissionModule(): Module = module {
    single<PermissionDelegate>(named(Permission.BLUETOOTH_SERVICE_ON.name)) {
        BluetoothServicePermissionDelegate()
    }
    single<PermissionDelegate>(named(Permission.BLUETOOTH.name)) {
        BluetoothPermissionDelegate()
    }
    single<PermissionDelegate>(named(Permission.LOCATION_SERVICE_ON.name)) {
        LocationServicePermissionDelegate()
    }
    single<PermissionDelegate>(named(Permission.LOCATION_FOREGROUND.name)) {
        LocationForegroundPermissionDelegate()
    }
    single<PermissionDelegate>(named(Permission.LOCATION_BACKGROUND.name)) {
        LocationBackgroundPermissionDelegate(
            locationForegroundPermissionDelegate = get(named(Permission.LOCATION_FOREGROUND.name)),
        )
    }
}
