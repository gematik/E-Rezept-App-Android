/*
 * Copyright (Change Date see Readme), gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.analytics.tracker.Tracker
import org.kodein.di.compose.rememberInstance

/**
 * [renderComposable] renders the [Screen] as a composable and allows this to be
 * included in the [NavGraphBuilder].
 *
 *  @param route route for the destination
 *  @param arguments list of arguments to associate with destination
 *  @param deepLinks list of deep links to associate with the destinations
 *  @param stackEnterAnimation callback to determine the destination's enter transition
 *  @param stackExitAnimation callback to determine the destination's exit transition
 *  @param popEnterAnimation callback to determine the destination's popEnter transition
 *  @param popExitAnimation callback to determine the destination's popExit transition
 *  @param screen [Screen] for the destination
 */
fun NavGraphBuilder.renderComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    stackEnterAnimation: (
        @JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
    )? = null,
    stackExitAnimation: (
        @JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
    )? = null,
    popEnterAnimation: (
        @JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
    )? = stackEnterAnimation,
    popExitAnimation: (
        @JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
    )? = stackExitAnimation,
    screen: AnimatedContentScope.(NavBackStackEntry) -> Screen
) {
    @Requirement(
        "A_19094-01#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Tracking is built into the navigation system of the app where only screen names can be transmitted."
    )
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = stackEnterAnimation,
        exitTransition = stackExitAnimation,
        popEnterTransition = popEnterAnimation ?: stackEnterAnimation,
        popExitTransition = popExitAnimation ?: stackExitAnimation,
        content = {
            val screenToBeRendered = screen(this, it)
            val tracker by rememberInstance<Tracker>()
            val routeToBeTracked = remember { route.routeEnum() }
            tracker.computeScreenTrackingProperty(routeToBeTracked)?.let { screenName ->
                tracker.trackScreen(screenName)
            }

            screenToBeRendered.Content()
        }
    )
}

/**
 * [renderBottomSheet] adds the [Screen] that holds [Composable] as bottom sheet content to the [NavGraphBuilder]
 *
 * @param route route for the destination
 * @param arguments list of arguments to associate with destination
 * @param deepLinks list of deep links to associate with the destinations
 * @param screen the [BottomSheetScreen] at the given destination
 */
@OptIn(ExperimentalMaterialNavigationApi::class)
fun NavGraphBuilder.renderBottomSheet(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    screen: ColumnScope.(NavBackStackEntry) -> BottomSheetScreen
) {
    @Requirement(
        "A_19094-01#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Tracking is built into the navigation system of the app where only screen names can be transmitted."
    )
    bottomSheet(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        content = {
            val screenToBeRendered = screen(this, it)
            val tracker by rememberInstance<Tracker>()
            val routeToBeTracked = remember { route.routeEnum() }
            tracker.computeScreenTrackingProperty(routeToBeTracked)?.let { screenName ->
                tracker.trackScreen(screenName)
            }
            screenToBeRendered.BottomSheetContent()
        }
    )
}

private fun String.routeEnum() = when {
    this.contains("?") -> this.split(("?"))[0]
    else -> this
}
