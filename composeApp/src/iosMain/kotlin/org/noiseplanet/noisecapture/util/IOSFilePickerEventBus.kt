package org.noiseplanet.noisecapture.util

import MainViewController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSURL
import platform.UIKit.UIDocumentInteractionController


/**
 * Event bus used to pass down file download events to the host [MainViewController]
 * so that it can build and present the [UIDocumentInteractionController] accordingly.
 */
class IOSFilePickerEventBus {

    // - Properties

    private val _events = MutableStateFlow<FilePickerEvent?>(null)
    val events = _events.asStateFlow()


    // - Public functions

    /**
     * Notify subscribers that a new file picker event is available.
     */
    suspend fun emitEvent(event: FilePickerEvent) {
        _events.emit(event)
    }
}

/**
 * A file picker event
 *
 * @param fileUrl URL of the file to download
 * @param onDismiss Optional callback that will be triggered when dismissing the view controller.
 */
data class FilePickerEvent(
    val fileUrl: NSURL,
    val onDismiss: () -> Unit = {},
)
