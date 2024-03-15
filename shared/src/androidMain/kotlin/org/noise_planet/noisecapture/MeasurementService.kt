package org.noise_planet.noisecapture;

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * Android service that will not close when app is sent in background
 */
typealias AudioCallBack = (audioSamples : AudioSamples) -> Unit

class MeasurementService : Service() {
    private val binder: IBinder = LocalBinder()
    private val audioCallBacks = ArrayList<AudioCallBack>()

    inner class LocalBinder : Binder() {
        val service: MeasurementService
            get() = this@MeasurementService
    }

    override fun onBind(intent: Intent): IBinder = binder

    public fun addCallBack(audioCallBack: AudioCallBack) {
        audioCallBacks.add(audioCallBack)
    }

    public fun removeCallBack(audioCallBack: AudioCallBack) : Boolean{
        return audioCallBacks.remove(audioCallBack)
    }
}
