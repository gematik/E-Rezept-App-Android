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

package de.gematik.ti.erp.app.redeem.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.base.usecase.DownloadAllResourcesUseCase
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionRedeemArguments
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionRedeemArguments.DirectRedemptionArguments
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionRedeemArguments.LoggedInUserRedemptionArguments
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.redeem.model.BaseRedeemState
import de.gematik.ti.erp.app.redeem.model.PrescriptionReadinessResult
import de.gematik.ti.erp.app.redeem.model.RedeemReadyPrescriptionsState
import de.gematik.ti.erp.app.redeem.model.RedeemReadyPrescriptionsState.AllReady
import de.gematik.ti.erp.app.redeem.model.RedeemReadyPrescriptionsState.NoneReady
import de.gematik.ti.erp.app.redeem.model.RedeemReadyPrescriptionsState.SomeMissing
import de.gematik.ti.erp.app.redeem.model.RedeemablePrescriptionInfo.Companion.toPrescriptionInfo
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState.IncompleteOrder
import de.gematik.ti.erp.app.redeem.model.RedeemedPrescriptionState.InvalidOrder
import de.gematik.ti.erp.app.redeem.usecase.GetReadyPrescriptionsByTaskIdsUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemPrescriptionsOnDirectUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemPrescriptionsOnLoggedInUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.compose.rememberInstance

