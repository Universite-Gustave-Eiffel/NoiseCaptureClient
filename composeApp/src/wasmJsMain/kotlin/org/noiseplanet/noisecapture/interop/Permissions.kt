@file:OptIn(ExperimentalWasmJsInterop::class)

package org.noiseplanet.noisecapture.interop

import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import kotlin.js.Promise

/**
 * The Permissions interface of the Permissions API provides the core Permission API functionality,
 * such as methods for querying and revoking permissions
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Permissions)
 */
external class Permissions {

    /**
     * The query() method of the Permissions interface returns the state of a user permission on
     * the global scope.
     * The user permission names are defined in the respective specifications for each feature.
     * The permissions supported by different browser versions are listed in the compatibility data
     * of the Permissions interface (see also the relevant source code for Firefox values,
     * Chromium values, and WebKit values).
     *
     * [MCN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Permissions/query)
     */
    fun query(permissionDescriptor: JsAny): Promise<PermissionStatus>
}


/**
 * The PermissionStatus interface of the Permissions API provides the state of an object and an
 * event handler for monitoring changes to said state.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/PermissionStatus)
 */
external class PermissionStatus : EventTarget {

    /**
     * Returns the name of a requested permission, identical to the name passed to Permissions.query.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/PermissionStatus/name)
     */
    val name: String

    /**
     * Returns the state of a requested permission; one of 'granted', 'denied', or 'prompt'.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/PermissionStatus/state)
     */
    val state: String

    /**
     * Invoked upon changes to PermissionStatus.state.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/PermissionStatus/change_event)
     */
    var onchange: (Event) -> Unit
}


/**
 * Utility enum to handle JS permission states
 */
internal enum class JsPermissionState(val value: String) {

    GRANTED("granted"),
    DENIED("denied"),
    PROMPT("prompt");

    companion object {

        fun fromValue(value: String): JsPermissionState? =
            JsPermissionState.entries.firstOrNull { it.value == value }
    }
}


/**
 * Maps JS permission states to internal NoiseCapture [PermissionState]
 */
internal fun JsPermissionState.toPermissionState(): PermissionState = when (this) {
    JsPermissionState.GRANTED -> PermissionState.GRANTED
    JsPermissionState.DENIED -> PermissionState.DENIED
    JsPermissionState.PROMPT -> PermissionState.NOT_DETERMINED
}


/**
 * Maps internal [Permission] to Js permission strings
 */
internal fun Permission.toJsPermission(): String = when (this) {
    Permission.LOCATION_BACKGROUND -> "geolocation"
    Permission.RECORD_AUDIO -> "microphone"
    Permission.PERSISTENT_LOCAL_STORAGE -> "persistent-storage"
    else -> ""
}


/**
 * Utility function for creating PermissionDescriptor
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Permissions/query#permissiondescriptor)
 */
@Suppress("UnusedParameter")
internal fun createPermissionDescriptor(
    name: String,
): JsAny = js(
    code = "({name: name})"
)
