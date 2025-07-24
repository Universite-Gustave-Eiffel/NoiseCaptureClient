package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.permission.DefaultPermissionDelegate
import org.noiseplanet.noisecapture.permission.Permission

internal class PersistentLocalStoragePermissionDelegate : DefaultPermissionDelegate(
    permission = Permission.PERSISTENT_LOCAL_STORAGE
) {

    // - Properties

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)


    // - PermissionDelegate

    override fun providePermission() {
        scope.launch {
            navigator?.storage?.persist()?.await<JsBoolean>()
        }
    }
}
