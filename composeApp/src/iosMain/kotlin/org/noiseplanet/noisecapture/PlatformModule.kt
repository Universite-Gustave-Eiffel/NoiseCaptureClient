package org.noiseplanet.noisecapture

import org.koin.core.module.Module
import org.koin.dsl.module
import org.noiseplanet.noisecapture.log.Logger

/**
 * Registers koin components specific to this platform
 */
val platformModule: Module = module {
    
    factory<Logger> { params ->
        val tag: String? = params.values.firstOrNull() as? String
        IOSLogger(tag)
    }
}
