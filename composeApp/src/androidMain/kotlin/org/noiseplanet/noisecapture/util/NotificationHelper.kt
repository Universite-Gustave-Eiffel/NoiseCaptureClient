package org.noiseplanet.noisecapture.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context

internal object NotificationHelper {

    const val APP_NOTIFICATION_CHANNEL_ID = "org.noiseplanet.noisecapture.general"

    private const val APP_NOTIFICATION_CHANNEL_NAME = "General"
    private const val APP_NOTIFICATION_CHANNEL_DESCRIPTION =
        "General NoiseCapture notification channel"

    /**
     * Creates a general channel to post app level notifications
     *
     * TODO: Add support for Android < O
     *
     * @param context An android context to access [NotificationManager]
     */
    fun createAppNotificationChannel(context: Context) {
        val notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel
        val channel = NotificationChannel(
            APP_NOTIFICATION_CHANNEL_ID,
            APP_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = APP_NOTIFICATION_CHANNEL_DESCRIPTION
        notificationManager.createNotificationChannel(channel)
    }
}
