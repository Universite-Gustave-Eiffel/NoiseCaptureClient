package org.noiseplanet.noisecapture.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@OptIn(ExperimentalComposeUiApi::class)
actual fun clipEntry(string: String): ClipEntry? {
    return ClipEntry.withPlainText(string)
}
