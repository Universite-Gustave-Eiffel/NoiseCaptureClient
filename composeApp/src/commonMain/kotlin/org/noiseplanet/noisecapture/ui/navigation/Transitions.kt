package org.noiseplanet.noisecapture.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry

private typealias TransitionCallback<TransitionType> =
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> TransitionType)

/**
 * Transition animations to be used across this app.
 */
object Transitions {

    /**
     * Duration of screen transitions in milliseconds
     */
    private const val TRANSITION_DURATION = 300

    val enterTransition: TransitionCallback<EnterTransition> = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            tween(easing = FastOutSlowInEasing, durationMillis = TRANSITION_DURATION)
        )
    }
    val exitTransition: TransitionCallback<ExitTransition> = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            tween(easing = FastOutSlowInEasing, durationMillis = TRANSITION_DURATION)
        )
    }
    val popEnterTransition: TransitionCallback<EnterTransition> = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.End,
            tween(easing = FastOutSlowInEasing, durationMillis = TRANSITION_DURATION)
        )
    }
    val popExitTransition: TransitionCallback<ExitTransition> = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.End,
            tween(easing = FastOutSlowInEasing, durationMillis = TRANSITION_DURATION)
        )
    }
}
