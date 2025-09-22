package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.isSuccess
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.centroidX
import ovh.plrapps.mapcompose.api.centroidY
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.setStateChangeListener
import ovh.plrapps.mapcompose.core.BelowAll
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan


class MeasurementsMapViewModel : ViewModel(), KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()

    private val levelCount = 19
    private val tileSize = 512
    private val mapSize = mapSizeAtLevel(levelCount - 1, tileSize = tileSize)

    val backgroundTilesProvider = TileStreamProvider { row, col, zoomLvl ->
        fetchRemoteTile(
            x = col,
            y = row,
            z = zoomLvl,
            baseUrl = "https://a.basemaps.cartocdn.com/light_all"
        )
    }

    val measurementTilesProvider = TileStreamProvider { row, col, zoomLvl ->
        fetchRemoteTile(
            x = col,
            y = row,
            z = zoomLvl,
            baseUrl = "https://onomap-gs.noise-planet.org/geoserver/gwc/service/tms/1.0.0/" +
                "noisecapture:noisecapture_area@EPSG:900913@png",
            tms = true,
        )
    }

    val mapState: MapState by mutableStateOf(
        MapState(
            levelCount = levelCount,
            fullWidth = mapSize,
            fullHeight = mapSize,
            tileSize = tileSize,
            workerCount = 16,
            initialValuesBuilder = {
                scale(1.0)
                val (x, y) = lonLatToNormalizedWebMercator(latitude = 48.5734, longitude = 7.7521)
                scroll(x, y)
            }
        ).apply {
            addLayer(backgroundTilesProvider, placement = BelowAll)
            addLayer(measurementTilesProvider)
        }
    )

    init {
        mapState.setStateChangeListener {
            logger.warning("NEW STATE: { x: ${this.centroidX}, y: ${this.centroidY}, scale: ${this.scale}")
        }
    }

    /**
     * wmts level are 0 based.
     * At level 0, the map corresponds to just one tile.
     */
    private fun mapSizeAtLevel(wmtsLevel: Int, tileSize: Int): Int {
        return tileSize * 2.0.pow(wmtsLevel).toInt()
    }

    private fun lonLatToNormalizedWebMercator(
        latitude: Double,
        longitude: Double,
    ): Pair<Double, Double> {
        val earthRadius = 6_378_137.0 // in meters
        val latRad = latitude * PI / 180.0
        val lngRad = longitude * PI / 180.0

        val x = earthRadius * lngRad
        val y = earthRadius * ln(tan((PI / 4.0) + (latRad / 2.0)))

        val piR = earthRadius * PI

        val normalizedX = (x + piR) / (2.0 * piR)
        val normalizedY = (piR - y) / (2.0 * piR)

        return Pair(normalizedX, normalizedY)
    }

    private suspend fun fetchRemoteTile(
        x: Int,
        y: Int,
        z: Int,
        baseUrl: String,
        tms: Boolean = false,
    ): RawSource? {
        // TODO: Dependency injection of Http client?
        val client = HttpClient(CIO)

        logger.debug("FETCHING TILE: x=$x, y=$y, z=$z")

        val trueY = if (tms) {
            val yTileCountForZoomLevel = 2.0.pow(z) - 1
            yTileCountForZoomLevel - y
        } else {
            y
        }
        val url = "$baseUrl/$z/$x/${trueY.toInt()}.png"

        val response = client.get(url)
        return try {
            if (response.status.isSuccess()) {
                Buffer().apply { write(response.bodyAsBytes()) }
            } else {
                logger.warning("Failed fetching tile at URL $url: ${response.status}")
                null
            }
        } finally {
            client.close()
        }
    }
}
