package org.noiseplanet.noisecapture

import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.JsAudioSource
import org.noiseplanet.noisecapture.log.Logger

val platformModule: Module = module {

    factory<Logger> { params ->
        val tag: String? = params.values.firstOrNull() as? String
        JSLogger(tag)
    }

    factory<AudioSource> {
        JsAudioSource(logger = get {
            parametersOf("AudioSource")
        })
    }
}
