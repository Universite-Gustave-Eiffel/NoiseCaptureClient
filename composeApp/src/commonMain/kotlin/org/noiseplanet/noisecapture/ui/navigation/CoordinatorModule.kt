package org.noiseplanet.noisecapture.ui.navigation


import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val coordinatorModule = module {

    viewModel {
        RootCoordinatorViewModel()
    }
}
