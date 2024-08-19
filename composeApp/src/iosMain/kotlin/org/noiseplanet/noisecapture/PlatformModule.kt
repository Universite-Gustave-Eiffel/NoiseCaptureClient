package org.noiseplanet.noisecapture

import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.IOSAudioSource
import org.noiseplanet.noisecapture.log.Logger

/**
 * Registers koin components specific to this platform
 */
val platformModule: Module = module {

    factory<Logger> { params ->
        val tag: String? = params.values.firstOrNull() as? String
        IOSLogger(tag)
    }

    factory<AudioSource> {
        IOSAudioSource(logger = get {
            parametersOf("AudioSource")
        })
    }
}
