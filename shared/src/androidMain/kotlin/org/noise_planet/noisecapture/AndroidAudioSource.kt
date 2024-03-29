package org.noise_planet.noisecapture

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioRecord.ERROR
import android.media.AudioRecord.ERROR_BAD_VALUE
import android.media.AudioRecord.ERROR_INVALID_OPERATION
import android.os.IBinder
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import org.noise_planet.noisecapture.AndroidMeasurementService.LocalBinder

const val BUFFER_SIZE_TIME = 0.1
class AndroidAudioSource(private val context : Context) : AudioSource, ServiceConnection {
    private val audioSamplesChannel = Channel<AudioSamples>(onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private var androidMeasurementService : AndroidMeasurementService? = null

    @SuppressLint("MissingPermission")
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        println("onServiceConnected $name $service")
        // This is called when the connection with the service has been
        // established, giving us the service object we can use to
        // interact with the service.  Because we have bound to a explicit
        // service that we know is running in our own process, we can
        // cast its IBinder to a concrete class and directly access it.
        if(service != null) {
            androidMeasurementService = (service as LocalBinder).service
            androidMeasurementService!!.addAudioCallBack(::onSamples)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        androidMeasurementService!!.removeAudioCallBack(::onSamples)
        androidMeasurementService = null
    }

    private fun onSamples(audioSamples: AudioSamples) {
        audioSamplesChannel.trySend(audioSamples)
    }

    override suspend fun setup(): Flow<AudioSamples> {
        if(androidMeasurementService == null) {
            val intent = Intent(context, AndroidMeasurementService::class.java)
            return if (context.bindService(intent, this, Context.BIND_AUTO_CREATE)) {
                audioSamplesChannel.consumeAsFlow()
            } else {
                println("Bind failed")
                emptyFlow()
            }
        } else {
            androidMeasurementService!!.addAudioCallBack(::onSamples)
            return audioSamplesChannel.consumeAsFlow()
        }
    }

    fun readErrorCodeToString(errorCode : Int) : String {
        return when(errorCode) {
            ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION"
            ERROR_BAD_VALUE -> "ERROR_BAD_VALUE"
            ERROR -> "Other Error"
            else -> "Unknown error"
        }
    }

    override fun release() {
        if(androidMeasurementService != null) {
            androidMeasurementService!!.removeAudioCallBack(::onSamples)
        }
        context.unbindService(this)
    }

    override fun getMicrophoneLocation(): AudioSource.MicrophoneLocation {
        return AudioSource.MicrophoneLocation.LOCATION_UNKNOWN
    }
}
