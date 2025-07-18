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
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.authentication.model.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.idp.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.redeem.model.ErrorOnRedeemablePrescriptionDialogParameters
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionDialogMessageState
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.compose.rememberInstance

class RedeemOrderOverviewScreenController(
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    biometricAuthenticator: BiometricAuthenticator,
    networkStatusTracker: NetworkStatusTracker
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    getProfilesUseCase = getProfilesUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator
) {
    private val _isProfileRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _orderHasError = MutableStateFlow(false)
    private val _isLoadingIndicatorShown = MutableStateFlow(false)

    val showErrorOnRedeemAlertDialogEvent = ComposableEvent<ErrorOnRedeemablePrescriptionDialogParameters>()
    val showPrescriptionRedeemAlertDialogEvent = ComposableEvent<RedeemPrescriptionDialogMessageState>()
    val onBiometricAuthenticationSuccessEvent = ComposableEvent<Unit>()
    val showAuthenticationErrorDialog = ComposableEvent<AuthenticationResult.Error>()

    val isProfileRefreshing = _isProfileRefreshing.asStateFlow()
    val orderHasError: StateFlow<Boolean> = _orderHasError.asStateFlow()
    val isLoadingIndicatorShown: StateFlow<Boolean> = _isLoadingIndicatorShown.asStateFlow()

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) {
            onBiometricAuthenticationSuccessEvent.trigger()
        }

        biometricAuthenticationResetErrorEvent.listen(controllerScope) { error ->
            showAuthenticationErrorDialog.trigger(error)
        }

        biometricAuthenticationOtherErrorEvent.listen(controllerScope) { error ->
            showAuthenticationErrorDialog.trigger(error)
        }

        onRefreshProfileAction.listen(controllerScope) { isRefreshing ->
            _isProfileRefreshing.value = isRefreshing
        }
    }

    fun toggleOrderHasError(state: Boolean) { _orderHasError.value = state }

    fun disableLoadingIndicator() { _isLoadingIndicatorShown.value = false }

    fun enableLoadingIndicator() { _isLoadingIndicatorShown.value = true }

    fun toggleLoadingIndicator(state: Boolean) { _isLoadingIndicatorShown.value = state }
}

@Composable
fun rememberOrderOverviewScreenController(): RedeemOrderOverviewScreenController {
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()

    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()

    return remember {
        RedeemOrderOverviewScreenController(
            biometricAuthenticator = biometricAuthenticator,
            networkStatusTracker = networkStatusTracker,
            getProfilesUseCase = getProfilesUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase
        )
    }
}
