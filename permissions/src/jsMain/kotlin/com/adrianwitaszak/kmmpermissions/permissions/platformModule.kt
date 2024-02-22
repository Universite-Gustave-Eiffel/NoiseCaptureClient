package com.adrianwitaszak.kmmpermissions.permissions

import com.adrianwitaszak.kmmpermissions.permissions.delegate.AudioRecordPermissionDelegate
import com.adrianwitaszak.kmmpermissions.permissions.delegate.PermissionDelegate
import com.adrianwitaszak.kmmpermissions.permissions.model.Permission
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal actual fun platformModule(): Module  = module {
    single<PermissionDelegate>(named(Permission.RECORD_AUDIO.name)) {
        AudioRecordPermissionDelegate()
    }
}
