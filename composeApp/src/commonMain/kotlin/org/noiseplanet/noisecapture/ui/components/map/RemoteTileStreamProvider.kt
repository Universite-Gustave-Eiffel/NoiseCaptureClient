package org.noiseplanet.noisecapture.ui.components.map

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.isSuccess
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.util.injectLogger
import ovh.plrapps.mapcompose.core.TileStreamProvider
import kotlin.math.pow

/**
 * Fetches map tiles hosted on remote tile servers through HTTP requests
 * and serves them as byte buffers to be displayed by map-compose.
 *
 * @param tileServerUrl The template URL string.
 *        Example: 'https://{s}.mapserver.org/service/{z}/{x}/{y}{r}.png'
 *        - {z}: Zoom level.
 *        - {x}: X coordinate.
 *        - {y}: Y coordinate (adjusted if [tms] is true).
 *        - {s}: Subdomain (selected from [subdomains] based on tile coordinates).
 *        - {r}: Retina suffix (renders as "@2x" if [isRetina] is true, otherwise empty).
 * @param tms If true, treats the Y coordinate as originating from the bottom-left corner.
 * @param subdomains A list of subdomains used to distribute network load.
 *        Defaults to ['a', 'b', 'c'].
 * @param isRetina If true, replaces the {r} placeholder with "@2x" to load high-density tiles.
 */
class RemoteTileStreamProvider(
    val tileServerUrl: String,
    val tms: Boolean = false,
    val subdomains: List<String> = listOf("a", "b", "c"),
    val isRetina: Boolean = false
) : TileStreamProvider, KoinComponent {

    // - Properties

    private val httpClient: HttpClient by inject()
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

        //Determine Subdomain (Distribute load based on tile coordinates)
        // This ensures the same tile always hits the same subdomain (good for caching)
        val subdomain = if (subdomains.isNotEmpty()) {
            subdomains[(row + col) % subdomains.size]
        } else {
            ""
        }

        val url = tileServerUrl
            .replace("{s}", subdomain)
            .replace("{z}", zoomLvl.toString())
            .replace("{x}", col.toString())
            .replace("{y}", trueRow.toString())
            .replace("{r}", if (isRetina) "@2x" else "")

        return runCatching {
            val response = httpClient.get(url)
            if (response.status.isSuccess()) {
                Buffer().apply {
                    write(response.bodyAsBytes())
                }
            } else {
                logger.error("Failed fetching tile at URL $url: ${response.status}")
                null
            }
        }.getOrElse { exception ->
            logger.error("Exception during call to $url:", exception)
            null
        }
    }
}
