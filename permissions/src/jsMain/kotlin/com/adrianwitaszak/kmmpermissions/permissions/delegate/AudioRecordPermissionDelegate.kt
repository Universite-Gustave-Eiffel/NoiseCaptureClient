package com.adrianwitaszak.kmmpermissions.permissions.delegate

import com.adrianwitaszak.kmmpermissions.permissions.model.PermissionState
import js.promise.await
import js.promise.toResult
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import web.navigator.navigator
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AudioRecordPermissionDelegate : PermissionDelegate  {
    var permissionState : PermissionState = PermissionState.NOT_DETERMINED
    var requestJob : Job? = null

    override fun getPermissionState(): PermissionState {
        if(requestJob == null || !requestJob!!.isActive) {
            requestJob = GlobalScope.launch {
                navigator.permissions.query(js("{ name: \"microphone\" }")).then { result ->
                    permissionState = if (result.state.toString() == "granted")
                        PermissionState.GRANTED
                    else PermissionState.DENIED
                }
            }
        }
        return permissionState
    }

    override suspend fun providePermission() {

    }

    override fun openSettingPage() {

    }
}