@Stable
class RedeemPrescriptionsController(
    private val redeemPrescriptionsOnLoggedInUseCase: RedeemPrescriptionsOnLoggedInUseCase,
    private val redeemPrescriptionsOnDirectUseCase: RedeemPrescriptionsOnDirectUseCase,
    private val downloadAllResourcesUseCase: DownloadAllResourcesUseCase,
    private val getReadyPrescriptionsByTaskIdsUseCase: GetReadyPrescriptionsByTaskIdsUseCase
) : Controller() {

    val onProcessStartEvent: ComposableEvent<Unit> = ComposableEvent()
    val onProcessEndEvent: ComposableEvent<Unit> = ComposableEvent()

    private var _redeemedState = MutableStateFlow<BaseRedeemState>(
        BaseRedeemState.Init
    )
    val redeemedState = _redeemedState.asStateFlow()

    // process prescription redemptions and update the state
    fun processPrescriptionRedemptions(
        arguments: PrescriptionRedeemArguments
    ) {
        arguments.onRedemptionState(
            directRedemptionBlock = {
                controllerScope.launch {
                    processPrescriptionForDirectRedemption(it)
                }
            },
            loggedInUserRedemptionBlock = { loggedInArguments ->
                controllerScope.launch {
                    downloadAllResourcesForProfile(
                        profileId = loggedInArguments.profile.id,
                        onDownloadProcessStarted = { onProcessStartEvent.trigger() }
                    )
                        .onSuccess {
                            val taskIds = loggedInArguments.prescriptionOrderInfos.map { it.taskId }
                            getReadyPrescriptionsByTaskIdsUseCase.invoke(taskIds)
                                .toRedeemReadyPrescriptionState(taskIds)
                                .let { state ->
                                    when (state) {
                                        is AllReady -> processPrescriptionRedemptionsForLoggedInUser(loggedInArguments)
                                        is NoneReady -> {
                                            onProcessEndEvent.trigger()
                                            _redeemedState.value = InvalidOrder(missingPrescriptionInfos = state.allPrescriptions)
                                        }

                                        is SomeMissing -> {
                                            onProcessEndEvent.trigger()
                                            _redeemedState.value = IncompleteOrder(state.missingPrescriptions)
                                        }
                                    }
                                }
                        }
                        .onFailure {
                            onProcessEndEvent.trigger()
                            Napier.e { "Failed to download resources for profile: ${loggedInArguments.profile.id} with error ${it.message}" }
                            _redeemedState.value = BaseRedeemState.Error(errorState = HttpErrorState.ErrorWithCause(it.message ?: ""))
                        }
                }
            }
        )
    }

    private suspend fun Flow<PrescriptionReadinessResult>.toRedeemReadyPrescriptionState(
        tasksInOrder: List<String>
    ): RedeemReadyPrescriptionsState {
        val readinessResult = first()
        val readyTaskIds = readinessResult.readyPrescriptions.map { it.taskId }.toSet()
        val missingTaskIds = tasksInOrder.filterNot { it in readyTaskIds }
        val notReadyPrescriptions = readinessResult.notReadyPrescriptions.map { it.toPrescriptionInfo() }
        val missingPrescriptions = notReadyPrescriptions.filter { it.taskId in missingTaskIds }

        return when {
            readyTaskIds.containsAll(tasksInOrder) && tasksInOrder.size == readyTaskIds.size -> AllReady
            readyTaskIds.isEmpty() -> NoneReady(notReadyPrescriptions)
            else -> SomeMissing(missingPrescriptions)
        }
    }

    private suspend fun downloadAllResourcesForProfile(
        profileId: ProfileIdentifier,
        onDownloadProcessStarted: () -> Unit
    ): Result<Unit> =
        withContext(controllerScope.coroutineContext) {
            runCatching {
                onDownloadProcessStarted()
                downloadAllResourcesUseCase(profileId).getOrThrow() // Throws if result is failure
                Result.success(Unit)
            }.getOrElse { error ->
                Napier.e(error) { "Failed to download resources for profile: $profileId" }
                Result.failure(error)
            }
        }

    private suspend fun processPrescriptionRedemptionsForLoggedInUser(
        arguments: LoggedInUserRedemptionArguments
    ) {
        redeemPrescriptionsOnLoggedInUseCase.invoke(
            orderId = arguments.orderId,
            profileId = arguments.profile.id,
            redeemOption = arguments.redeemOption,
            prescriptionOrderInfos = arguments.prescriptionOrderInfos,
            contact = arguments.contact,
            pharmacy = arguments.pharmacy,
            onRedeemProcessEnd = { onProcessEndEvent.trigger() }
        ).collectLatest { value ->
            _redeemedState.value = value
        }
    }

    @Requirement(
        "A_22778-01#1",
        "A_22779-01#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Start Redeem without TI (Controller)."
    )
    private suspend fun processPrescriptionForDirectRedemption(
        arguments: DirectRedemptionArguments
    ) {
        redeemPrescriptionsOnDirectUseCase.invoke(
            orderId = arguments.orderId,
            redeemOption = arguments.redeemOption,
            prescriptionOrderInfos = arguments.prescriptionOrderInfos,
            contact = arguments.contact,
            pharmacy = arguments.pharmacy,
            onRedeemProcessStart = { onProcessStartEvent.trigger() },
            onRedeemProcessEnd = { onProcessEndEvent.trigger() }
        ).collectLatest {
            _redeemedState.value = it
        }
    }
}

@Composable
fun rememberRedeemPrescriptionsController(): RedeemPrescriptionsController {
    val redeemPrescriptionsOnLoggedInUseCase by rememberInstance<RedeemPrescriptionsOnLoggedInUseCase>()
    val redeemPrescriptionsOnDirectUseCase by rememberInstance<RedeemPrescriptionsOnDirectUseCase>()
    val downloadAllResourcesUseCase by rememberInstance<DownloadAllResourcesUseCase>()
    val getReadyPrescriptionsByTaskIdsUseCase by rememberInstance<GetReadyPrescriptionsByTaskIdsUseCase>()

    return remember {
        RedeemPrescriptionsController(
            redeemPrescriptionsOnLoggedInUseCase = redeemPrescriptionsOnLoggedInUseCase,
            redeemPrescriptionsOnDirectUseCase = redeemPrescriptionsOnDirectUseCase,
            downloadAllResourcesUseCase = downloadAllResourcesUseCase,
            getReadyPrescriptionsByTaskIdsUseCase = getReadyPrescriptionsByTaskIdsUseCase
        )
    }
}
