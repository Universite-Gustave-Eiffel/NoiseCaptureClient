package org.noiseplanet.noisecapture

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File


class FilePickerEventBus {

    // - Properties

    private val _events = MutableSharedFlow<File>()
    val events = _events.asSharedFlow()


    // - Public functions

    /**
     * Notify subscribers that a new file picker event is available.
     */
    suspend fun emitEvent(file: File) {
        _events.emit(file)
    }
}
