package org.noiseplanet.noisecapture.permission

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noiseplanet.noisecapture.permission.delegate.AudioRecordPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.LocationBackgroundPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.PersistentLocalStoragePermissionDelegate

internal actual fun platformPermissionModule(): Module = module {

    single<PermissionDelegate>(named(Permission.RECORD_AUDIO.name)) {
        AudioRecordPermissionDelegate()
    }

    single<PermissionDelegate>(named(Permission.LOCATION_BACKGROUND.name)) {
        LocationBackgroundPermissionDelegate()
    }

    single<PermissionDelegate>(named(Permission.PERSISTENT_LOCAL_STORAGE.name)) {
        PersistentLocalStoragePermissionDelegate()
    }
}
