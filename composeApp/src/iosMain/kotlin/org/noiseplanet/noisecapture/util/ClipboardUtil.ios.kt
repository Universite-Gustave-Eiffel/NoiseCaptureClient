package org.noiseplanet.noisecapture.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

@OptIn(ExperimentalComposeUiApi::class)
actual suspend fun Clipboard.setClipEntry(text: String) {
    val entry = ClipEntry.withPlainText(text)
    setClipEntry(entry)
}
