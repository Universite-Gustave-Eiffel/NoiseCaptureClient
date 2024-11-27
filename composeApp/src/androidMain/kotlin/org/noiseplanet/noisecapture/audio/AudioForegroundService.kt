package org.noiseplanet.noisecapture.audio

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.MainActivity
import org.noiseplanet.noisecapture.R
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.NotificationHelper

/**
 * An Android service that will keep the audio recording active as long as possible while the
 * app is not in foreground.
 *
 * TODO: Check under which circumstances the system kills the service
 * TODO: Handle system audio interruptions (playing sound from another app, phone call, timer, ...)
 * TODO: Handle Android versions prior to Android O
 */
internal class AudioForegroundService : Service() {

    // - Constants

    companion object {

        private const val TAG = "AudioForegroundService"
        private const val FOREGROUND_SERVICE_ID = 1
    }


    // Associated types

    /**
     * Allows accessing this service from a Context using bindService.
     */
    inner class LocalBinder : Binder() {

        /**
         * Gets this service instance.
         */
        fun getService(): AudioForegroundService = this@AudioForegroundService
    }


    // - Properties

    private var audioRecorder: AudioRecorder? = null
    private var audioThread: Thread? = null

    private val logger: Logger by inject { parametersOf(TAG) }
    private val binder = LocalBinder()


    // - Service

    override fun onBind(intent: Intent?): IBinder {
        logger.debug("ON BIND")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.debug("ON START COMMAND")

        // Promote this service to foreground service, showing a notification to the user.
        // This needs to be called within 10 seconds of starting the service otherwise an
        // exception will be thrown by the system.
        startAsForegroundService()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        logger.debug("ON CREATE")
    }

    override fun onDestroy() {
        logger.debug("ON DESTROY")
        stopRecording()
        super.onDestroy()
    }


    // - Public functions

    /**
     * Starts recording audio through the provided [AudioRecorder].
     */
    fun startRecording(audioRecorder: AudioRecorder) {
        this.audioRecorder = audioRecorder
        audioThread = Thread(audioRecorder)
        audioThread?.start()
    }

    /**
     * Stops audio recording and stops this service from running.
     */
    fun stopRecording() {
        audioRecorder?.stopRecording()
        audioThread?.join()
        audioRecorder = null
        stopSelf()
    }


    // - Private functions

    /**
     * Promotes this service to a foreground service.
     * This needs to be called within 10 seconds after starting the service otherwise an
     * exception will be thrown by the system.
     *
     * [More details](https://developer.android.com/develop/background-work/services/foreground-services)
     */
    private fun startAsForegroundService() {
        // Create the notification channel
        NotificationHelper.createAppNotificationChannel(this)

        // Promote this service to foreground service
        ServiceCompat.startForeground(
            this,
            FOREGROUND_SERVICE_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                0
            }
        )
    }

    /**
     * Builds the notification that will show the service as active to the user.
     *
     * TODO: Localize content
     *
     * TODO: Notification can still be manually dismissed for Android > 14.
     *       Does the service get killed then?
     *
     * TODO: Add pause/resume controls to the notification?
     */
    private fun buildNotification(): Notification {
        return Notification.Builder(this, NotificationHelper.APP_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Notification content title")
            .setContentText("Notification content text")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(Intent(this, MainActivity::class.java).let {
                PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
            })
            .setOngoing(true)
            .build()
    }
}
