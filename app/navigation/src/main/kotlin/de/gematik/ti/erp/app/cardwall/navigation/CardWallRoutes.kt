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

package de.gematik.ti.erp.app.cardwall.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.authentication.model.GidNavigationData
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.navigation.fromNavigationString
import de.gematik.ti.erp.app.navigation.toNavigationString
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier

object CardWallRoutes : NavigationRoutes {
    override fun subGraphName() = "cardwall"
    const val CARD_WALL_NAV_PROFILE_ID = "profileId"
    const val CARD_WALL_PIN_NAV_PROFILE_ID = "cardwall-profileId"
    const val GID_AUTH_INFORMATION = "gidAuthInformation"
    const val CARD_WALL_NAV_CAN = "cardwall-can"

    object CardWallIntroScreen : Routes(
        NavigationRouteNames.CardWallIntroScreen.name,
        navArgument(CARD_WALL_NAV_PROFILE_ID) { type = androidx.navigation.NavType.StringType },
        navArgument(GID_AUTH_INFORMATION) { type = androidx.navigation.NavType.StringType }
    ) {
        fun path(profileIdentifier: ProfileIdentifier) = path(
            CARD_WALL_NAV_PROFILE_ID to profileIdentifier,
            GID_AUTH_INFORMATION to ""
        )

        fun pathWithGid(
            gidNavigationData: GidNavigationData
        ) = path(
            CARD_WALL_NAV_PROFILE_ID to gidNavigationData.profileId,
            GID_AUTH_INFORMATION to gidNavigationData.toNavigationString()
        )
    }

    object CardWallCanScreen : Routes(NavigationRouteNames.CardWallCanScreen.name)
    object CardWallPinScreen : Routes(
        NavigationRouteNames.CardWallPinScreen.name,
        navArgument(CARD_WALL_NAV_CAN) { type = androidx.navigation.NavType.StringType },
        navArgument(CARD_WALL_PIN_NAV_PROFILE_ID) { type = androidx.navigation.NavType.StringType }
    ) {
        fun path(
            profileIdentifier: ProfileIdentifier,
            can: String
        ) = path(
            CARD_WALL_PIN_NAV_PROFILE_ID to profileIdentifier,
            CARD_WALL_NAV_CAN to can
        )
    }
    object CardWallSaveCredentialsScreen : Routes(NavigationRouteNames.CardWallSaveCredentialsScreen.name)
    object CardWallSaveCredentialsInfoScreen : Routes(NavigationRouteNames.CardWallSaveCredentialsInfoScreen.name)
    object CardWallReadCardScreen : Routes(NavigationRouteNames.CardWallReadCardScreen.name)
    object CardWallGidListScreen : Routes(NavigationRouteNames.CardWallGidListScreen.name)
    object CardWallGidHelpScreen : Routes(NavigationRouteNames.CardWallGidHelpScreen.name)

    fun NavBackStackEntry.processGidEventData(): GidNavigationData? {
        val authInfo = arguments?.getString(GID_AUTH_INFORMATION)
        return authInfo?.takeIf { it.isNotEmpty() }?.let { fromNavigationString<GidNavigationData>(it) }
    }
}
