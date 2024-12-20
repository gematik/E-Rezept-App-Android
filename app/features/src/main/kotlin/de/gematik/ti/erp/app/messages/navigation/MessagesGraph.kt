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

package de.gematik.ti.erp.app.messages.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.messages.presentation.ui.components.MessageBottomSheetScreen
import de.gematik.ti.erp.app.messages.presentation.ui.screens.MessageDetailScreen
import de.gematik.ti.erp.app.messages.presentation.ui.screens.MessageListScreen
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideOutUp

fun NavGraphBuilder.messagesGraph(
    startDestination: String = MessagesRoutes.MessageListScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = MessagesRoutes.subGraphName()
    ) {
        renderComposable(
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() },
            popExitAnimation = { slideOutUp() },
            route = MessagesRoutes.MessageListScreen.route,
            arguments = MessagesRoutes.MessageListScreen.arguments
        ) { navEntry ->
            MessageListScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = MessagesRoutes.MessageDetailScreen.route,
            arguments = MessagesRoutes.MessageDetailScreen.arguments
        ) { navEntry ->
            MessageDetailScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = MessagesRoutes.MessageBottomSheetScreen.route,
            arguments = MessagesRoutes.MessageBottomSheetScreen.arguments
        ) { navEntry ->
            MessageBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
