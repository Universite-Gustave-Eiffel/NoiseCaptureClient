package org.noise_planet.noisecapture.shared

import com.bumble.appyx.utils.multiplatform.Parcelable
import com.bumble.appyx.utils.multiplatform.Parcelize

sealed class ScreenData(val title : String) : Parcelable {

    @Parcelize
    data object HomeTarget : ScreenData("Home")

    @Parcelize
    data object PermissionTarget : ScreenData("Permissions")

    @Parcelize
    data object MeasurementTarget : ScreenData("Measurement")
}