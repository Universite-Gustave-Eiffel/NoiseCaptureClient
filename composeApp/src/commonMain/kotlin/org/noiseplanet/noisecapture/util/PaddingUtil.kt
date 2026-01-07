package org.noiseplanet.noisecapture.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Utility modifier for bottom padding to provide different padding values based on the type of
 * bottom navigation bar used by the device. If the device uses "modern" mobile navigation with a
 * "bottom pill" (i.e. most borderless phones or tablets), it will apply the [withNavBar] value,
 * plus bottom navigation bar insets. Otherwise (on desktop, older iPhones or "old school" android
 * navigation with buttons), apply the [withoutNavBar] value.
 *
 * @param withNavBar Value applied for modern navigation bars, on top of the nav bar insets.
 *                   Defaults to `0.dp`.
 * @param withoutNavBar Value applied when no navigation bar that goes over content.
 *                      By default, equals to `withNavBar`.
 */
@Composable
fun Modifier.paddingBottomWithInsets(
    withNavBar: Dp = 0.dp,
    withoutNavBar: Dp = withNavBar,
): Modifier {
    // Calculate the bottom navigation bar padding in dp
    val bottomNavBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    return if (bottomNavBarHeight == 0.dp) {
        // If the nav bar doesn't go over content (i.e. buttons Android nav bar, desktop, old iPhones),
        // return the padding value "without nav bar".
        this.padding(bottom = withoutNavBar)
    } else {
        // Otherwise, return the padding value "with nav bar", plus the insets of the nav bar itself.
        this.padding(bottom = withNavBar + bottomNavBarHeight)
    }
}


/**
 * Utility modifier to apply only top navigation bar window insets with shorter syntax.
 */
@Composable
fun Modifier.navigationBarInsetsTop(): Modifier = this.windowInsetsPadding(
    WindowInsets.navigationBars.only(WindowInsetsSides.Top)
)
