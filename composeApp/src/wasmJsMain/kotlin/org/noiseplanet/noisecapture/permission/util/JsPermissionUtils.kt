package org.noiseplanet.noisecapture.permission.util

import kotlinx.coroutines.await
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import kotlin.js.Promise
import kotlin.reflect.KClass

/**
 * Checks the current status of a given permission using the
 * [Permissions API](https://developer.mozilla.org/en-US/docs/Web/API/Permissions_API)
 *
 * @param permission Target permission
 */
internal suspend fun checkPermission(permission: Permission): PermissionState {
    val result = permissionsAPIQuery(permission.jsName).await<JsString>()
    return PermissionState::class.fromJsState(result)
}

/**
 * Checks the status of the given permission using the permissions API.
 *
 * If an error is thrown (e.g. when the browser doesn't support checking the given permission),
 * we default to "prompt", which is is the equivalent of [PermissionState.NOT_DETERMINED].
 *
 * @param permissionName: Permission name
 * @return A promise with the result of the query
 */
@JsFun(
    code = """
    async (permissionName) => { 
        let result = await navigator.permissions.query({ name: permissionName })
            .catch((error) => {
                console.warn(error)
                return { state: "prompt" }
            })
        return result.state
    }
    """
)
private external fun permissionsAPIQuery(permissionName: JsString): Promise<JsString>

/**
 * Maps the internal permission enum to values used in
 * [Permission API](https://developer.mozilla.org/en-US/docs/Web/API/Permissions)
 */
private val Permission.jsName: JsString
    get() {
        return when (this) {
            Permission.LOCATION_BACKGROUND, Permission.LOCATION_FOREGROUND -> "geolocation"
            Permission.RECORD_AUDIO -> "microphone"
            else -> "_unsupported"
        }.toJsString()
    }

/**
 * Builds an internal [PermissionState] object from a JS
 * [PermissionStatus](https://developer.mozilla.org/en-US/docs/Web/API/PermissionStatus)
 *
 * @param state Raw JS permission status
 * @return [PermissionState] equivalent
 */
private fun KClass<PermissionState>.fromJsState(state: JsString): PermissionState {
    return when (state.toString()) {
        "granted" -> PermissionState.GRANTED
        "denied" -> PermissionState.DENIED
        "prompt" -> PermissionState.NOT_DETERMINED
        else -> PermissionState.NOT_IMPLEMENTED
    }
}
