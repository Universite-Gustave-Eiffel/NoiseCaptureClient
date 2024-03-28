package org.noise_planet.noisecapture;

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicBoolean


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
    private val recording = AtomicBoolean(false)
    private val audioThread = AudioThread(recording, audioCallBacks)

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private val NOTIFICATION: Int = 1

    inner class LocalBinder : Binder() {
        val service: MeasurementService
            get() = this@MeasurementService
    }

    companion object {
        class AudioThread(val recording: AtomicBoolean, val audioCallBacks : Collection<AudioCallBack>) : Runnable {

            private lateinit var audioRecord: AudioRecord
            private var bufferSize = -1
            private var sampleRate = -1

            @SuppressLint("MissingPermission")
            fun startAudioRecord() {
                if(this.bufferSize == -1) {
                    val mSampleRates = intArrayOf(48000, 44100)
                    val channel = AudioFormat.CHANNEL_IN_MONO
                    val encoding = AudioFormat.ENCODING_PCM_FLOAT
                    for (tryRate in mSampleRates) {
                        this.sampleRate = tryRate
                        val minimalBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
                        if (minimalBufferSize == AudioRecord.ERROR_BAD_VALUE || minimalBufferSize == AudioRecord.ERROR) {
                            continue
                        }
                        this.bufferSize =
                            Integer.max(minimalBufferSize, (BUFFER_SIZE_TIME * sampleRate * 4).toInt())
                        audioRecord = AudioRecord(
                            MediaRecorder.AudioSource.VOICE_RECOGNITION,
                            sampleRate,
                            channel,
                            encoding,
                            bufferSize
                        )
                        Thread(this).start()
                        break
                    }
                }
            }

            override fun run() {
                recording.set(true)
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                } catch (ex: IllegalArgumentException) {
                    // Ignore
                } catch (ex: SecurityException) {
                    // Ignore
                }
                audioRecord.startRecording()
                println("Capture microphone")
                var buffer = FloatArray(bufferSize / 4)
                while(recording.get()) {
                    val read: Int = audioRecord.read(
                        buffer, 0, buffer.size,
                        AudioRecord.READ_BLOCKING
                    )
                    if (read < buffer.size) {
                        if(read > 0) {
                            buffer = buffer.copyOfRange(0, read)
                            audioCallBacks.forEach {
                                it(AudioSamples(
                                    System.currentTimeMillis(),
                                    buffer,
                                    AudioSamples.ErrorCode.OK,
                                    sampleRate
                                ))
                            }
                        } else {
                            audioCallBacks.forEach {
                                // End of audio stream
                                it(AudioSamples(System.currentTimeMillis(), buffer.clone(), AudioSamples.ErrorCode.ABORTED, sampleRate))
                            }
                            recording.set(false)
                            break
                        }
                    } else {
                        audioCallBacks.forEach {
                            it(AudioSamples(System.currentTimeMillis(), buffer.clone(), AudioSamples.ErrorCode.OK, sampleRate))
                        }
                    }
                }
                bufferSize = -1
                audioCallBacks.forEach {
                    // End of audio stream
                    it(AudioSamples(System.currentTimeMillis(), FloatArray(0),
                        AudioSamples.ErrorCode.ABORTED, sampleRate))
                }
                audioRecord.stop()
                println("Release microphone")
            }

        }
    }


    override fun onCreate() {
        super.onCreate()
        mNM = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
    }

    override fun onBind(intent: Intent): IBinder {
        bindingIntent = intent
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        return binder
    }

    public fun addAudioCallBack(audioCallBack: AudioCallBack) {
        if(!audioCallBacks.contains(audioCallBack)) {
            audioCallBacks.add(audioCallBack)
            if (!recording.get()) {
                audioThread.startAudioRecord()
            }
        }
    }


    public fun removeAudioCallBack(audioCallBack: AudioCallBack) : Boolean{
        val found = audioCallBacks.remove(audioCallBack)
        if(audioCallBacks.isEmpty()) {
            recording.set(false)
        }
        return found
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
        println("Destroy MeasurementService")

        recording.set(false)

        // Cancel the persistent notification.
        mNM!!.cancel(NOTIFICATION)
    }

    /**
     * Show a notification while this service is running.
     */
    private fun showNotification() {
        val text = "Measurement service started"

        // The PendingIntent to launch our activity if the user selects this notification
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            bindingIntent, PendingIntent.FLAG_IMMUTABLE
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
                .setSmallIcon(R.drawable.stat_notify_voicemail) // the status icon
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
