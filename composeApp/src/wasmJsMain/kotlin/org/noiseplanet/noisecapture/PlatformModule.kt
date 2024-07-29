package org.noiseplanet.noisecapture

import org.koin.core.module.Module
import org.koin.dsl.module
import org.noiseplanet.noisecapture.audio.AudioSource
import org.noiseplanet.noisecapture.audio.JsAudioSource
import org.noiseplanet.noisecapture.log.Logger

val platformModule: Module = module {

    factory<Logger> { (tag: String) ->
        JSLogger(tag)
    }

    factory<AudioSource> { JsAudioSource() }
}
