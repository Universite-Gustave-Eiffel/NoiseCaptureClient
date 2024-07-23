package com.adrianwitaszak.kmmpermissions.permissions.delegate

import com.adrianwitaszak.kmmpermissions.permissions.model.PermissionState
import js.errors.TypeError
import js.promise.await
import web.audio.AudioContext
import web.navigator.navigator

class AudioRecordPermissionDelegate : PermissionDelegate {
    private var overrideState = PermissionState.NOT_DETERMINED

    override suspend fun getPermissionState(): PermissionState {
        if (overrideState == PermissionState.GRANTED) {
            return overrideState
        }
        return try {
            when (
                navigator.permissions.query(js("{ name: \"microphone\" }"))
                    .await().state.toString()
            ) {
                "granted" -> PermissionState.GRANTED
                else -> PermissionState.DENIED
            }
        } catch (ignore: TypeError) {
            PermissionState.NOT_DETERMINED
        }
    }

    override suspend fun providePermission() {
        val audioContext = AudioContext()
        navigator.mediaDevices.getUserMedia(js("{ audio: true, video: false }")).then { stream ->
            audioContext.createMediaStreamSource(stream)
            overrideState = PermissionState.GRANTED
        }
    }

    override fun openSettingPage() {
    }
}
