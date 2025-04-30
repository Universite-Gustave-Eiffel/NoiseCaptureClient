package org.noiseplanet.noisecapture

import Platform
import WasmJSPlatform
import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import org.koin.core.module.Module
import org.koin.dsl.module
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.JsAudioSource
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.audio.AudioRecordingService
import org.noiseplanet.noisecapture.services.audio.AudioStorageService
import org.noiseplanet.noisecapture.services.audio.JSAudioRecordingService
import org.noiseplanet.noisecapture.services.audio.OPFSAudioStorageService
import org.noiseplanet.noisecapture.services.location.UserLocationProvider
import org.noiseplanet.noisecapture.services.location.WasmJSUserLocationProvider

val platformModule: Module = module {

    single<Platform> {
        WasmJSPlatform()
    }

    factory<Logger> { params ->
        val tag: String? = params.values.firstOrNull() as? String
        JSLogger(tag)
    }

    factory<AudioSource> {
        JsAudioSource()
    }

    factory<UserLocationProvider> {
        WasmJSUserLocationProvider()
    }

    single<Settings> {
        StorageSettings()
    }

    single<AudioRecordingService> {
        JSAudioRecordingService()
    }

    single<AudioStorageService> {
        OPFSAudioStorageService()
    }
}
