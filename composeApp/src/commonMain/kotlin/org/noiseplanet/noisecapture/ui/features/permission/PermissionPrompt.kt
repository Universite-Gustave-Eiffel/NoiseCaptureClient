package org.noiseplanet.noisecapture.ui.features.permission

import org.noiseplanet.noisecapture.permission.Permission


data class PermissionPrompt(
    val permission: Permission,
    val isRequired: Boolean,
)
