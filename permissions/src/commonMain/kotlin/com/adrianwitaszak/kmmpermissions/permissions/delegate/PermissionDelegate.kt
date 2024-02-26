package com.adrianwitaszak.kmmpermissions.permissions.delegate

import com.adrianwitaszak.kmmpermissions.permissions.model.PermissionState

internal interface PermissionDelegate {
    suspend fun getPermissionState(): PermissionState
    suspend fun providePermission()
    fun openSettingPage()
}
