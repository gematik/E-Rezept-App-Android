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

package de.gematik.ti.erp.app.profiles.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import de.gematik.ti.erp.app.cardwall.mini.ui.Authenticator
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.prescription.ui.GeneralErrorState
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.presentation.catchAndTransformRemoteExceptions
import de.gematik.ti.erp.app.prescription.presentation.retryWithAuthenticator
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import de.gematik.ti.erp.app.protocol.usecase.AuditEventsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import org.kodein.di.compose.rememberInstance

@Stable
class AuditEventsController(
    private val auditEventsUseCase: AuditEventsUseCase,
    private val authenticator: Authenticator,
    coroutineScope: CoroutineScope
) {

    private val searchChannelFlow = MutableStateFlow("")

    var profileId by mutableStateOf("")
        private set

    @Stable
    sealed interface State : PrescriptionServiceState {
        @Stable
        object Loading : State

        @Stable
        data class AuditEvents(val auditEvents: List<AuditEventData.AuditEvent>) : State
    }

    private var isLoading by mutableStateOf(false)

    suspend fun refresh(
        profileId: ProfileIdentifier,
        isUserAction: Boolean,
        onUserNotAuthenticated: () -> Unit,
        onShowCardWall: () -> Unit
    ) {
        val finalState = refreshFlow(
            profileId = profileId,
            isUserAction = isUserAction
        ).cancellable().first()

        when (finalState) {
            GeneralErrorState.NoneEnrolled -> {
                onShowCardWall()
            }
            GeneralErrorState.UserNotAuthenticated -> {
                onUserNotAuthenticated()
            }
            else -> {
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val auditEventPagingFlow: Flow<PagingData<AuditEventData.AuditEvent>> =
        searchChannelFlow
            .filterNotNull()
            .onEach {
                profileId = it
            }
            .flatMapLatest { searchData ->
                isLoading = true

                auditEventsUseCase.loadAuditEventsPaged(searchData)
                    .onEach {
                        isLoading = false
                    }
                    .flowOn(Dispatchers.IO)
                    .shareIn(
                        coroutineScope,
                        SharingStarted.WhileSubscribed(),
                        1
                    )
            }

    private fun refreshFlow(
        profileId: ProfileIdentifier,
        isUserAction: Boolean
    ): Flow<PrescriptionServiceState> =
        flow {
            searchChannelFlow.emit(profileId)
            emit(State.Loading)
            val auditEvents = auditEventsUseCase.loadAuditEvents(profileId)
            emit(State.AuditEvents(auditEvents))
        }
            .retryWithAuthenticator(
                isUserAction = isUserAction,
                authenticate = authenticator.authenticateForPrescriptions(profileId)
            )
            .catchAndTransformRemoteExceptions()
            .flowOn(Dispatchers.IO)
}

@Composable
fun rememberAuditEventsController(): AuditEventsController {
    val auditEventsUseCase by rememberInstance<AuditEventsUseCase>()
    val authenticator = LocalAuthenticator.current
    val scope = rememberCoroutineScope()

    return remember {
        AuditEventsController(
            auditEventsUseCase = auditEventsUseCase,
            authenticator = authenticator,
            coroutineScope = scope
        )
    }
}
