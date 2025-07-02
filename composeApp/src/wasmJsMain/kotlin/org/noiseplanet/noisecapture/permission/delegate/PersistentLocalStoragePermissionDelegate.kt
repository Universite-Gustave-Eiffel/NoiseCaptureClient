package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.coroutines.await
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.permission.PermissionState

class PersistentLocalStoragePermissionDelegate : PermissionDelegate {

    // - Properties

    private var permissionState = PermissionState.NOT_DETERMINED


    // - PermissionDelegate

    override suspend fun getPermissionState(): PermissionState {
        if (permissionState == PermissionState.GRANTED) {
            return permissionState
        }
        val storage = navigator?.storage ?: return PermissionState.NOT_DETERMINED

        return if (storage.persisted().await<JsBoolean>().toBoolean()) {
            PermissionState.GRANTED
        } else {
            PermissionState.NOT_DETERMINED
        }
    }

    override suspend fun providePermission() {
        val storage = navigator?.storage ?: return

        permissionState = if (storage.persist().await<JsBoolean>().toBoolean()) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
    }

    override fun openSettingPage() {
        // TODO: Is there a common way to open browser settings?
        //       Should we just show an alert/popup inquiring users to manually grant permission?
    }
}
