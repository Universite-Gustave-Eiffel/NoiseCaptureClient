package org.noiseplanet.noisecapture.ui.features.permission

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.features.permission.stateview.PermissionStateViewModel

val requestPermissionModule = module {

    viewModel { (permission: Permission) ->
        PermissionStateViewModel(
            permission = permission
        )
    }
}
