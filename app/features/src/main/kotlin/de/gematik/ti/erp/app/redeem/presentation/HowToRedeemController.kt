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

package de.gematik.ti.erp.app.redeem.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.authentication.presentation.AuthReason
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.IsFeatureToggleEnabledUseCase
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.database.datastore.featuretoggle.EU_REDEEM
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.redeem.usecase.HasEuRedeemablePrescriptionsUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.compose.rememberInstance
import androidx.compose.runtime.State as ComposeState

@Suppress("ConstructorParameterNaming")
@Stable
class HowToRedeemController(
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    networkStatusTracker: NetworkStatusTracker,
    biometricAuthenticator: BiometricAuthenticator,
    private val hasEuRedeemablePrescriptionsUseCase: HasEuRedeemablePrescriptionsUseCase,
    isFeatureToggleEnabledUseCase: IsFeatureToggleEnabledUseCase
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator
) {
    val euRedeemFeatureFlag: StateFlow<Boolean> =
        isFeatureToggleEnabledUseCase(EU_REDEEM)
            .stateIn(
                controllerScope,
                SharingStarted.WhileSubscribed(),
                false
            )

    val onBiometricAuthenticationSuccessForSubmitEvent = ComposableEvent<Unit>()

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) { reason ->
            if (reason == AuthReason.SUBMIT) {
                onBiometricAuthenticationSuccessForSubmitEvent.trigger()
            }
        }
    }

    fun onEuConsentClick(): Boolean =
        activeProfile.value.data?.let { profile ->
            profile.isSSOTokenValid().also { authenticated ->
                if (!authenticated) chooseAuthenticationMethod(profile)
            }
        } ?: false

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hasEuRedeemablePrescriptionsFlow: Flow<Boolean> =
        activeProfile
            .map { it.data?.id }
            .distinctUntilChanged()
            .flatMapLatest { id ->
                id?.let { hasEuRedeemablePrescriptionsUseCase(it) } ?: flowOf(false)
            }

    val hasEuRedeemablePrescriptions: ComposeState<Boolean>
        @Composable
        get() = hasEuRedeemablePrescriptionsFlow.collectAsStateWithLifecycle(false)
}

@Composable
fun rememberHowToRedeemController(): HowToRedeemController {
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val hasEuRedeemablePrescriptionsUseCase by rememberInstance<HasEuRedeemablePrescriptionsUseCase>()
    val isFeatureToggleEnabledUseCase by rememberInstance<IsFeatureToggleEnabledUseCase>()

    return remember {
        HowToRedeemController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator,
            hasEuRedeemablePrescriptionsUseCase = hasEuRedeemablePrescriptionsUseCase,
            isFeatureToggleEnabledUseCase = isFeatureToggleEnabledUseCase
        )
    }
}
