package org.noise_planet.noisecapture.shared

import com.bumble.appyx.utils.multiplatform.Parcelable
import com.bumble.appyx.utils.multiplatform.Parcelize

sealed class Screens(val title : String) : Parcelable {

    @Parcelize
    data object HomeTarget : Screens("Home")

    @Parcelize
    data object PermissionTarget : Screens("Permissions")

}