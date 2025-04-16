/*
 * Copyright 2025, gematik GmbH
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

import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object SettingsNavigationScreens : NavigationRoutes {
    override fun subGraphName() = "settings"
    object SettingsScreen : Routes(NavigationRouteNames.SettingsScreen.name)
    object SettingsProductImprovementsScreen : Routes(NavigationRouteNames.SettingsProductImprovementScreen.name)
    object SettingsAllowAnalyticsScreen : Routes(NavigationRouteNames.SettingsAllowAnalyticsScreen.name)
    object SettingsAppSecurityScreen : Routes(NavigationRouteNames.SettingsAppSecurityScreen.name)
    object SettingsSetAppPasswordScreen : Routes(NavigationRouteNames.SettingsSetAppPasswordScreen.name)
    object SettingsDataProtectionScreen : Routes(NavigationRouteNames.SettingsDataProtectionScreen.name)
    object SettingsTermsOfUseScreen : Routes(NavigationRouteNames.SettingsTermsOfUseScreen.name)
    object SettingsLegalNoticeScreen : Routes(NavigationRouteNames.SettingsLegalNoticeScreen.name)
    object SettingsOpenSourceLicencesScreen : Routes(NavigationRouteNames.SettingsOpenSourceLicencesScreen.name)
    object SettingsAdditionalLicencesScreen : Routes(NavigationRouteNames.SettingsAdditionalLicencesScreen.name)
    object SettingsLanguageScreen : Routes(NavigationRouteNames.SettingsLanguageScreen.name)
}
