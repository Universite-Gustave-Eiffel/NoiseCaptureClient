package org.noiseplanet.noisecapture

import IOSPlatform
import Platform
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.IOSAudioSource
import org.noiseplanet.noisecapture.audio.player.AudioPlayer
import org.noiseplanet.noisecapture.audio.player.IOSAudioPlayer
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.audio.AudioRecordingService
import org.noiseplanet.noisecapture.services.audio.IOSAudioRecordingService
import org.noiseplanet.noisecapture.services.location.IOSUserLocationProvider
import org.noiseplanet.noisecapture.services.location.UserLocationProvider
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import org.noiseplanet.noisecapture.services.storage.IOSFileSystemService
import platform.Foundation.NSBundle

/**
 * Registers koin components specific to this platform
 */
@OptIn(ExperimentalSettingsImplementation::class)
val platformModule: Module = module {

    single<Platform> {
        IOSPlatform()
    }

    factory<Logger> { params ->
        val tag: String? = params.values.firstOrNull() as? String
        IOSLogger(tag)
    }

    factory<AudioSource> {
        IOSAudioSource()
    }

    factory<UserLocationProvider> {
        IOSUserLocationProvider()
    }

    single<Settings> {
        NSBundle.mainBundle.bundleIdentifier
            ?.let { KeychainSettings(it) }
            ?: KeychainSettings()
    }

    single<AudioRecordingService> {
        IOSAudioRecordingService()
    }

    single<FileSystemService> {
        IOSFileSystemService()
    }

    factory<AudioPlayer> { (filePath: String) ->
        IOSAudioPlayer(filePath)
    }
}
