package org.noiseplanet.noisecapture.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject


class LocationManager {

    // - Properties

    private val clLocationManager = CLLocationManager()
    private val delegate = CLLocationManagerDelegate(onPermissionStateChange = {
        refreshStatus()
    })

    private val _authorizationStatusFlow = MutableStateFlow(kCLAuthorizationStatusNotDetermined)
    val authorizationStatusFlow: StateFlow<CLAuthorizationStatus> = _authorizationStatusFlow

    val locationServicesEnabled: Boolean
        get() = authorizationStatusFlow.value != kCLAuthorizationStatusDenied


    // - Lifecycle

    init {
        clLocationManager.delegate = delegate
        refreshStatus()
    }


    // - Public functions

    fun refreshStatus() {
        _authorizationStatusFlow.tryEmit(clLocationManager.authorizationStatus)
    }

    fun requestForegroundAuthorization() {
        clLocationManager.requestWhenInUseAuthorization()
    }

    fun requestBackgroundAuthorization() {
        clLocationManager.requestAlwaysAuthorization()
    }
}


private class CLLocationManagerDelegate(
    private val onPermissionStateChange: () -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onPermissionStateChange()
    }
}
