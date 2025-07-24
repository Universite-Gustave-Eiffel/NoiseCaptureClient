package org.noiseplanet.noisecapture.permission.delegate

import android.app.Activity
import android.content.Context
import org.noiseplanet.noisecapture.permission.Permission

internal class LocationForegroundPermissionDelegate(
    context: Context,
    activity: Lazy<Activity>,
) : DefaultAndroidPermissionDelegate(
    permission = Permission.LOCATION_FOREGROUND,
    context = context,
    activity = activity,
)
