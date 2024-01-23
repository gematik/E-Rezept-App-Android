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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.orders.usecase.GetOrderUsingOrderIdUseCase
import de.gematik.ti.erp.app.orders.usecase.GetRepliedMessagesUseCase
import de.gematik.ti.erp.app.orders.usecase.UpdateCommunicationByCommunicationIdUseCase
import de.gematik.ti.erp.app.orders.usecase.UpdateCommunicationByOrderIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import org.kodein.di.compose.rememberInstance

@Stable
class MessageController(
    orderId: String,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getRepliedMessagesUseCase: GetRepliedMessagesUseCase,
    private val getOrderUsingOrderIdUseCase: GetOrderUsingOrderIdUseCase,
    private val updateCommunicationByCommunicationIdUseCase: UpdateCommunicationByCommunicationIdUseCase,
    private val updateCommunicationByOrderIdUseCase: UpdateCommunicationByOrderIdUseCase,
    coroutineScope: CoroutineScope
) {
    enum class States {
        LoadingMessages,
        HasMessages,
        NoMessages
    }

    var state by mutableStateOf(States.LoadingMessages)
        private set

    val activeProfile by lazy {
        getActiveProfileUseCase()
    }

    private val messageFlow = getRepliedMessagesUseCase
        .invoke(orderId)
        .onEach {
            state = if (it.isEmpty()) {
                States.NoMessages
            } else {
                States.HasMessages
            }
            Napier.d("state is $state")
        }
        .shareIn(coroutineScope, SharingStarted.Lazily, 1)

    val messages
        @Composable
        get() = messageFlow
            .collectAsStateWithLifecycle(emptyList())

    private val orderFlow = getOrderUsingOrderIdUseCase
        .invoke(orderId)
        .shareIn(coroutineScope, SharingStarted.Lazily, 1)

    val order
        @Composable
        get() = orderFlow
            .collectAsStateWithLifecycle(null)

    val activeProfileState
        @Composable
        get() = activeProfile.collectAsStateWithLifecycle(null)

    suspend fun consumeAllMessages() {
        withContext(NonCancellable) {
            orderFlow.first()?.let {
                if (it.hasUnreadMessages) {
                    updateCommunicationByOrderIdUseCase.invoke(it.orderId)
                    messageFlow.first().forEach {
                        updateCommunicationByCommunicationIdUseCase.invoke(it.communicationId)
                    }
                }
            }
        }
    }
}

@Composable
fun rememberMessageController(
    orderId: String
): MessageController {
    val coroutineScope = rememberCoroutineScope()
    val getActiveProfileUseCase: GetActiveProfileUseCase by rememberInstance()
    val getRepliedMessagesUseCase: GetRepliedMessagesUseCase by rememberInstance()
    val getOrderUsingOrderIdUseCase: GetOrderUsingOrderIdUseCase by rememberInstance()
    val updateCommunicationByCommunicationIdUseCase: UpdateCommunicationByCommunicationIdUseCase by rememberInstance()
    val updateCommunicationByOrderIdUseCase: UpdateCommunicationByOrderIdUseCase by rememberInstance()

    return remember(orderId) {
        MessageController(
            orderId = orderId,
            getActiveProfileUseCase = getActiveProfileUseCase,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getOrderUsingOrderIdUseCase = getOrderUsingOrderIdUseCase,
            updateCommunicationByCommunicationIdUseCase = updateCommunicationByCommunicationIdUseCase,
            updateCommunicationByOrderIdUseCase = updateCommunicationByOrderIdUseCase,
            coroutineScope = coroutineScope
        )
    }
}
