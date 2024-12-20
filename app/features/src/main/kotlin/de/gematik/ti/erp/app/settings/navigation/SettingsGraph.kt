/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
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
import de.gematik.ti.erp.app.settings.ui.screens.SettingsDataProtectionScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsAppSecurityScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsLanguageScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsLegalNoticeScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsOpenSourceLicencesScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsProductImprovementsScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsSetAppPasswordScreen
import de.gematik.ti.erp.app.settings.ui.screens.SettingsTermsOfUseScreen

@Suppress("LongMethod")
fun NavGraphBuilder.settingsGraph(
    startDestination: String = SettingsNavigationScreens.SettingsScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = SettingsNavigationScreens.subGraphName()
    ) {
        renderComposable(
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() },
            popExitAnimation = { slideOutUp() },
            route = SettingsNavigationScreens.SettingsScreen.route,
            arguments = SettingsNavigationScreens.SettingsScreen.arguments
        ) { navEntry ->
            SettingsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsProductImprovementsScreen.route,
            arguments = SettingsNavigationScreens.SettingsProductImprovementsScreen.arguments
        ) { navEntry ->
            SettingsProductImprovementsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsAppSecurityScreen.route,
            arguments = SettingsNavigationScreens.SettingsAppSecurityScreen.arguments,
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() }
        ) { navEntry ->
            SettingsAppSecurityScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsTermsOfUseScreen.route,
            arguments = SettingsNavigationScreens.SettingsTermsOfUseScreen.arguments
        ) { navEntry ->
            SettingsTermsOfUseScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsLegalNoticeScreen.route,
            arguments = SettingsNavigationScreens.SettingsLegalNoticeScreen.arguments
        ) { navEntry ->
            SettingsLegalNoticeScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsDataProtectionScreen.route,
            arguments = SettingsNavigationScreens.SettingsDataProtectionScreen.arguments
        ) { navEntry ->
            SettingsDataProtectionScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsOpenSourceLicencesScreen.route,
            arguments = SettingsNavigationScreens.SettingsOpenSourceLicencesScreen.arguments
        ) { navEntry ->
            SettingsOpenSourceLicencesScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsAdditionalLicencesScreen.route,
            arguments = SettingsNavigationScreens.SettingsAdditionalLicencesScreen.arguments
        ) { navEntry ->
            SettingsAdditionalLicencesScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsAllowAnalyticsScreen.route,
            arguments = SettingsNavigationScreens.SettingsAllowAnalyticsScreen.arguments
        ) { navEntry ->
            SettingsAllowAnalyticsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsSetAppPasswordScreen.route,
            arguments = SettingsNavigationScreens.SettingsSetAppPasswordScreen.arguments,
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() }
        ) { navEntry ->
            SettingsSetAppPasswordScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsLanguageScreen.route,
            arguments = SettingsNavigationScreens.SettingsLanguageScreen.arguments
        ) { navEntry ->
            SettingsLanguageScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
