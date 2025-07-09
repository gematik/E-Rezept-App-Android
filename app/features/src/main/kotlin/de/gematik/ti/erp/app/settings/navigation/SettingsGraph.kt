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

package de.gematik.ti.erp.app.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideInRight
import de.gematik.ti.erp.app.navigation.slideOutLeft
import de.gematik.ti.erp.app.navigation.slideOutUp
import de.gematik.ti.erp.app.settings.ui.screens.SettingsAdditionalLicencesScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsAllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsAppSecurityScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsDataProtectionScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsLanguageScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsLegalNoticeScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsOpenSourceLicencesScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsProductImprovementsScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsSetAppPasswordScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsTermsOfUseScreen

@Suppress("LongMethod")
fun NavGraphBuilder.settingsGraph(
    startDestination: String = SettingsRoutes.SettingsScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = SettingsRoutes.subGraphName()
    ) {
        renderComposable(
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() },
            popExitAnimation = { slideOutUp() },
            route = SettingsRoutes.SettingsScreen.route,
            arguments = SettingsRoutes.SettingsScreen.arguments
        ) { navEntry ->
            SettingsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsProductImprovementsScreen.route,
            arguments = SettingsRoutes.SettingsProductImprovementsScreen.arguments
        ) { navEntry ->
            SettingsProductImprovementsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsAppSecurityScreen.route,
            arguments = SettingsRoutes.SettingsAppSecurityScreen.arguments,
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() }
        ) { navEntry ->
            SettingsAppSecurityScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsTermsOfUseScreen.route,
            arguments = SettingsRoutes.SettingsTermsOfUseScreen.arguments
        ) { navEntry ->
            SettingsTermsOfUseScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsLegalNoticeScreen.route,
            arguments = SettingsRoutes.SettingsLegalNoticeScreen.arguments
        ) { navEntry ->
            SettingsLegalNoticeScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsDataProtectionScreen.route,
            arguments = SettingsRoutes.SettingsDataProtectionScreen.arguments
        ) { navEntry ->
            SettingsDataProtectionScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsOpenSourceLicencesScreen.route,
            arguments = SettingsRoutes.SettingsOpenSourceLicencesScreen.arguments
        ) { navEntry ->
            SettingsOpenSourceLicencesScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsAdditionalLicencesScreen.route,
            arguments = SettingsRoutes.SettingsAdditionalLicencesScreen.arguments
        ) { navEntry ->
            SettingsAdditionalLicencesScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsAllowAnalyticsScreen.route,
            arguments = SettingsRoutes.SettingsAllowAnalyticsScreen.arguments
        ) { navEntry ->
            SettingsAllowAnalyticsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsSetAppPasswordScreen.route,
            arguments = SettingsRoutes.SettingsSetAppPasswordScreen.arguments,
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() }
        ) { navEntry ->
            SettingsSetAppPasswordScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsRoutes.SettingsLanguageScreen.route,
            arguments = SettingsRoutes.SettingsLanguageScreen.arguments
        ) { navEntry ->
            SettingsLanguageScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
