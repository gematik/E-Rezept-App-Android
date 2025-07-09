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

package de.gematik.ti.erp.app.debugsettings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.debugsettings.showcase.ui.screens.BottomSheetShowcaseScreen
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideOutUp

/**
 * This graph shows the different UI components that are possible in the app.
 * The different dimensions and states of them.
 * Every new screen added here is an example of a new component and some information about it
 */
fun NavGraphBuilder.showcaseScreensGraph(
    navController: NavController
) {
    navigation(
        startDestination = ShowcaseScreensRoutes.BottomSheetShowcaseScreen.route,
        route = ShowcaseScreensRoutes.subGraphName()
    ) {
        renderComposable(
            route = ShowcaseScreensRoutes.BottomSheetShowcaseScreen.route,
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() }
        ) {
            BottomSheetShowcaseScreen(
                navController,
                it
            )
        }
    }
}
