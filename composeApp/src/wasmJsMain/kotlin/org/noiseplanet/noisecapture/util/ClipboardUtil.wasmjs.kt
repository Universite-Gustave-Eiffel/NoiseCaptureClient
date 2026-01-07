package org.noiseplanet.noisecapture.util

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

actual suspend fun Clipboard.setClipEntry(text: String) {
    val entry = ClipEntry.withPlainText(text)
    setClipEntry(entry)
}
