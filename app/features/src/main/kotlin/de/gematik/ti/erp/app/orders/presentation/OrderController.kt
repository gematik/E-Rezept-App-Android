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

package de.gematik.ti.erp.app.orders.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.orders.usecase.GetOrdersUsingProfileIdUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.onEach
import org.kodein.di.compose.rememberInstance

@Stable
class OrderController(
    profileIdentifier: ProfileIdentifier,
    getOrdersUsingProfileIdUseCase: GetOrdersUsingProfileIdUseCase
) {
    enum class States {
        LoadingOrders,
        HasOrders,
        NoOrders
    }

    var state by mutableStateOf(States.LoadingOrders)
        private set

    private val orderFlow = getOrdersUsingProfileIdUseCase
        .invoke(profileIdentifier)
        .onEach {
            state = if (it.isEmpty()) {
                States.NoOrders
            } else {
                States.HasOrders
            }
        }

    val orders
        @Composable
        get() = orderFlow.collectAsStateWithLifecycle(emptyList())
}

@Composable
fun rememberOrderState(
    profileIdentifier: ProfileIdentifier
): OrderController {
    val getOrdersUsingProfileIdUseCase by rememberInstance<GetOrdersUsingProfileIdUseCase>()
    return remember(profileIdentifier) {
        OrderController(profileIdentifier, getOrdersUsingProfileIdUseCase)
    }
}
