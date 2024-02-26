package com.adrianwitaszak.kmmpermissions.permissions.delegate

import com.adrianwitaszak.kmmpermissions.permissions.model.PermissionState
import js.errors.TypeError
import js.promise.await
import web.audio.AudioContext
import web.audio.MediaStreamAudioSourceNode
import web.media.devices.MediaDevices
import web.navigator.navigator
import web.window.window


class AudioRecordPermissionDelegate : PermissionDelegate  {
    var overrideState = PermissionState.NOT_DETERMINED

    companion object {

    }

    override suspend fun getPermissionState(): PermissionState {
        if(overrideState == PermissionState.GRANTED) {
            return overrideState
        }
        return try {
            when (navigator.permissions.query(js("{ name: \"microphone\" }"))
                .await().state.toString()) {
                "granted" -> PermissionState.GRANTED
                else -> PermissionState.DENIED
            }
        } catch (e : TypeError) {
            PermissionState.NOT_DETERMINED
        }
    }

    override suspend fun providePermission() {
        val audioContext = AudioContext()
        navigator.mediaDevices.getUserMedia(js("{ audio: true, video: false }")).then {
            stream -> audioContext.createMediaStreamSource(stream)
            overrideState = PermissionState.GRANTED
        }
    }

    override fun openSettingPage() {

    }


}
