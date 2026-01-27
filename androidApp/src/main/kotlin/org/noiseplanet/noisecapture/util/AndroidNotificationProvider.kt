package org.noiseplanet.noisecapture.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.MainActivity
import org.noiseplanet.noisecapture.R

class AndroidNotificationProvider : NotificationProvider, KoinComponent {

    // - Constants

    companion object {
        const val APP_NOTIFICATION_CHANNEL_ID = "org.noiseplanet.noisecapture.general"

        private const val APP_NOTIFICATION_CHANNEL_NAME = "General"
        private const val APP_NOTIFICATION_CHANNEL_DESCRIPTION =
            "General NoiseCapture notification channel"
    }


    // - Properties

    private val context: Context by inject()

    override val notificationChannelId: String
        get() = APP_NOTIFICATION_CHANNEL_ID


    // - Public functions

    /**
     * Creates a general channel to post app level notifications
     *
     * @param context An android context to access [android.app.NotificationManager]
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun createAppNotificationChannel() {
        val notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        // If the channel already exists, don't recreate it again
        if (notificationManager.getNotificationChannel(APP_NOTIFICATION_CHANNEL_ID) != null) {
            return
        }

        // Create the notification channel
        val channel = NotificationChannel(
            APP_NOTIFICATION_CHANNEL_ID,
            APP_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = APP_NOTIFICATION_CHANNEL_DESCRIPTION
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildNotification(
        contentTitle: String,
        contentText: String,
        requestCode: Int,
        flags: Int,
        ongoing: Boolean,
    ): Notification {
        // Prepare notification intent to resume app when clicking the notification
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            .or(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        // Create the notification using notification builder
        val notification = NotificationCompat.Builder(context, APP_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_small_notification)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    requestCode,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE,
                )
            )
            // This will only prevent notification from being dismissed for Android version prior
            // to Android 14. Since this version, all notifications can be dismissed. It doesn't
            // stop the ongoing service however.
            // https://developer.android.com/about/versions/14/behavior-changes-all#non-dismissable-notifications
            .setOngoing(ongoing)
            .build()

        // Set notification intent flags to only resume the app instead of starting a new activity
        notification.flags = flags

        // Return the created notification
        return notification
    }
}
