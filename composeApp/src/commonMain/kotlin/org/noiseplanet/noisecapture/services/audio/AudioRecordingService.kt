package org.noiseplanet.noisecapture.services.audio


/**
 * Allows recording incoming audio to a compressed output file.
 *
 * Recording, compression and file management will be handled at platform level to provide the
 * most efficient yet simple implementation for each platform.
 *
 * > Note: Audio recording permissions are not handled by this service.
 */
interface AudioRecordingService {

    /**
     * Starts recording audio to a file with the given name.
     *
     * @param outputFileName Name of the output file. File format may vary depending on the platform.
     */
    fun startRecordingToFile(outputFileName: String)

    /**
     * Stops any ongoing recording and closes output file handle.
     */
    fun stopRecordingToFile()
}
