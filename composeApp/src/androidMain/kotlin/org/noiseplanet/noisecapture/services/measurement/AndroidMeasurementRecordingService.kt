package org.noiseplanet.noisecapture.services.measurement

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.ongoing_measurement_notification_body
import noisecapture.composeapp.generated.resources.ongoing_measurement_notification_title
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.MainActivity
import org.noiseplanet.noisecapture.R
import org.noiseplanet.noisecapture.util.NotificationHelper


/**
 * An implementation of [MeasurementRecordingService] that will start a wrapped instance of
 * [DefaultMeasurementRecordingService] in an Android Foreground Service so it keeps running
 * when the app is sent to the background (as long as the system doesn't kill it).
 */
class AndroidMeasurementRecordingService(
    private val context: Context,
) : MeasurementRecordingService {

    // - Properties

    /**
     * Will hold the currently bound wrapper as long as a foreground service is running.
     */
    private var wrapper: ForegroundServiceWrapper? = null

    /**
     * Will take care of delivering the recording state of the latest running wrapped service,
     * or false if no wrapped service is currently available.
     */
    private val mergedIsRecordingFlow = MutableStateFlow(false)

    private val scope = CoroutineScope(Dispatchers.Default)
    private var isRecordingRedirectionJob: Job? = null

    /**
     * Service connection will allow us to retrieve the wrapper instance when the service is started.
     */
    private val serviceConnection = object : ServiceConnection {

        /**
         * Called when service is connected through [Context.bindService].
         * Retrieves the wrapper instance from the given [binder] and launches
         * recording through the wrapped service instance.
         */
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            checkNotNull(binder) { "Binder is null" }

            val localBinder = binder as ForegroundServiceWrapper.LocalBinder
            wrapper = localBinder.getService()

            wrapper?.innerService?.start()
            wrapper?.innerService?.onMeasurementDone = onMeasurementDone

            isRecordingRedirectionJob = scope.launch {
                // Redirect the
                wrapper?.innerService?.isRecordingFlow?.collect {
                    mergedIsRecordingFlow.tryEmit(it)
                }
            }
        }

        /**
         * Called when service is disconnected.
         */
        override fun onServiceDisconnected(name: ComponentName?) {
            wrapper = null
            isRecordingRedirectionJob?.cancel()
            mergedIsRecordingFlow.tryEmit(false)
        }
    }


    // - MeasurementRecordingService

    override val isRecording: Boolean
        get() = mergedIsRecordingFlow.value

    override val isRecordingFlow: StateFlow<Boolean>
        get() = mergedIsRecordingFlow

    override var onMeasurementDone: MeasurementRecordingService.OnMeasurementDoneListener? = null


    override fun start() {
        startForegroundServiceWrapper()
    }

    override fun endAndSave() {
        wrapper?.stopForegroundService()
    }


    // - Private functions

    /**
     * Based on the current OS version, start the [ForegroundServiceWrapper] as a foreground service
     * with a persistent notification, or as a "regular" service.
     */
    private fun startForegroundServiceWrapper() {
        val intent = Intent(context, ForegroundServiceWrapper::class.java)

        // Based
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        context.bindService(intent, serviceConnection, 0)
    }
}


/**
 * Wraps the default implementation of [MeasurementRecordingService] into a Foreground Service.
 */
internal class ForegroundServiceWrapper : KoinComponent, Service() {

    // - Constants

    companion object {

        private const val FOREGROUND_SERVICE_ID = 1
        private const val NOTIFICATION_REQUEST_CODE = 1029384756
    }


    // - Associated types

    /**
     * Allows accessing this service from a Context using bindService.
     */
    inner class LocalBinder : Binder() {

        /**
         * Gets this service instance.
         */
        fun getService(): ForegroundServiceWrapper = this@ForegroundServiceWrapper
    }


    // - Properties

    private val binder = LocalBinder()

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    // Build inner service instance
    val innerService = DefaultMeasurementRecordingService()


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
        // Promote this service to foreground service, showing a notification to the user.
        // This needs to be called within 10 seconds of starting the service otherwise an
        // exception will be thrown by the system.
        startAsForegroundService()

        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Called when this service is terminated by the calling context or by the system.
     */
    override fun onDestroy() {
        super.onDestroy()

        if (innerService.isRecording) {
            innerService.endAndSave()
        }
        job.cancel()
    }


    // - Public functions

    /**
     * Stops the ongoing foreground service, which will trigger [onDestroy], thus
     * stopping the underlying measurement and saving results before ending this service.
     */
    fun stopForegroundService() {
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
                this@ForegroundServiceWrapper,
                FOREGROUND_SERVICE_ID,
                buildNotification(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
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
