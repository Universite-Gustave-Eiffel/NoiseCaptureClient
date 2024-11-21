package org.noiseplanet.noisecapture.util.shadow

import androidx.compose.ui.graphics.NativePaint
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter

actual fun NativePaint.setBlurMaskFilter(blurRadius: Float) {
    this.maskFilter = MaskFilter.makeBlur(
        mode = FilterBlurMode.NORMAL,
        sigma = blurRadius / 2f,
        respectCTM = true
    )
}
