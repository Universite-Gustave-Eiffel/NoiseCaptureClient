package org.noiseplanet.noisecapture.ui.features.settings.item

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.ui.navigation.Route

data class SettingsItemViewModel(
    val title: StringResource,
    val description: StringResource,
    val icon: ImageVector,
    val target: Route,
)
