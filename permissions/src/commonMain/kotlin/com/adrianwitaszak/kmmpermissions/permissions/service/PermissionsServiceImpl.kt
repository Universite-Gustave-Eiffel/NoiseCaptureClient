@file:Suppress("TooGenericExceptionCaught")

package com.adrianwitaszak.kmmpermissions.permissions.service

import com.adrianwitaszak.kmmpermissions.permissions.model.Permission
import com.adrianwitaszak.kmmpermissions.permissions.model.PermissionState
import com.adrianwitaszak.kmmpermissions.permissions.service.PermissionsService.Companion.PERMISSION_CHECK_FLOW_FREQUENCY
import com.adrianwitaszak.kmmpermissions.permissions.util.getPermissionDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.logger.Logger
import org.koin.mp.KoinPlatformTools

internal class PermissionsServiceImpl : PermissionsService, KoinComponent {
    private val logger: Logger = KoinPlatformTools.defaultLogger()

    private suspend fun checkPermission(permission: Permission): PermissionState {
        return try {
            return getPermissionDelegate(permission).getPermissionState()
        } catch (e: Exception) {
            logger.debug("Failed to check permission $permission \n${e.stackTraceToString()}")
            PermissionState.NO_PERMISSION_DELEGATE
        }
    }

    override fun checkPermissionFlow(permission: Permission): Flow<PermissionState> {
        return flow {
            while (true) {
                val permissionState = checkPermission(permission)
                emit(permissionState)
                delay(PERMISSION_CHECK_FLOW_FREQUENCY)
            }
        }
    }

    override suspend fun providePermission(permission: Permission) {
        try {
            getPermissionDelegate(permission).providePermission()
        } catch (e: Exception) {
            logger.error("Failed to request permission $permission")
            logger.error(e.stackTraceToString())
        }
    }

    override fun openSettingPage(permission: Permission) {
        println("Open settings for permission $permission")
        try {
            getPermissionDelegate(permission).openSettingPage()
        } catch (e: Exception) {
            logger.error("Failed to open settings for permission $permission")
            logger.error(e.stackTraceToString())
        }
    }
}
