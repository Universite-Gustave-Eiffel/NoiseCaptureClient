package org.noiseplanet.noisecapture

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File


class FilePickerEventBus {

    // - Properties

    private val _events = MutableSharedFlow<FilePickerEvent>()
    val events = _events.asSharedFlow()


    // - Public functions

    /**
     * Notify subscribers that a new file picker event is available.
     */
    suspend fun emitEvent(event: FilePickerEvent) {
        _events.emit(event)
    }
}


/**
 * Properties of a file picker event
 *
 * @param file File to be saved or shared
 * @param deleteAfterUse If true, the file should be deleted after the picker is dismissed
 */
data class FilePickerEvent(
    val file: File,
    val deleteAfterUse: Boolean = false,
)
