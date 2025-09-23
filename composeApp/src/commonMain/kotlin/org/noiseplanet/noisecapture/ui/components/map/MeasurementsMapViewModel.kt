package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.location.UserLocationProvider
import org.noiseplanet.noisecapture.ui.components.button.IconNCButtonViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.util.injectLogger
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.enableRotation
import ovh.plrapps.mapcompose.api.getMarkerInfo
import ovh.plrapps.mapcompose.api.moveMarker
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.api.setStateChangeListener
import ovh.plrapps.mapcompose.core.BelowAll
import ovh.plrapps.mapcompose.ui.state.MapState
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.tan


class MeasurementsMapViewModel : ViewModel(), KoinComponent {

    // - Constants

    companion object {

        /**
         * Sets the maximum zoom level. Defines the zoom level at which scale will be 1.0.
         * Each smaller zoom level then divides scale by two so zoom_max-1 = 0.5, zoom_max-2 = 0.25, etc.
         */
        private const val MAX_ZOOM_LEVEL = 20

        /**
         * Zoom level that is initially picked upon opening the map.
         */
        private const val INITIAL_ZOOM_LEVEL = 17

        /**
         * Size of tiles in pixels. The greater this value, the more "zoomed in" the map will
         * appear on the device.
         */
        private const val TILE_SIZE_PX = 750

        /**
         * Number of threads allocated to fetching and decoding/encoding tiles to bitmap.
         * Value recommended by MapCompose for remote tiles is 16: https://github.com/p-lr/MapComposeMP#layers
         */
        private const val WORKER_COUNT = 16

        /**
         * Computes the size of the entire map at max zoom level, in pixels.
         * WMTS levels are 0 based. At level 0, the map corresponds to just one tile.
         */
        private val TOTAL_MAP_SIZE_PX = TILE_SIZE_PX * 2.0.pow(MAX_ZOOM_LEVEL).toInt()

        /**
         * Default coordinates for the map if user location isn't enabled.
         * Set to Nantes city center.
         */
        private const val DEFAULT_LATITUDE = 47.21724981872895
        private const val DEFAULT_LONGITUDE = -1.5583589911308107

        private const val BACKGROUND_TILESET_URL = "https://a.basemaps.cartocdn.com/light_all"

        private const val NOISEPLANET_GEOSERVER_URL =
            "https://onomap-gs.noise-planet.org/geoserver/gwc/service/tms/1.0.0/"

        private const val MEASUREMENTS_TILESET_URL =
            NOISEPLANET_GEOSERVER_URL + "noisecapture:noisecapture_area@EPSG:900913@png"

        private const val USER_LOCATION_MARKER_ID = "user_location"
    }


    // - Properties

    private val logger: Logger by injectLogger()
    private val locationProvider: UserLocationProvider by inject()

    val backgroundTilesProvider = RemoteTileStreamProvider(
        tileServerUrl = BACKGROUND_TILESET_URL
    )

    val measurementTilesProvider = RemoteTileStreamProvider(
        tileServerUrl = MEASUREMENTS_TILESET_URL,
        tms = true,
    )

    val mapState: MapState by mutableStateOf(
        MapState(
            levelCount = MAX_ZOOM_LEVEL + 1,
            fullWidth = TOTAL_MAP_SIZE_PX,
            fullHeight = TOTAL_MAP_SIZE_PX,
            tileSize = TILE_SIZE_PX,
            workerCount = WORKER_COUNT,
            initialValuesBuilder = {
                // Move the map to its initial scale and centroid.
                val (x, y) = lonLatToNormalizedWebMercator(
                    latitude = DEFAULT_LATITUDE,
                    longitude = DEFAULT_LONGITUDE
                )
                scroll(x, y)
                scale(zoomLevelToScale(INITIAL_ZOOM_LEVEL))

                // Preload the next N tiles in every direction for smoother scrolling.
                // Greater values provide better map scrolling experience but also increase performance
                // impact and network usage.
                preloadingPadding(TILE_SIZE_PX * 2)
            }
        ).apply {
            enableRotation()

            // Add both background and measurement layers.
            addLayer(backgroundTilesProvider, placement = BelowAll)
            addLayer(measurementTilesProvider, initialOpacity = 0.5f)
        }
    )

    val recenterButtonViewModel = IconNCButtonViewModel(
        icon = Icons.Default.MyLocation,
        colors = {
            NCButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        },
        hasDropShadow = true,
    )


    // - Lifecycle

