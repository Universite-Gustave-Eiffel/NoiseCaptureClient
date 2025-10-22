package org.noiseplanet.noisecapture.ui.navigation


import kotlinx.coroutines.flow.SharedFlow
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.features.permission.PermissionPrompt
import org.noiseplanet.noisecapture.ui.features.permission.RequestPermissionModalViewModel
import org.noiseplanet.noisecapture.util.MockMeasurementBuilder


val coordinatorModule = module {

    viewModel {
        RootCoordinatorViewModel()
    }

    viewModel { (permissionPromptFlow: SharedFlow<PermissionPrompt?>) ->
        RequestPermissionModalViewModel(permissionPromptFlow)
    }

    single<MockMeasurementBuilder> {
        MockMeasurementBuilder(measurementService = get())
    }
}
