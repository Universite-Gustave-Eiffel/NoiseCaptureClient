package org.noiseplanet.noisecapture.permission

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noiseplanet.noisecapture.permission.delegate.AudioRecordPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate

internal actual fun platformPermissionModule(): Module = module {
    single<PermissionDelegate>(named(Permission.RECORD_AUDIO.name)) {
        AudioRecordPermissionDelegate()
    }
}
