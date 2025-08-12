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

package de.gematik.ti.erp.app.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/**
 * Retrieves a [ViewModel] scoped to the navigation graph identified by [graphRoute],
 * ensuring that the ViewModel instance is shared across all destinations within that graph.
 *
 * This is particularly useful when multiple screens (e.g., in a nested navigation graph) need to
 * share a common [ViewModel] for state or coordination. It allows creating a scoped ViewModel
 * with a custom factory, rather than relying on Hilt or the default ViewModelProvider.
 *
 * ### Example usage:
 * ```
 * val graphController = rememberGraphScopedViewModel(
 *     navController = navController,
 *     navEntry = navBackStackEntry,
 *     graphRoute = "mainGraph"
 * ) {
 *     MyGraphScopedViewModel(...)
 * }
 * ```
 *
 * @param VM The type of [ViewModel] to retrieve.
 * @param navController The [NavController] used to retrieve the graph's back stack entry.
 * @param navEntry The current [NavBackStackEntry] used to scope recomposition and remember the parent entry.
 * @param graphRoute The route of the navigation graph to which the [ViewModel] should be scoped.
 * @param creator A lambda to create a new instance of the [ViewModel] using a custom factory.
 *
 * @return A [ViewModel] instance scoped to the given [graphRoute], remembered across recompositions.
 */
@Composable
inline fun <reified VM : ViewModel> rememberGraphScopedViewModel(
    navController: NavController,
    navEntry: NavBackStackEntry,
    graphRoute: String,
    noinline creator: @DisallowComposableCalls () -> VM
): VM {
    // scope to the graph’s entry
    val parentEntry = remember(navEntry) {
        navController.getBackStackEntry(graphRoute)
    }

    val factory = remember(creator) {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return creator() as T
            }
        }
    }

    return viewModel(parentEntry, factory = factory)
}