    init {
        mapState.setStateChangeListener {
//            logger.warning("NEW STATE: { x: ${this.centroidX}, y: ${this.centroidY}, scale: ${this.scale}")
        }

        // Subscribe to user location update to follow the user location on the map.
        locationProvider.startUpdatingLocation()
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                locationProvider.liveLocation.map { locationRecord ->
                    logger.debug("NEW LOCATION UPDATE: ${locationRecord.lat}, ${locationRecord.lon}")
                    // Map 3D coordinates to 2D normalized projection
                    lonLatToNormalizedWebMercator(
                        latitude = locationRecord.lat,
                        longitude = locationRecord.lon
                    )
                }.collect { (x, y) ->
                    updateUserLocationMarker(x, y)
                    recenterMapIfNeeded(x, y)
                }
            }
        }
    }


    // - Public function

    fun recenter() {
        locationProvider.currentLocation?.let { locationRecord ->
            val (x, y) = lonLatToNormalizedWebMercator(
                latitude = locationRecord.lat,
                longitude = locationRecord.lon
            )
            viewModelScope.launch {
                mapState.scrollTo(x, y, destScale = zoomLevelToScale(INITIAL_ZOOM_LEVEL))
            }
        }
    }


    // - Private functions

    private suspend fun recenterMapIfNeeded(centroidX: Double, centroidY: Double) {
        // TODO: Add a snap to location property to only follow user location if enabled
        // Scroll map to new location
        mapState.scrollTo(centroidX, centroidY)
    }

    /**
     * Creates or updates the blue dot that marks the user's current location.
     */
    private fun updateUserLocationMarker(x: Double, y: Double) {
        mapState.getMarkerInfo(id = USER_LOCATION_MARKER_ID)?.let {
            // If marker is already added to the map, move it to the new location
            mapState.moveMarker(id = USER_LOCATION_MARKER_ID, x = x, y = y)
        } ?: run {
            // Otherwise, create and add marker
            mapState.addMarker(id = USER_LOCATION_MARKER_ID, x = x, y = y) {
                UserLocationMarker()
            }
        }
    }

    /**
     * Converts input latitude and longitude to normalized Web Mercator coordinates.
     * Basically does a projection from a 3D coordinates system to a 2D projection.
     *
     * In the projected referential, [0, 0] is the top left corner of the map, and [1, 1] is the
     * bottom right corner. This is the coordinates system used by MapCompose.
     *
     * @param latitude Input latitude in degrees
     * @param longitude Input longitude in degrees
     *
     * @return X,Y normalized 2D coordinates.
     */
    private fun lonLatToNormalizedWebMercator(
        latitude: Double,
        longitude: Double,
    ): Pair<Double, Double> {
        // Could be precomputed if optimization is needed.
        val earthRadius = 6_378_137.0 // in meters
        val piR = earthRadius * PI

        val latRad = latitude * PI / 180.0
        val lngRad = longitude * PI / 180.0

        val x = earthRadius * lngRad
        val y = earthRadius * ln(tan((PI / 4.0) + (latRad / 2.0)))

        val normalizedX = (x + piR) / (2.0 * piR)
        val normalizedY = (piR - y) / (2.0 * piR)

        return Pair(normalizedX, normalizedY)
    }

    /**
     * Converts a given zoom level to a map scale value based on the map's maximum zoom level.
     * Each smaller zoom level from the max level divides the scale by two. Max zoom level is scale 1.0.
     *
     * @param zoomLevel Tile zoom level.
     * @return Corresponding scale value.
     */
    private fun zoomLevelToScale(zoomLevel: Int): Double {
        return 1.0 / 2.0.pow(MAX_ZOOM_LEVEL - zoomLevel)
    }

    /**
     * Converts the given scale value to a map zoom level based on the map's maximum zoom level.
     * Each smaller zoom level from the max level divides the scale by two. Max zoom level is scale 1.0.
     *
     * @param scale Input map scale.
     * @return Tile zoom level.
     */
    private fun scaleToZoomLevel(scale: Double): Int {
        // Solve for zoomLevel: scale = 1 / 2^(MAX_ZOOM_LEVEL - zoomLevel)
        // => 2^(MAX_ZOOM_LEVEL - zoomLevel) = 1 / scale
        // => MAX_ZOOM_LEVEL - zoomLevel = log2(1 / scale)
        // => zoomLevel = MAX_ZOOM_LEVEL - log2(1 / scale)
        return MAX_ZOOM_LEVEL - log2(1.0 / scale).toInt()
    }
}
