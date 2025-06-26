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

    // - Properties

    var recordingStartListener: RecordingStartListener?
    var recordingStopListener: RecordingStopListener?


    // - Public functions

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

    /**
     * Gets the size of the file at the given URL, in bytes.
     *
     * @param audioUrl Audio file URL.
     * @return File size in bytes, null if not found.
     */
    fun getFileSize(audioUrl: String): Long?

    /**
     * Deletes the audio file at the given URL.
     *
     * @param audioUrl Audio file URL.
     */
    fun deleteFileAtUrl(audioUrl: String)


    // - Listeners

    interface RecordingStartListener {

        /**
         * Called when starting a new recording.
         */
        fun onRecordingStart()
    }

    interface RecordingStopListener {

        /**
         * Called after recording stopped.
         *
         * @param fileUrl URL of the audio file.
         */
        fun onRecordingStop(fileUrl: String)
    }
}
