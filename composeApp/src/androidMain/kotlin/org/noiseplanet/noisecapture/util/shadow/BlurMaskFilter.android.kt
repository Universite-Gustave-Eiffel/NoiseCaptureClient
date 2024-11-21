package org.noiseplanet.noisecapture.util.shadow

import android.graphics.BlurMaskFilter
import androidx.compose.ui.graphics.NativePaint


actual fun NativePaint.setBlurMaskFilter(blurRadius: Float) {
    this.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
}
