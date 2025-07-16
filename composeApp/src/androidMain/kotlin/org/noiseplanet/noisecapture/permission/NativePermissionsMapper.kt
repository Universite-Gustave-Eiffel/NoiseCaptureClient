package org.noiseplanet.noisecapture.permission

import android.Manifest
import android.os.Build

/**
 * Maps a multiplatform [Permission] to a set of native android permission strings.
 */
fun Permission.toAndroidPermissions(): List<String> {
    return when (this) {
        Permission.LOCATION_FOREGROUND -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        Permission.RECORD_AUDIO -> listOf(Manifest.permission.RECORD_AUDIO)

        Permission.POST_NOTIFICATIONS -> listOf(Manifest.permission.POST_NOTIFICATIONS)

        else -> emptyList()
    }
}


/**
 * Maps a list of Android permissions to a multiplatform [Permission]
 */
fun List<String>.toPermission(): Permission? {
    return when (this) {
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ),
            -> Permission.LOCATION_FOREGROUND

        listOf(Manifest.permission.RECORD_AUDIO) -> Permission.RECORD_AUDIO
        listOf(Manifest.permission.POST_NOTIFICATIONS) -> Permission.POST_NOTIFICATIONS
        else -> null
    }
}
