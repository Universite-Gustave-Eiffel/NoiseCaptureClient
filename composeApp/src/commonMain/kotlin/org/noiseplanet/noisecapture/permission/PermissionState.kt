package org.noiseplanet.noisecapture.permission


/**
 * Represents the state of a permission
 */
enum class PermissionState {

    /**
     * Indicates that the permission has not been requested yet
     */
    NOT_DETERMINED,

    /**
     * Indicates that the permission has been requested and accepted.
     */
    GRANTED,

    /**
     * No permission delegate is available for this permission
     * It has not been implemented or it is no required on this platform
     */
    NOT_IMPLEMENTED,

    /**
     * Indicates that the permission has been requested but the user denied the permission
     */
    DENIED;

    companion object
}
