package org.noiseplanet.noisecapture.ui.components.map

import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.isSuccess
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger
import ovh.plrapps.mapcompose.core.TileStreamProvider
import kotlin.math.pow

/**
 * Fetches map tiles hosted on remote tile servers through HTTP requests
 * and serves them as byte buffers to be displayed by map-compose.
 *
 * @param tileServerUrl Base URL for the tile server before the tile XYZ indices.
 * @param tms In TMS, the origin of coordinates is the bottom left corner so the Y coordinate goes up.
 */
class RemoteTileStreamProvider(
    val tileServerUrl: String,
    val tms: Boolean = false,
) : TileStreamProvider, KoinComponent {

    // - Properties

    private val httpClient = HttpClient {
        // TODO: Enable persistent storage caching for tiles up to a few megabytes
        //       to avoid reloading the same map area everytime a user opens the app.
        //       This would require to provide a multiplatform file storage API that is not
        //       currently provided by Ktor, but a few tweaks to our KStore implementation should
        //       probably do the job.
        install(HttpCache)
    }

    private val logger: Logger by injectLogger()


    // - Public functions

    override suspend fun getTileStream(row: Int, col: Int, zoomLvl: Int): RawSource? {

        // If TMS is enabled, we need to flip the row index so that instead of going top to bottom
        // it goes bottom to top.
        val trueRow = if (tms) {
            // Zoom level defines the number of tiles as powers of two.
            val rowCountForZoomLevel = (2.0.pow(zoomLvl) - 1).toInt()
            rowCountForZoomLevel - row
        } else {
            row
        }

        val url = "$tileServerUrl/$zoomLvl/$col/$trueRow.png"
        val response = httpClient.get(url)

        return if (response.status.isSuccess()) {
            Buffer().apply {
                write(response.bodyAsBytes())
            }
        } else {
            logger.warning("Failed fetching tile at URL $url: ${response.status}")
            null
        }
    }
}
