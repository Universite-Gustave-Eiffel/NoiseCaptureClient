package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.location.UserLocationProvider
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.ui.components.button.IconNCButtonViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.GeoUtil
import org.noiseplanet.noisecapture.util.injectLogger
import ovh.plrapps.mapcompose.api.BoundingBox
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.addPath
import ovh.plrapps.mapcompose.api.centerOnMarker
import ovh.plrapps.mapcompose.api.centroidX
import ovh.plrapps.mapcompose.api.centroidY
import ovh.plrapps.mapcompose.api.enableRotation
import ovh.plrapps.mapcompose.api.hasMarker
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
import kotlin.math.log2
import kotlin.math.pow


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
    private val measurementService: MeasurementService by inject()

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
                val (x, y) = GeoUtil.lonLatToNormalizedWebMercator(
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

                val measurementUuid = measurementService.getAllMeasurements().first().uuid
                addPathsForMeasurement(measurementUuid)
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
                locationProvider.liveLocation.collect { locationRecord ->
                    logger.debug("NEW LOCATION UPDATE: ${locationRecord.lat}, ${locationRecord.lon}")
                    // Map 3D coordinates to 2D normalized projection
                    val (x, y) = GeoUtil.lonLatToNormalizedWebMercator(
                        latitude = locationRecord.lat,
                        longitude = locationRecord.lon
                    )
                    updateUserLocationMarker(x, y)

                    if (autoRecenterEnabled) {
                        recenter(locationRecord.orientation)
                    }
                }
            }
        }
    }


    // - Public function

    fun recenter(heading: Double?) {
        viewModelScope.launch(Dispatchers.Default) {
            mapState.centerOnMarker(
                id = USER_LOCATION_MARKER_ID,
                destScale = zoomLevelToScale(INITIAL_ZOOM_LEVEL),
//                destAngle = heading?.toFloat() ?: mapState.rotation
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
        if (mapState.hasMarker(id = USER_LOCATION_MARKER_ID)) {
            // If marker is already added to the map, move it to the new location
            mapState.moveMarker(id = USER_LOCATION_MARKER_ID, x = x, y = y)
        } else {
            // Otherwise, create and add marker
            mapState.addMarker(id = USER_LOCATION_MARKER_ID, x = x, y = y) {
                UserLocationMarker(orientationDegrees = mapState.rotation)
            }
        }
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

    /**
     * Resamples the measurement LAEq values to get one sound level value per GPS point.
     */
    private suspend fun addPathsForMeasurement(measurementUuid: String) {
        val pathBuilder = SoundLevelPathBuilder(measurementService)
        val pathPoints = pathBuilder.pathForMeasurement(measurementUuid)
        var prevXY: Pair<Double, Double>? = null

        if (pathPoints.isEmpty()) {
            return
        }

        // Add path data to map
        pathPoints.forEachIndexed { index, point ->
            if (index == 0) {
                prevXY = GeoUtil.lonLatToNormalizedWebMercator(point.latitude, point.longitude)
                return@forEachIndexed
            }
            val currXY = GeoUtil.lonLatToNormalizedWebMercator(
                latitude = point.latitude,
                longitude = point.longitude
            )

            mapState.addPath(
                id = "path-$index",
                color = NoiseLevelColorRamp.getColorForSPLValue(value = point.level),
                width = 6.dp,
                zIndex = pathPoints.size - index.toFloat(),
            ) {
                addPoints(listOfNotNull(prevXY, currXY))
            }
            prevXY = currXY
        }

        // Calculate path bounding box
        autoRecenterEnabled = false
        val (xLeft, yTop) = GeoUtil.lonLatToNormalizedWebMercator(
            latitude = pathPoints.minOf { it.latitude },
            longitude = pathPoints.minOf { it.longitude }
        )
        val (xRight, yBottom) = GeoUtil.lonLatToNormalizedWebMercator(
            latitude = pathPoints.maxOf { it.latitude },
            longitude = pathPoints.maxOf { it.longitude }
        )

        // Center path into visible viewport
        withVisibleAreaPaddingRatio(visibleAreaPaddingRatio) {
            mapState.scrollTo(
                area = BoundingBox(
                    xLeft = xLeft,
                    xRight = xRight,
                    yTop = yTop,
                    yBottom = yBottom
                ),
                padding = Offset(x = 0.1f, y = 0.1f)
            )
        }
    }

    /**
     * Sets map state's visible area padding ratio to the given values, runs the given block and
     * sets visible padding ratio back to its original value.
     *
     * @param paddingRatio Visible padding ratio to apply before running the block
     * @param block Closure to execute
     */
    private suspend fun withVisibleAreaPaddingRatio(
        paddingRatio: VisibleAreaPaddingRatio,
        block: suspend (VisibleAreaPaddingRatio) -> Unit,
    ) {
        mapState.setVisibleAreaPadding(
            leftRatio = paddingRatio.left,
            rightRatio = paddingRatio.right,
            bottomRatio = paddingRatio.bottom,
            topRatio = paddingRatio.top
        )
        block(paddingRatio)
        mapState.setVisibleAreaPadding(0f)
    }
}
