package org.noiseplanet.noisecapture.permission.delegate

import android.app.Activity
import android.content.Context
import org.noiseplanet.noisecapture.permission.Permission

internal class AudioRecordPermissionDelegate(
    context: Context,
    activity: Lazy<Activity>,
) : DefaultAndroidPermissionDelegate(
    permission = Permission.RECORD_AUDIO,
    context = context,
    activity = activity
)
