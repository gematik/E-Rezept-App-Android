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

package de.gematik.ti.erp.app.cardunlock.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object CardUnlockRoutes : NavigationRoutes {
    override fun subGraphName() = "cardUnlock"
    const val CARD_UNLOCK_NAV_UNLOCK_METHOD = "unlockMethod"

    object CardUnlockIntroScreen : Routes(
        NavigationRouteNames.CardUnlockIntroScreen.name,
        navArgument(CARD_UNLOCK_NAV_UNLOCK_METHOD) { type = NavType.StringType }
    ) {
        fun path(unlockMethod: String) = path(CARD_UNLOCK_NAV_UNLOCK_METHOD to unlockMethod)
    }

    object CardUnlockCanScreen : Routes(NavigationRouteNames.CardUnlockCanScreen.name)

    object CardUnlockPukScreen : Routes(NavigationRouteNames.CardUnlockPukScreen.name)

    object CardUnlockOldSecretScreen : Routes(NavigationRouteNames.CardUnlockOldSecretScreen.name)

    object CardUnlockNewSecretScreen : Routes(NavigationRouteNames.CardUnlockNewSecretScreen.name)

    object CardUnlockEgkScreen : Routes(NavigationRouteNames.CardUnlockEgkScreen.name)
}
