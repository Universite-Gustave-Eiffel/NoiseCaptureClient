package org.noiseplanet.noisecapture.util

import android.app.Notification
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Used to build notifications that link to the app's MainActivity, only available in its separate
 * submodule.
 */
interface NotificationProvider {

    // - Properties

    val notificationChannelId: String


    // - Public functions

    /**
     * Creates a general channel to post app level notifications
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createAppNotificationChannel()

    /**
     * Builds and return a notification object with the given content title and text.
     *
     * @param contentTitle Notification title.
     * @param contentText Notification body.
     * @param requestCode Notification request code.
     * @param flags Notification flags.
     * @param ongoing True if notification should be an ongoing (i.e. non dismissable) notification.
     */
    fun buildNotification(
        contentTitle: String,
        contentText: String,
        requestCode: Int,
        flags: Int,
        ongoing: Boolean,
    ): Notification
}
