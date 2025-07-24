package org.noiseplanet.noisecapture.permission.delegate

import android.app.Activity
import android.content.Context
import org.noiseplanet.noisecapture.permission.Permission


internal class PostNotificationsPermissionDelegate(
    context: Context,
    activity: Lazy<Activity>,
) : DefaultAndroidPermissionDelegate(
    permission = Permission.POST_NOTIFICATIONS,
    context = context,
    activity = activity
)
