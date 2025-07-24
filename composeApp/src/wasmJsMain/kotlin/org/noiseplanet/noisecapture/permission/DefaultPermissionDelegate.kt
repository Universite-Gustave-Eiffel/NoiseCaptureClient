package org.noiseplanet.noisecapture.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.interop.JsPermissionState
import org.noiseplanet.noisecapture.interop.PermissionStatus
import org.noiseplanet.noisecapture.interop.createPermissionDescriptor
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.interop.toJsPermission
import org.noiseplanet.noisecapture.interop.toPermissionState
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate
import org.noiseplanet.noisecapture.util.injectLogger


internal abstract class DefaultPermissionDelegate(
    permission: Permission,
) : PermissionDelegate, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()

    private val _permissionStateFlow = MutableStateFlow(PermissionState.NOT_DETERMINED)
    override val permissionStateFlow: StateFlow<PermissionState> = _permissionStateFlow


    // - Lifecycle

    init {
        // Listen to permission status updates
        navigator?.permissions
            ?.query(createPermissionDescriptor(permission.toJsPermission()))
            ?.then { permissionStatus ->
                // Handle initial value
                handlePermissionStatus(permissionStatus)

                logger.warning("GOT INITIAL STATE: ${permissionStatus.state}")

                permissionStatus.onchange = { _ ->
                    logger.warning("GOT NEW STATE: ${permissionStatus.state}")
                    // Handle subsequent updates
                    handlePermissionStatus(permissionStatus)
                }

                permissionStatus
            }
    }


    // - Public functions

    override fun checkPermissionState() {
        // No need for manual updates, new values will come through registered event listener.
    }

    override fun canOpenSettings(): Boolean = false

    override fun openSettingPage() {
        // For now we just hide the settings button.
        // TODO: Show a popup to the user?
    }


    // - Private functions

    private fun handlePermissionStatus(permissionStatus: PermissionStatus) {
        JsPermissionState.fromValue(permissionStatus.state)?.toPermissionState()?.let {
            _permissionStateFlow.tryEmit(it)
        }
    }
}
