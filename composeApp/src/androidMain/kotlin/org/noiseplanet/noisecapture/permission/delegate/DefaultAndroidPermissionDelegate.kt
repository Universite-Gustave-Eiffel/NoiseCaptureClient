package org.noiseplanet.noisecapture.permission.delegate

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.reduce
import org.noiseplanet.noisecapture.permission.toAndroidPermissions
import org.noiseplanet.noisecapture.permission.util.PermissionRequestException
import org.noiseplanet.noisecapture.permission.util.checkPermissionState
import org.noiseplanet.noisecapture.permission.util.openAppSettingsPage
import org.noiseplanet.noisecapture.permission.util.providePermissions


internal open class DefaultAndroidPermissionDelegate(
    private val permission: Permission,
    private val context: Context,
    private val activity: Lazy<Activity>,
) : PermissionDelegate {

    // - Properties

    protected val permissionMutableSateFlow = MutableStateFlow(PermissionState.NOT_DETERMINED)
    override val permissionStateFlow: StateFlow<PermissionState> = permissionMutableSateFlow


    // - Lifecycle

    init {
        checkPermissionState()
    }


    // - Public functions

    override fun checkPermissionState() {
        val states = permission.toAndroidPermissions().map {
            activity.value.checkPermissionState(it)
        }
        permissionMutableSateFlow.tryEmit(states.reduce())
    }

    override fun providePermission() {
        activity.value.providePermissions(permission.toAndroidPermissions()) {
            throw PermissionRequestException(permission.name)
        }
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(permission)
    }
}
