package org.noiseplanet.noisecapture.permission.delegate

import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.CannotOpenSettingsException
import org.noiseplanet.noisecapture.permission.util.openPage


internal class LocationServicePermissionDelegate(
    private val context: Context,
    private val locationManager: LocationManager,
) : PermissionDelegate {

    // - Properties

    private val _permissionSateFlow = MutableStateFlow(PermissionState.NOT_DETERMINED)
    override val permissionStateFlow: StateFlow<PermissionState> = _permissionSateFlow


    // - Lifecycle

    init {
        checkPermissionState()
    }


    // - Public functions

    override fun checkPermissionState() {
        val granted = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val state = if (granted) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
        _permissionSateFlow.tryEmit(state)
    }


    // - Public functions

    override fun providePermission() {
        openSettingPage()
    }

    override fun canOpenSettings(): Boolean = true

    override fun openSettingPage() {
        context.openPage(
            action = Settings.ACTION_LOCATION_SOURCE_SETTINGS,
            onError = { throw CannotOpenSettingsException(Permission.LOCATION_SERVICE_ON.name) }
        )
    }
}
