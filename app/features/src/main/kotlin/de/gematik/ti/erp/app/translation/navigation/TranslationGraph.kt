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

package de.gematik.ti.erp.app.translation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.translation.ui.screens.TranslationConsentBottomSheetScreen
import de.gematik.ti.erp.app.translation.ui.screens.TranslationPickLanguageScreen
import de.gematik.ti.erp.app.translation.ui.screens.TranslationSettingsScreen

fun NavGraphBuilder.translationGraph(
    navController: NavController
) {
    navigation(
        startDestination = TranslationRoutes.TranslationSettingsScreen.route,
        route = TranslationRoutes.subGraphName()
    ) {
        renderComposable(
            route = TranslationRoutes.TranslationSettingsScreen.route,
            arguments = TranslationRoutes.TranslationSettingsScreen.arguments
        ) {
            TranslationSettingsScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }

        renderBottomSheet(
            route = TranslationRoutes.TranslationConsentBottomSheetScreen.route,
            arguments = TranslationRoutes.TranslationConsentBottomSheetScreen.arguments
        ) {
            TranslationConsentBottomSheetScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }

        renderComposable(
            route = TranslationRoutes.TranslationPickLanguageScreen.route,
            arguments = TranslationRoutes.TranslationPickLanguageScreen.arguments
        ) {
            TranslationPickLanguageScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
    }
}
