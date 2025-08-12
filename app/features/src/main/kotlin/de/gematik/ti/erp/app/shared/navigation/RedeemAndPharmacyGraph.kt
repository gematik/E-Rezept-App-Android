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

package de.gematik.ti.erp.app.shared.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.pharmacy.navigation.pharmacyGraph
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.redeem.navigation.redeemGraph
import org.kodein.di.DI

/**
 * Defines a shared navigation graph that includes both the [redeemGraph] and [pharmacyGraph].
 *
 * This graph is useful for scenarios where both redemption and pharmacy flows need to access shared state,
 * such as a shared [de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemSharedViewModel],
 * without exposing it to the rest of the app.
 *
 * By nesting both graphs under a common route, view models scoped to [RedeemAndPharmacyGraphRoutes.subGraphName]
 * will be retained across navigation between redemption and pharmacy-related screens, but will reset when exiting
 * to other parts of the app.
 *
 * @param dependencyInjector The DI container providing dependencies for the pharmacy graph, which needs to be modified.
 * @param navController The [NavController] used for navigating within the graph.
 * @param startDestination The initial route when this graph is entered. Defaults to the "How to Redeem" screen.
 */
fun NavGraphBuilder.redeemAndPharmacySharedGraph(
    dependencyInjector: DI,
    navController: NavController,
    startDestination: String = RedeemRoutes.HowToRedeemScreen.route
) {
    navigation(
        startDestination = startDestination,
        route = RedeemAndPharmacyGraphRoutes.subGraphName()
    ) {
        redeemGraph(navController = navController)
        pharmacyGraph(
            dependencyInjector = dependencyInjector,
            navController = navController
        )
    }
}

object RedeemAndPharmacyGraphRoutes : NavigationRoutes {
    override fun subGraphName(): String = "redeemAndPharmacy"
}
