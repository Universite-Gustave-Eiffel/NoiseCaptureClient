package org.noiseplanet.noisecapture.ui.navigation

import Platform
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.MeasurementRecordingService
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


@OptIn(ExperimentalCoroutinesApi::class)
class RootCoordinatorViewModel : ViewModel(), KoinComponent {

    // - Associated types

    data class PermissionPrompt(
        val permission: Permission,
        val isRequired: Boolean,
    )


    // - Properties

    private val liveAudioService: LiveAudioService by inject()
    private val measurementRecordingService: MeasurementRecordingService by inject()
    private val permissionService: PermissionService by inject()
    private val platform: Platform by inject()

    private val currentRouteFlow: MutableStateFlow<Route?> = MutableStateFlow(null)

    /**
     * Keep track of optional permissions that were already prompted to the user during this
     * session to avoid spamming with permission request popups.
     */
    private var alreadyPromptedPermissionsForCurrentSession = mutableListOf<Permission>()

    /**
     * While navigating through the app, check for required permissions for the current route.
     * Prompt user for permission request if necessary.
     */
    val permissionPrompt: StateFlow<PermissionPrompt?> = currentRouteFlow
        .filterNotNull()
        .flatMapLatest { route ->
            // Get all required or optional permissions for the current route,
            // required permissions come first in the list, then optional.
            val requiredPermissions = platform.requiredPermissions[route.id] ?: emptyList()
            val optionalPermissions = platform.optionalPermissions[route.id] ?: emptyList()
            val routePermissions = requiredPermissions + optionalPermissions
            val routePermissionStates = routePermissions.associateWith { permission ->
                permissionService.getPermissionStateFlow(permission)
            }

            // Transform all permission states to a flow that will emit the first required or
            // optional permission that is not satisfied by the current screen.
            combine(routePermissionStates.values.toList()) { states ->
                // Get the non granted permission with the highest priority.
                val permission = routePermissionStates.keys.firstOrNull { permission ->
                    val index = routePermissionStates.keys.indexOf(permission)
                    val isGranted = states[index] == PermissionState.GRANTED
                    val alreadyPrompted = alreadyPromptedPermissionsForCurrentSession
                        .contains(permission)

                    !isGranted && !alreadyPrompted
                }
                permission?.let {
                    val isRequired = requiredPermissions.contains(it)
                    if (!isRequired) {
                        // If permission is optional, keep track that we've already prompted
                        // it to the user for this screen to avoid asking again.
                        alreadyPromptedPermissionsForCurrentSession.add(it)
                    }
                    PermissionPrompt(it, isRequired)
                }
            }
        }
        .distinctUntilChanged()
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = null,
        )

    val isRecording: Boolean
        get() = measurementRecordingService.isRecording


    // - Public functions

    fun setupAudioSource() = liveAudioService.setupAudioSource()
    fun releaseAudioSource() = liveAudioService.releaseAudioSource()

    /**
     * If there is no ongoing measurement recording, resume audio source
     */
    fun startAudioSourceIfNotRecording() {
        if (!isRecording) {
            liveAudioService.startListening()
        }
    }

    /**
     * If there is no ongoing measurement recording, pause audio source
     */
    fun stopAudioSourceIfNotRecording() {
        if (!isRecording) {
            liveAudioService.stopListening()
        }
    }

    fun endRecording() = measurementRecordingService.endAndSave()

    /**
     * Updates the current route.
     * Used to trigger some generic but screen specific side effects, like asking for
     * required permissions if necessary.
     */
    fun setCurrentRoute(route: Route) {
        if (currentRouteFlow.value != route) {
            toggleAudioSourceForScreen(route)
            currentRouteFlow.tryEmit(route)
        }
    }

    /**
     * Updates the current state of each app permission.
     */
    fun refreshPermissionStates() {
        Permission.entries.forEach {
            permissionService.refreshPermissionState(it)
        }
    }


    // - Private functions

    /**
     * Given a route name, checks if the corresponding screen uses audio source.
     * If true, resume audio source if there is no ongoing measurement.
     * If false, pause audio source if there is no ongoing measurement.
     */
    private fun toggleAudioSourceForScreen(route: Route) {
        if (route.usesAudioSource) {
            startAudioSourceIfNotRecording()
        } else {
            stopAudioSourceIfNotRecording()
        }
    }
}
