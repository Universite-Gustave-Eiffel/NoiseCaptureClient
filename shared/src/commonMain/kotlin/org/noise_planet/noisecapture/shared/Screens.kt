package org.noise_planet.noisecapture.shared

import com.bumble.appyx.utils.multiplatform.Parcelable
import com.bumble.appyx.utils.multiplatform.Parcelize

sealed class Screens : Parcelable {

    @Parcelize
    data object HomeTarget : Screens()

    @Parcelize
    data object PermissionTarget : Screens()

}