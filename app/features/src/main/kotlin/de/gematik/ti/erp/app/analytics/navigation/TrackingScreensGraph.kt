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

package de.gematik.ti.erp.app.analytics.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.analytics.ui.DemoTrackerScreen
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideOutUp

fun NavGraphBuilder.trackingGraph(
    navController: NavController
) {
    navigation(
        startDestination = TrackingScreenRoutes.DemoTrackerScreen.route,
        route = TrackingScreenRoutes.subGraphName()
    ) {
        renderComposable(
            route = TrackingScreenRoutes.DemoTrackerScreen.route,
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() }
        ) {
            DemoTrackerScreen(
                navController,
                it
            )
        }
    }
}
