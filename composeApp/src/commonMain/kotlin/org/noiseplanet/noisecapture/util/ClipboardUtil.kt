package org.noiseplanet.noisecapture.util

import androidx.compose.ui.platform.Clipboard

/**
 * Utility function to copy text to clipboard regardless of the platform.
 *
 * @param text Text to be copied to the clipboard.
 */
expect suspend fun Clipboard.setClipEntry(text: String)
