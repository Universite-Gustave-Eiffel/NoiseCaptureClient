package org.noiseplanet.noisecapture.permission

/**
 * This enum represents the permissions used in the application.
 * It provides constant values for various permissions related to system services and features.
 */
enum class Permission {

    /**
     * Indicates that the system setting bluetooth service is on.
     */
    BLUETOOTH_SERVICE_ON,

    /**
     * App bluetooth permission.
     */
    BLUETOOTH,

    /**
     * Indicates that the system setting location service is on.
     */
    LOCATION_SERVICE_ON,

    /**
     * App location fine permission.
     */
    LOCATION_FOREGROUND,

    /**
     * App location background permission.
     */
    LOCATION_BACKGROUND,

    /**
     * App audio recording permission.
     */
    RECORD_AUDIO,

    /**
     * Permission to send notifications.
     */
    POST_NOTIFICATIONS,

    /**
     * Permission to persistently store data in local storage.
     * Required to store files persistently using Origin Private FileSystem (OPFS) on web.
     */
    PERSISTENT_LOCAL_STORAGE,
}
