package org.noiseplanet.noisecapture

import AndroidLogger

import org.koin.core.module.Module
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

    factory<AudioSource> { AndroidAudioSource(logger = get()) }
}
