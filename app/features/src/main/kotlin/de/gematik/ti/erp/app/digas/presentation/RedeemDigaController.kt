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

package de.gematik.ti.erp.app.digas.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.authentication.model.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.DownloadResourcesWorker
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.digas.ui.model.RedeemEvent
import de.gematik.ti.erp.app.idp.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.pharmacy.model.orderID
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.redeem.model.BaseRedeemState
import de.gematik.ti.erp.app.redeem.model.DigaRedeemedPrescriptionState
import de.gematik.ti.erp.app.redeem.usecase.RedeemDigaUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val DOWNLOAD_WAIT_TIME_SECONDS = 10L

class RedeemDigaController(
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    biometricAuthenticator: BiometricAuthenticator,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    networkStatusTracker: NetworkStatusTracker,
    private val redeemDigaUseCase: RedeemDigaUseCase
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    getProfilesUseCase = getProfilesUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    biometricAuthenticator = biometricAuthenticator,
    networkStatusTracker = networkStatusTracker,
    onActiveProfileSuccess = { _, _ -> }
) {

    private val _isProfileRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _isRedeeming: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val showAuthenticationErrorDialog = ComposableEvent<AuthenticationResult.Error>()
    val onBiometricAuthenticationSuccessEvent = ComposableEvent<Unit>()
    val redeemEvent = ComposableEvent<RedeemEvent>()
    val onAutoDownloadEvent = ComposableEvent<Pair<WorkManager, WorkRequest>>()
    val isRedeeming: StateFlow<Boolean> = _isRedeeming.asStateFlow()

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

    fun redeem(
        profileId: ProfileIdentifier,
        isRedeemAgain: Boolean,
        taskId: String,
        context: Context,
        orderId: UUID = orderID()
    ) {
        controllerScope.launch {
            runCatching {
                redeemDigaUseCase.invoke(
                    arguments = RedeemDigaUseCase.RedeemDigaArguments(
                        profileId = profileId,
                        taskId = taskId,
                        orderId = orderId.toString(),
                        lifecycleHooks = RedeemDigaUseCase.RedeemDigaProgressState(
                            onRedeemStartState = { _isRedeeming.value = true },
                            onTelematikIdObtained = { },
                            onRedeemSuccess = { _isRedeeming.value = false },
                            onRedeemFailure = { _isRedeeming.value = false }
                        )
                    )
                )
            }.fold(onSuccess = { state ->
                when (state) {
                    BaseRedeemState.Success -> {
                        downloadResources(context, DOWNLOAD_WAIT_TIME_SECONDS)
                    }

                    is DigaRedeemedPrescriptionState.AlreadyRedeemed -> {
                        redeemEvent.trigger(RedeemEvent.AlreadyRedeemed)
                    }

                    is DigaRedeemedPrescriptionState.NotAvailableInDatabase -> {
                        redeemEvent.trigger(RedeemEvent.LocalError)
                    }

                    is DigaRedeemedPrescriptionState.NotAvailableInInsuranceDirectory -> {
                        redeemEvent.trigger(RedeemEvent.DirectoryError)
                    }

                    is BaseRedeemState.Error -> {
                        redeemEvent.trigger(RedeemEvent.HttpError(state.errorState))
                    }

                    else -> {
                        Napier.d { "redeem event $state should not occur" }
                        // not applicable at the moment
                    }
                }
            }, onFailure = { exception ->
                    redeemEvent.trigger(
                        RedeemEvent.HttpError(
                            HttpErrorState.ErrorWithCause(
                                exception.message ?: exception.stackTraceToString()
                            )
                        )
                    )
                })
        }
    }

    fun downloadResources(
        context: Context,
        delay: Long = 0L
    ) {
        controllerScope.launch {
            activeProfile.first { it.isDataState }.data?.let { activeProfile ->
                val workRequest = OneTimeWorkRequestBuilder<DownloadResourcesWorker>().setInitialDelay(delay, TimeUnit.SECONDS)
                    .setInputData(workDataOf("profileId" to activeProfile.id)).build()

                val uniqueWorkName = "DownloadResources-${activeProfile.id}"

                val workManager = WorkManager.getInstance(context)
                // provide the work request with a unique name
                workManager.enqueueUniqueWork(
                    uniqueWorkName,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )

                onAutoDownloadEvent.trigger(workManager to workRequest)
            }
        }
    }
}

@Composable
fun rememberRedeemDigasController(): RedeemDigaController {
    val redeemDigaUseCase by rememberInstance<RedeemDigaUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    return remember {
        RedeemDigaController(
            redeemDigaUseCase = redeemDigaUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            getProfilesUseCase = getProfilesUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            biometricAuthenticator = biometricAuthenticator,
            networkStatusTracker = networkStatusTracker
        )
    }
}
