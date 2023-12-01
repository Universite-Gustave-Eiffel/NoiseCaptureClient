package com.adrianwitaszak.kmmpermissions.permissions.delegate

import android.Manifest
import android.app.Activity
import android.content.Context
import com.adrianwitaszak.kmmpermissions.permissions.model.Permission
import com.adrianwitaszak.kmmpermissions.permissions.model.PermissionState
import com.adrianwitaszak.kmmpermissions.permissions.util.PermissionRequestException
import com.adrianwitaszak.kmmpermissions.permissions.util.checkPermissions
import com.adrianwitaszak.kmmpermissions.permissions.util.openAppSettingsPage
import com.adrianwitaszak.kmmpermissions.permissions.util.providePermissions

internal class AudioRecordPermissionDelegate(
    private val context: Context,
    private val activity: Lazy<Activity>,
) : PermissionDelegate {
    override fun getPermissionState(): PermissionState {
        return checkPermissions(context, activity, audioRecordPermissions)
    }

    override suspend fun providePermission() {
        activity.value.providePermissions(audioRecordPermissions) {
            throw PermissionRequestException(Permission.RECORD_AUDIO.name)
        }
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(Permission.RECORD_AUDIO)
    }
}


private val audioRecordPermissions: List<String> =
    listOf(Manifest.permission.RECORD_AUDIO)
