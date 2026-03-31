package org.noiseplanet.noisecapture.permission.delegate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
        // Listen for user toggling location services on or off
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    checkPermissionState()
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        // Initialise with current status
        checkPermissionState()
    }


    // - Public functions

    override fun checkPermissionState() {
        val granted = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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
