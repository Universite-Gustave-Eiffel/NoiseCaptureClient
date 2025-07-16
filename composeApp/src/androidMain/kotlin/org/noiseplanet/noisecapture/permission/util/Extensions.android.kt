@file:Suppress("TooGenericExceptionCaught")

package org.noiseplanet.noisecapture.permission.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState

internal fun Context.openPage(
    action: String,
    newData: Uri? = null,
    onError: (Exception) -> Unit,
) {
    try {
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            newData?.let { data = it }
        }
        startActivity(intent)
    } catch (e: Exception) {
        onError(e)
    }
}

internal fun Activity.checkPermissions(
    permissions: List<String>,
): PermissionState {
    permissions.ifEmpty {
        return PermissionState.GRANTED
    } // no permissions needed
    val status: List<Int> = permissions.map {
        this.checkSelfPermission(it)
    }
    val isOneDenied: Boolean = permissions.all {
        this.shouldShowRequestPermissionRationale(it)
    }
    val isAllGranted: Boolean = status.all {
        it == PackageManager.PERMISSION_GRANTED
    }
    return if (isAllGranted) {
        PermissionState.GRANTED
    } else if (isOneDenied) {
        PermissionState.DENIED
    } else {
        PermissionState.NOT_DETERMINED
    }
}

internal fun Activity.checkPermissionState(
    permission: String,
): PermissionState {
    return when (this.checkSelfPermission(permission)) {
        PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED

        PackageManager.PERMISSION_DENIED -> {
            if (this.shouldShowRequestPermissionRationale(permission)) {
                PermissionState.NOT_DETERMINED
            } else {
                PermissionState.DENIED
            }
        }

        else -> PermissionState.NOT_DETERMINED
    }
}

private const val REQUEST_PERMISSION_CODE = 100

internal fun Activity.providePermissions(
    permissions: List<String>,
    onError: (Throwable) -> Unit,
) {
    try {
        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            REQUEST_PERMISSION_CODE
        )
    } catch (t: Throwable) {
        onError(t)
    }
}

internal fun Context.openAppSettingsPage(permission: Permission) {
    openPage(
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        newData = "package:$packageName".toUri(),
        onError = { throw CannotOpenSettingsException(permission.name) }
    )
}
