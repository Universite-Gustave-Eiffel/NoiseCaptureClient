package org.noiseplanet.noisecapture.permission

interface PermissionService {

    fun checkPermission(permission: Permission): PermissionState

    //    fun permissionStateFlow(permission: Permission): Flow<PermissionState>
    fun requestPermission(permission: Permission)
}

expect fun getPermissionService(): PermissionService

//internal class PermissionService : IPermissionService {
//
//    // TODO: Add logger once Koin has been added
//
//    override fun checkPermission(permission: Permission): PermissionState {
//        // TODO: Provide platform specific implementation
//        return PermissionState.DENIED
//    }
//
//    override fun permissionStateFlow(permission: Permission): Flow<PermissionState> {
//        return flow {
//            emit(PermissionState.DENIED)
//        }
//    }
//
//    override fun requestPermission(permission: Permission) {
//        TODO("Not yet implemented")
//    }
//}
