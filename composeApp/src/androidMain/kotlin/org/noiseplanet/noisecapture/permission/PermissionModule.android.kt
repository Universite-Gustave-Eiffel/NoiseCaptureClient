package org.noiseplanet.noisecapture.permission

import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noiseplanet.noisecapture.permission.delegate.AudioRecordPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.BluetoothPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.BluetoothServicePermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.LocationBackgroundPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.LocationForegroundPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.LocationServicePermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.PostNotificationsPermissionDelegate

internal actual fun platformPermissionModule(): Module = module {
    single<PermissionDelegate>(named(Permission.BLUETOOTH_SERVICE_ON.name)) {
        BluetoothServicePermissionDelegate(
            context = get(),
            bluetoothAdapter = get(),
        )
    }
    single<PermissionDelegate>(named(Permission.BLUETOOTH.name)) {
        BluetoothPermissionDelegate(
            context = get(),
            activity = inject(),
        )
    }
    single {
        get<Context>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    single {
        get<BluetoothManager>().adapter
    }
    single {
        get<Context>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    single<PermissionDelegate>(named(Permission.LOCATION_SERVICE_ON.name)) {
        LocationServicePermissionDelegate(
            context = get(),
            locationManager = get(),
        )
    }
    single<PermissionDelegate>(named(Permission.LOCATION_FOREGROUND.name)) {
        LocationForegroundPermissionDelegate(
            context = get(),
            activity = inject(),
        )
    }
    single<PermissionDelegate>(named(Permission.LOCATION_BACKGROUND.name)) {
        LocationBackgroundPermissionDelegate(
            context = get(),
            activity = inject(),
            locationForegroundPermissionDelegate = get(named(Permission.LOCATION_FOREGROUND.name)),
        )
    }
    single<PermissionDelegate>(named(Permission.RECORD_AUDIO.name)) {
        AudioRecordPermissionDelegate(
            context = get(),
            activity = inject(),
        )
    }
    single<PermissionDelegate>(named(Permission.POST_NOTIFICATIONS.name)) {
        PostNotificationsPermissionDelegate(
            context = get(),
            activity = inject(),
        )
    }
}
