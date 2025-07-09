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

package de.gematik.ti.erp.app.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.fadeInLong
import de.gematik.ti.erp.app.navigation.fadeOutLong
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideOutUp
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.onboarding.ui.DataProtectionScreen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingAllowAnalyticsScreen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingAnalyticsPreviewScreen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingDataProtectionAndTermsOfUseOverviewScreen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingPasswordAuthenticationScreen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingSelectAppLoginScreen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingWelcomeScreen
import de.gematik.ti.erp.app.onboarding.ui.TermsOfUseScreen
import org.kodein.di.DI
import org.kodein.di.instance

fun NavGraphBuilder.onboardingGraph(
    dependencyInjector: DI,
    startDestination: String = OnboardingRoutes.OnboardingWelcomeScreen.route,
    navController: NavController
) {
    val controller by dependencyInjector.instance<OnboardingGraphController>()
    navigation(
        startDestination = startDestination,
        route = OnboardingRoutes.subGraphName()
    ) {
        renderComposable(
            route = OnboardingRoutes.OnboardingWelcomeScreen.route,
            stackEnterAnimation = { fadeInLong() },
            stackExitAnimation = { fadeOutLong() }
        ) {
            OnboardingWelcomeScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderComposable(
            route = OnboardingRoutes.OnboardingDataProtectionAndTermsOfUseOverviewScreen.route
        ) {
            OnboardingDataProtectionAndTermsOfUseOverviewScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderComposable(
            route = OnboardingRoutes.OnboardingSelectAppLoginScreen.route
        ) {
            OnboardingSelectAppLoginScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderComposable(
            route = OnboardingRoutes.OnboardingPasswordAuthenticationScreen.route
        ) {
            OnboardingPasswordAuthenticationScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderComposable(
            route = OnboardingRoutes.OnboardingAnalyticsPreviewScreen.route
        ) {
            OnboardingAnalyticsPreviewScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderComposable(
            route = OnboardingRoutes.AllowAnalyticsScreen.route,
            stackEnterAnimation = { slideInDown() }
        ) {
            OnboardingAllowAnalyticsScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderComposable(
            route = OnboardingRoutes.TermsOfUseScreen.route,
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() }
        ) {
            TermsOfUseScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderComposable(
            route = OnboardingRoutes.DataProtectionScreen.route,
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() }
        ) {
            DataProtectionScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
    }
}
