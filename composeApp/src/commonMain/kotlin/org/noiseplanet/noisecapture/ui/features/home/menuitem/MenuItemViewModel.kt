package org.noiseplanet.noisecapture.ui.features.home.menuitem

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.ui.navigation.Route

class MenuItemViewModel(
    val label: StringResource,
    val imageVector: ImageVector,
    val route: Route? = null,
) : ViewModel()
