package org.noiseplanet.noisecapture.permission.util

internal class CannotOpenSettingsException(permissionName: String) :
    Exception("Cannot open settings for permission $permissionName.")

internal class PermissionRequestException(permissionName: String) :
    Exception("Failed to request $permissionName permission.")
