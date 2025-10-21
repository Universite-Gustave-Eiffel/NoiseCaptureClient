package org.noiseplanet.noisecapture.services.storage

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.services.storage.kstore.KStoreStorageService
import org.noiseplanet.noisecapture.util.injectLogger


class MeasurementStorageService : KStoreStorageService<Measurement>(
    prefix = "measurement",
    type = Measurement::class,
) {
    // - Properties

    private val logger: Logger by injectLogger()

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
            leqSequenceStorageService.delete(locationSequenceId.jsonPrimitive.content)
        }

        logger.warning("Deleting measurement object...")
        super.delete(uuid)
        logger.warning("Done cleaning up measurement with id: $uuid")

        return null
    }

    /**
     * Override delete method to also delete associated sequence fragments.
     *
     * TODO: Perhaps this should move to MeasurementService...
     */
    override suspend fun delete(uuid: String) {
        val measurement = get(uuid) ?: return

        // Delete all attached LEq sequence fragments
        measurement.leqsSequenceIds.forEach {
            leqSequenceStorageService.delete(it)
        }
        // Delete all attached location sequence fragments
        measurement.locationSequenceIds.forEach {
            locationSequenceStorageService.delete(it)
        }

        // Lastly, delete the measurement itself
        super.delete(uuid)
    }
}
