package org.noiseplanet.noisecapture.audio

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.ongoing_measurement_notification_body
import noisecapture.composeapp.generated.resources.ongoing_measurement_notification_title
import org.jetbrains.compose.resources.getString
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
 */
internal class AudioSourceService : Service() {

    // - Constants

    companion object {

        private const val TAG = "AudioForegroundService"
        private const val FOREGROUND_SERVICE_ID = 1
        private const val NOTIFICATION_REQUEST_CODE = 1029384756
    }

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)


    // Associated types

    /**
     * Allows accessing this service from a Context using bindService.
     */
    inner class LocalBinder : Binder() {

        /**
         * Gets this service instance.
         */
        fun getService(): AudioSourceService = this@AudioSourceService
    }


    // - Properties

    private var audioRecorder: AudioRecorder? = null
    private var audioThread: Thread? = null

    private val logger: Logger by inject { parametersOf(TAG) }
    private val binder = LocalBinder()


    // - Service

    /**
     * Called when this service is bound to a Context.
     */
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    /**
     * Called when a Context starts this service.
     */
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
        super.onDestroy()
        logger.debug("ON DESTROY")
        stopRecording()
        job.cancel()
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
        // Create the notification channel for newer Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper.createAppNotificationChannel(this)
        }

        coroutineScope.launch {
            // Promote this service to foreground service
            ServiceCompat.startForeground(
                this@AudioSourceService,
                FOREGROUND_SERVICE_ID,
                buildNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    0
                }
            )
        }
    }

    /**
     * Builds the notification that will show the service as active to the user.
     *
     * TODO: Add pause/resume controls to the notification?
     */
    private suspend fun buildNotification(): Notification {
        // Prepare notification intent to resume app when clicking the notification
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            .or(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        // Create the notification using notification builder
        val notification = NotificationCompat
            .Builder(this, NotificationHelper.APP_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(Res.string.ongoing_measurement_notification_title))
            .setContentText(getString(Res.string.ongoing_measurement_notification_body))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    NOTIFICATION_REQUEST_CODE,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE,
                )
            )
            // This will only prevent notification from being dismissed for Android version prior
            // to Android 14. Since this version, all notifications can be dismissed. It doesn't
            // stop the ongoing service however.
            // https://developer.android.com/about/versions/14/behavior-changes-all#non-dismissable-notifications
            .setOngoing(true)
            .build()

        // Set notification intent flags to only resume the app instead of starting a new activity
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE
            .or(Notification.FLAG_ONGOING_EVENT)
            .or(Notification.FLAG_NO_CLEAR)

        // Return the created notification
        return notification
    }
}
