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

package de.gematik.ti.erp.app.eurezept.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.authentication.presentation.AuthReason
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.ObserveNavigationTriggerUseCase
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.eurezept.ui.model.EuRedeemSelector.WAS_EU_REDEEM_INSTRUCTION_VIEWED
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

internal class EuRedeemScreenController(
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    observeNavigationTriggerUseCase: ObserveNavigationTriggerUseCase,
    networkStatusTracker: NetworkStatusTracker,
    biometricAuthenticator: BiometricAuthenticator
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator
) {
    val onBiometricAuthenticationSuccessForSubmitEvent = ComposableEvent<Unit>()

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) { reason ->
            if (reason == AuthReason.SUBMIT) {
                onBiometricAuthenticationSuccessForSubmitEvent.trigger()
            }
        }
    }

    /**
     * Handles the redeem button click.
     * Navigates to instructions if not seen, otherwise checks authentication and starts redemption if possible.
     *
     * @param onStartRedemption Called when redemption can start immediately.
     * @param onShowInstructions Called when the user needs to see the instructions first.
     */
    fun handleRedeemAction(
        onStartRedemption: () -> Unit,
        onShowInstructions: () -> Unit
    ) {
        controllerScope.launch {
            if (wasRedeemInstructionNotViewed()) {
                onShowInstructions()
                return@launch
            }

            val profile = activeProfile.value.data
            if (profile?.isSSOTokenValid() == true) {
                onStartRedemption()
            } else {
                profile?.let { chooseAuthenticationMethod(it) }
            }
        }
    }

    /**
     * Returns true if the user has not seen the redeem instructions yet.
     */
    suspend fun wasRedeemInstructionNotViewed(): Boolean =
        wasRedeemInstructionViewed.firstOrNull() != true

    private val wasRedeemInstructionViewed: Flow<Boolean> by lazy {
        observeNavigationTriggerUseCase.invoke(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
    }
}

@Composable
internal fun rememberEuRedeemScreenController(): EuRedeemScreenController {
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()

    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val observeNavigationTriggerUseCase by rememberInstance<ObserveNavigationTriggerUseCase>()

    return remember {
        EuRedeemScreenController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            observeNavigationTriggerUseCase = observeNavigationTriggerUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator
        )
    }
}
