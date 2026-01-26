package org.noiseplanet.noisecapture.services.storage

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.storage.kstore.KStoreStorageService


class MeasurementStorageService : KStoreStorageService<Measurement>(
    prefix = "measurement",
    type = Measurement::class,
) {
    // - Properties

    private val locationSequenceStorageService: StorageService<LocationSequenceFragment> by injectStorageService()
    private val leqSequenceStorageService: StorageService<LeqSequenceFragment> by injectStorageService()


    // - Public functions

    /**
     * When migrating a measurement, also handle potential sub effects of underlying sequence fragments.
     */
    override suspend fun migrate(
        uuid: String,
        currentVersion: Int,
        storedVersion: Int?,
        storedData: JsonElement?,
    ): Measurement? {
        logger.warning("Could not deserialize measurement with id: $uuid")
        logger.warning("Deleting measurement...")

        storedData?.jsonObject?.get("leqsSequenceIds")?.jsonArray?.forEach { leqSequenceId ->
            logger.warning("Deleting leq sequence fragment $leqSequenceId...")
            leqSequenceStorageService.delete(leqSequenceId.jsonPrimitive.content)
        }
        storedData?.jsonObject?.get("locationSequenceIds")?.jsonArray?.forEach { locationSequenceId ->
            logger.warning("Deleting location sequence fragment $locationSequenceId...")
            locationSequenceStorageService.delete(locationSequenceId.jsonPrimitive.content)
        }

        logger.warning("Deleting measurement object...")
        delete(uuid)
        logger.warning("Done cleaning up measurement with id: $uuid")

        return null
    }

    /**
     * Since a measurement is composed of multiple files (root measurement file + Leq and location
     * sequences fragments), we need to gather the paths to all of these sub files and download
     * them all at once as zip using [FileSystemService.downloadFiles].
     */
    override suspend fun download(uuid: String) {
        val measurement = get(uuid) ?: return

        // We need to access KStore specific methods for these services
        val leqKStoreService = leqSequenceStorageService
            as? KStoreStorageService<LeqSequenceFragment> ?: return
        val locationKStoreService = locationSequenceStorageService
            as? KStoreStorageService<LocationSequenceFragment> ?: return

        // Will hold paths to all files related to this measurement
        val measurementFiles = mutableListOf<String>()

        // Add all leq and location sequence fragments
        measurementFiles.addAll(
            measurement.leqsSequenceIds.map {
                leqKStoreService.getFileNameForRecord(it)
            }
        )
        measurementFiles.addAll(
            measurement.locationSequenceIds.map {
                locationKStoreService.getFileNameForRecord(it)
            }
        )
        // Add associated audio file, if any
        measurement.recordedAudioUrl?.let { measurementFiles.add(it) }
        // And top level measurement file
        measurementFiles.add(getFileNameForRecord(measurement.uuid))

        // Then zip and download
        fileSystemService.downloadFiles(measurementFiles, archiveName = "${uuid}_raw_export")
    }
}
