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

package de.gematik.ti.erp.app.profiles.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.protocol.usecase.AuditEventsUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import org.kodein.di.compose.rememberInstance

@Requirement(
    "A_19177#3",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The audit events are displayed in a paged manner."
)
@Stable
class AuditEventsController(
    selectedProfileId: ProfileIdentifier,
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    authenticator: BiometricAuthenticator,
    networkStatusTracker: NetworkStatusTracker,
    private val auditEventsUseCase: AuditEventsUseCase
) : ChooseAuthenticationController(
    profileId = selectedProfileId,
    getProfileByIdUseCase = getProfileByIdUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    getProfilesUseCase = getProfilesUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = authenticator
) {
    val refreshStartedEvent = ComposableEvent<Unit>()

    private val refreshTrigger = MutableStateFlow(false)

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) { refreshAuditEvents() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val auditEvents by lazy {
        refreshTrigger
            .flatMapLatest {
                auditEventsUseCase.invoke(selectedProfileId)
                    .onEach { refreshStartedEvent.trigger() }
                    .shareIn(
                        scope = controllerScope,
                        started = SharingStarted.WhileSubscribed(),
                        replay = 1
                    )
            }
    }

    fun refreshAuditEvents() {
        refreshTrigger.value = !refreshTrigger.value
    }
}

@Composable
fun rememberAuditEventsController(
    profileId: ProfileIdentifier
): AuditEventsController {
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val auditEventsUseCase by rememberInstance<AuditEventsUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val authenticator = LocalBiometricAuthenticator.current

    return remember {
        AuditEventsController(
            selectedProfileId = profileId,
            getProfilesUseCase = getProfilesUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            auditEventsUseCase = auditEventsUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            authenticator = authenticator
        )
    }
}
