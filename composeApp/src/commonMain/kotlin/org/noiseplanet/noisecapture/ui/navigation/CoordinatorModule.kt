package org.noiseplanet.noisecapture.ui.navigation


import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.features.permission.RequestPermissionModalViewModel


val coordinatorModule = module {

    viewModel {
        RootCoordinatorViewModel()
    }

    viewModel { (permission: Permission) ->
        RequestPermissionModalViewModel(permission)
    }
}
