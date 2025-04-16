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

package de.gematik.ti.erp.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import io.github.aakira.napier.Napier

// This is a composable function that takes a NavBackStackEntry and a route and an action to be executed when
@Suppress("ComposableNaming")
@Composable
fun NavBackStackEntry.onReturnAction(
    route: Routes,
    onReturnAction: () -> Unit
) {
    LaunchedEffect(this) {
        Napier.d { "on returning to ${destination.route} looking for ${route.route}" }
        if (destination.route == route.route) {
            onReturnAction()
        }
    }
}
