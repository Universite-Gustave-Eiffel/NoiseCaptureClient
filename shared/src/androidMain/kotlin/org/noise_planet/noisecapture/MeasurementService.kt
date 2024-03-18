package org.noise_planet.noisecapture;

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


/**
 * Android service that will not close when app is sent in background
 */
typealias AudioCallBack = (audioSamples : AudioSamples) -> Unit

class MeasurementService : Service() {
    private val binder: IBinder = LocalBinder()
    private val audioCallBacks = ArrayList<AudioCallBack>()
    private var mNM: NotificationManager? = null
    private var notificationInstance: Notification? = null

    private var bindingIntent: Intent? = null

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private val NOTIFICATION: Int = 1

    inner class LocalBinder : Binder() {
        val service: MeasurementService
            get() = this@MeasurementService
    }

    override fun onCreate() {
        super.onCreate()
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    override fun onBind(intent: Intent): IBinder {
        bindingIntent = intent
        return binder
    }

    public fun addCallBack(audioCallBack: AudioCallBack) {
        audioCallBacks.add(audioCallBack)
    }

    public fun removeCallBack(audioCallBack: AudioCallBack) : Boolean{
        return audioCallBacks.remove(audioCallBack)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        return if (Build.VERSION.SDK_INT >= 26) {
            val chan = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_NONE
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            channelId
        } else {
            ""
        }
    }

    override fun onDestroy() {
        // Cancel the persistent notification.
        mNM!!.cancel(NOTIFICATION)

        // Tell the user we stopped.
        Toast.makeText(this, "Measurement service stopped", Toast.LENGTH_SHORT).show()
    }

    /**
     * Show a notification while this service is running.
     */
    private fun showNotification() {
        val text = "Measurement service started"

        // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            bindingIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        if(notificationInstance == null) {

            var channelId = ""
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            if (Build.VERSION.SDK_INT >= 26) {
                channelId = createNotificationChannel("noisecapture", "NoiseCapture measurement");
            }
            // Set the info for the views that show in the notification panel.
            notificationInstance = NotificationCompat.Builder(this, channelId)
                //.setSmallIcon(R.drawable.stat_sample) // the status icon
                .setTicker(text) // the status text
                .setWhen(System.currentTimeMillis()) // the time stamp
                .setContentTitle("Measurement") // the label of the entry
                .setContentText(text) // the contents of the entry
                .setContentIntent(contentIntent) // The intent to send when the entry is clicked
                .build()
        }

        // Send the notification.
        mNM!!.notify(NOTIFICATION, notificationInstance)
    }
}
