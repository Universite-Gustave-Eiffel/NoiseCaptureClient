package org.noiseplanet.noisecapture

import AndroidLogger
import AndroidPlatform
import Platform
import androidx.preference.PreferenceManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.Module
import org.koin.dsl.module
import org.noiseplanet.noisecapture.audio.AndroidAudioSource
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.player.AndroidAudioPlayer
import org.noiseplanet.noisecapture.audio.player.AudioPlayer
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.audio.AndroidAudioRecordingService
import org.noiseplanet.noisecapture.services.audio.AudioRecordingService
import org.noiseplanet.noisecapture.services.location.AndroidUserLocationProvider
import org.noiseplanet.noisecapture.services.location.UserLocationProvider
import org.noiseplanet.noisecapture.services.measurement.AndroidRecordingService
import org.noiseplanet.noisecapture.services.measurement.RecordingService

/**
 * Registers koin components specific to this platform
 */
val platformModule: Module = module {

    single<Platform> {
        AndroidPlatform()
    }

    factory<Logger> { params ->
        val tag: String? = params.values.firstOrNull() as? String
        AndroidLogger(tag)
    }

    factory<AudioSource> {
        AndroidAudioSource()
    }

    factory<UserLocationProvider> {
        AndroidUserLocationProvider()
    }

    single<Settings> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(get())
        SharedPreferencesSettings(sharedPreferences)
    }

    single<AudioRecordingService> {
        AndroidAudioRecordingService()
    }

    /**
     * Override the default [RecordingService] implementation with the one
     * wrapped into a foreground service.
     */
    single<RecordingService> {
        AndroidRecordingService()
    }

    factory<AudioPlayer> { (filePath: String) ->
        AndroidAudioPlayer(filePath)
    }
}
