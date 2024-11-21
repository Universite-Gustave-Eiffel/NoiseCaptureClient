package org.noiseplanet.noisecapture

import AndroidLogger
import androidx.preference.PreferenceManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.noiseplanet.noisecapture.audio.AndroidAudioSource
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.log.Logger

/**
 * Registers koin components specific to this platform
 */
val platformModule: Module = module {

    factory<Logger> { params ->
        val tag: String? = params.values.firstOrNull() as? String
        AndroidLogger(tag)
    }

    factory<AudioSource> {
        AndroidAudioSource(logger = get {
            parametersOf("AudioSource")
        })
    }

    single<Settings> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(get())
        SharedPreferencesSettings(sharedPreferences)
    }
}
