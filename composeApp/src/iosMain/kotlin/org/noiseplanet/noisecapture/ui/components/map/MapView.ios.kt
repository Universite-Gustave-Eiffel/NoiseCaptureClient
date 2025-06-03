@file:OptIn(ExperimentalForeignApi::class)

package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView

@Composable
actual fun MapView(modifier: Modifier) {

    UIKitView(
        factory = {
            val mapView = MKMapView()
            val span = MKCoordinateSpanMake(0.025, 0.025)
            val center = CLLocationCoordinate2DMake(48.5734, 7.7521)
            val region = MKCoordinateRegionMake(centerCoordinate = center, span = span)
            mapView.setRegion(region)
            mapView.setShowsPointsOfInterest(false)
//            val mapView = MLNMapView()
//            mapView.setStyleURL(
//                NSURL.URLWithString("https://basemaps.cartocdn.com/gl/positron-gl-style/style.json")
//            )
            mapView
        },
        modifier = modifier.fillMaxSize()
    )
}


//class MLNMapViewDelegate : NSObject(), MLNMapViewDelegateProtocol {
//
//}
