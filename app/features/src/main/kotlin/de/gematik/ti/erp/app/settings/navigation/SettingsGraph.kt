/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.settings.navigation

import SettingsAccessibilityScreen
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.settings.ui.SettingsLegalNoticeScreen
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.settings.ui.SettingsOpenSourceLicencesScreen
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.settings.ui.SettingsDataProtectionScreen
import de.gematik.ti.erp.app.settings.ui.SettingsAllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsDeviceSecurityScreen
import de.gematik.ti.erp.app.settings.ui.SettingsAdditionalLicencesScreen
import de.gematik.ti.erp.app.settings.ui.SettingsProductImprovementsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsSetAppPasswordScreen
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsTermsOfUseScreen

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
            route = SettingsNavigationScreens.SettingsScreen.route,
            arguments = SettingsNavigationScreens.SettingsScreen.arguments
        ) { navEntry ->
            SettingsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsAccessibilityScreen.route,
            arguments = SettingsNavigationScreens.SettingsAccessibilityScreen.arguments
        ) { navEntry ->
            SettingsAccessibilityScreen(
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
            route = SettingsNavigationScreens.SettingsDeviceSecurityScreen.route,
            arguments = SettingsNavigationScreens.SettingsDeviceSecurityScreen.arguments
        ) { navEntry ->
            SettingsDeviceSecurityScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = SettingsNavigationScreens.SettingsTermsOfUseScreen.route,
            arguments = SettingsNavigationScreens.SettingsTermsOfUseScreen.arguments
        ) { navEntry ->
            @Requirement(
                "O.Arch_8#3",
                "O.Plat_11#3",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Webview containing local html without javascript"
            )
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
            @Requirement(
                "O.Arch_8#4",
                "O.Plat_11#4",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Webview containing local html without javascript"
            )
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
            arguments = SettingsNavigationScreens.SettingsSetAppPasswordScreen.arguments
        ) { navEntry ->
            SettingsSetAppPasswordScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
