package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import ovh.plrapps.mapcompose.api.centerOnMarker
import ovh.plrapps.mapcompose.api.centroidX
import ovh.plrapps.mapcompose.api.centroidY
import ovh.plrapps.mapcompose.api.enableRotation
import ovh.plrapps.mapcompose.api.getMarkerInfo
import ovh.plrapps.mapcompose.api.moveMarker
import ovh.plrapps.mapcompose.api.onTouchDown
import ovh.plrapps.mapcompose.api.rotateTo
import ovh.plrapps.mapcompose.api.rotation
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.api.setStateChangeListener
import ovh.plrapps.mapcompose.api.setVisibleAreaPadding
import ovh.plrapps.mapcompose.core.BelowAll
import ovh.plrapps.mapcompose.ui.state.MapState
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.tan


/**
 * ViewModel for [MeasurementsMapView].
 *
 * @param windowSizeClass Current device's [WindowSizeClass]. Used for tiles scaling.
 * @param visibleAreaPaddingRatio Map content padding relative to the screen dimensions.
 */
class MeasurementsMapViewModel(
    windowSizeClass: WindowSizeClass,
    val visibleAreaPaddingRatio: VisibleAreaPaddingRatio = VisibleAreaPaddingRatio(),
) : ViewModel(), KoinComponent {

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
         * Number of threads allocated to fetching and decoding/encoding tiles to bitmap.
         * Value recommended by MapCompose for remote tiles is 16: https://github.com/p-lr/MapComposeMP#layers
         */
        private const val WORKER_COUNT = 16

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


    // - Associated types

    data class VisibleAreaPaddingRatio(
        val left: Float = 0f,
        val right: Float = 0f,
        val top: Float = 0f,
        val bottom: Float = 0f,
    )


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

    /*
     * Adapt the size of tiles based on window size class.
     * Higher tile size values will make map appear more zoomed in (better fit for phones)?
     * Might need some tweaking after further testing.
     */
    val tileSizePx = when (windowSizeClass.minWidthDp) {
        WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> 200
        WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> 300
        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> 350
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> 400
        else -> 600
    }

    /**
     * Computes the size of the entire map at max zoom level, in pixels.
     * WMTS levels are 0 based. At level 0, the map corresponds to just one tile.
     */
    val totalMapSizePx = tileSizePx * 2.0.pow(MAX_ZOOM_LEVEL).toInt()

    val mapState: MapState by mutableStateOf(
        MapState(
            levelCount = MAX_ZOOM_LEVEL + 1,
            fullWidth = totalMapSizePx,
            fullHeight = totalMapSizePx,
            tileSize = tileSizePx,
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
                preloadingPadding(tileSizePx * 2)
            }
        ).apply {
            enableRotation()

            // Add both background and measurement layers.
            addLayer(backgroundTilesProvider, placement = BelowAll)
            addLayer(measurementTilesProvider, initialOpacity = 0.5f)

            viewModelScope.launch(Dispatchers.Default) {
                setVisibleAreaPadding(
                    leftRatio = visibleAreaPaddingRatio.left,
                    rightRatio = visibleAreaPaddingRatio.right,
                    topRatio = visibleAreaPaddingRatio.top,
                    bottomRatio = visibleAreaPaddingRatio.bottom
                )
            }
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

    private var _mapOrientationFlow = MutableStateFlow(0f)
    var mapOrientationFlow: StateFlow<Float> = _mapOrientationFlow

    /**
     * If enabled, automatically recenter the map on every location updates.
     * Useful for following user movements when making a measurement.
     */
    var autoRecenterEnabled: Boolean = true


    // - Lifecycle

    init {
        mapState.onTouchDown {
            // If the user manually interacts with the map, disables automatic location tracking.
            autoRecenterEnabled = false
        }

        mapState.setStateChangeListener {
            // Whenever map orientation changes, emit new value through flow to update UI.
            _mapOrientationFlow.tryEmit(this.rotation)
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

                    if (autoRecenterEnabled) {
                        recenter()
                    }
                }
            }
        }
    }


    // - Public function

    fun recenter() {
        viewModelScope.launch {
            mapState.centerOnMarker(
                id = USER_LOCATION_MARKER_ID,
                destScale = zoomLevelToScale(INITIAL_ZOOM_LEVEL),
            )
        }
    }

    fun resetOrientation() {
        viewModelScope.launch {
            mapState.rotateTo(0f)
        }
    }

    fun zoomIn() {
        val zoomLevel = scaleToZoomLevel(mapState.scale)
        snapToZoomLevel(zoomLevel + 1)
    }

    fun zoomOut() {
        val zoomLevel = scaleToZoomLevel(mapState.scale)
        snapToZoomLevel(zoomLevel - 1)
    }


    // - Private functions

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
                UserLocationMarker(orientationDegrees = mapState.rotation)
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

    /**
     * Animates mapState to new zoom level.
     */
    private fun snapToZoomLevel(zoomLevel: Int) {
        viewModelScope.launch {
            // TODO: Take visible area padding into account for getting current centroid.
            mapState.scrollTo(
                x = mapState.centroidX,
                y = mapState.centroidY,
                destScale = zoomLevelToScale(zoomLevel)
            )
        }
    }
}
