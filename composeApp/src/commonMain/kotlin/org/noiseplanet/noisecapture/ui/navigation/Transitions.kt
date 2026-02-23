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
 * Describes navigation transitions when going from one screen to another.
 * Wrapped into an interface so that platform can implement specific navigation behaviour.
 */
interface NavigationTransitions {

    /**
     * Transition for the new entering screen
     */
    val enterTransition: TransitionCallback<EnterTransition>

    /**
     * Transition for the old exiting screen
     */
    val exitTransition: TransitionCallback<ExitTransition>

    /**
     * Transition for the parent screen coming in when popping back stack
     */
    val popEnterTransition: TransitionCallback<EnterTransition>

    /**
     * Transition for the current screen exiting when popping back stack
     */
    val popExitTransition: TransitionCallback<ExitTransition>
}

/**
 * On mobile targets, use slide in/out transitions between screens to emphasize the idea of
 * navigation stack to the end user. New screens will come from the right hand side of the screen.
 */
object MobileNavigationTransitions : NavigationTransitions {

    /**
     * Duration of screen transitions in milliseconds
     */
    private const val TRANSITION_DURATION = 300

    override val enterTransition: TransitionCallback<EnterTransition> = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            tween(easing = FastOutSlowInEasing, durationMillis = TRANSITION_DURATION)
        )
    }

    override val exitTransition: TransitionCallback<ExitTransition> = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Start,
            tween(easing = FastOutSlowInEasing, durationMillis = TRANSITION_DURATION)
        )
    }

    override val popEnterTransition: TransitionCallback<EnterTransition> = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.End,
            tween(easing = FastOutSlowInEasing, durationMillis = TRANSITION_DURATION)
        )
    }

    override val popExitTransition: TransitionCallback<ExitTransition> = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.End,
            tween(easing = FastOutSlowInEasing, durationMillis = TRANSITION_DURATION)
        )
    }
}

/**
 * On web, no transitions are expected between screens, new screens entering just replace the
 * currently displayed ones.
 */
object WebNavigationTransitions : NavigationTransitions {

    override val enterTransition: TransitionCallback<EnterTransition> = { EnterTransition.None }
    override val exitTransition: TransitionCallback<ExitTransition> = { ExitTransition.None }
    override val popEnterTransition: TransitionCallback<EnterTransition> = { EnterTransition.None }
    override val popExitTransition: TransitionCallback<ExitTransition> = { ExitTransition.None }

}
