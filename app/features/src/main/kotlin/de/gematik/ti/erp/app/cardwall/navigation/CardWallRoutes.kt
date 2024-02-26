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

package de.gematik.ti.erp.app.cardwall.navigation

import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier

object CardWallRoutes : NavigationRoutes {
    override fun subGraphName() = "cardwall"
    const val profileId = "profileId"
    object CardWallIntroScreen : Routes(
        NavigationRouteNames.CardWallIntroScreen.name,
        navArgument(profileId) { type = androidx.navigation.NavType.StringType }
    ) {
        fun path(profileIdentifier: ProfileIdentifier) = path(profileId to profileIdentifier)
    }
    object CardWallCanScreen : Routes(NavigationRouteNames.CardWallCanScreen.name)
    object CardWallPinScreen : Routes(NavigationRouteNames.CardWallPinScreen.name)
    object CardWallSaveCredentialsScreen : Routes(NavigationRouteNames.CardWallSaveCredentialsScreen.name)
    object CardWallSaveCredentialsInfoScreen : Routes(NavigationRouteNames.CardWallSaveCredentialsInfoScreen.name)
    object CardWallReadCardScreen : Routes(NavigationRouteNames.CardWallReadCardScreen.name)
    object CardWallExternalAuthenticationScreen : Routes(NavigationRouteNames.CardWallExternalAuthenticationScreen.name)
}
