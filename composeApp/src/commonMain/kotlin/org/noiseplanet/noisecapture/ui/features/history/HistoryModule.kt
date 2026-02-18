package org.noiseplanet.noisecapture.ui.features.history

import org.koin.dsl.module
import org.noiseplanet.noisecapture.log.Logger

val historyModule = module {

    factory { (measurementId: String) ->
        val logger = get<Logger>()
        logger.debug("VIEWMODEL FOR ID: $measurementId")
        HistoryItemViewModel(measurementId)
    }
}
