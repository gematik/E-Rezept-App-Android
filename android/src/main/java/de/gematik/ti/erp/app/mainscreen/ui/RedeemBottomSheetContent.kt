/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.featuretoggle.Features
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.compose.BottomSheetAction
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import org.kodein.di.compose.rememberViewModel

interface RedeemStateBridge {
    fun scannedTasks(profileIdentifier: ProfileIdentifier): Flow<List<ScannedTaskData.ScannedTask>>
    fun syncedTasks(profileIdentifier: ProfileIdentifier): Flow<List<SyncedTaskData.SyncedTask>>
    fun allowRedeemWithoutTiFeatureEnabled(): Flow<Boolean>
}

class RedeemStateViewModel(
    private val prescriptionUseCase: PrescriptionUseCase,
    private val toggleManager: FeatureToggleManager
) : ViewModel(), RedeemStateBridge {
    override fun scannedTasks(profileIdentifier: ProfileIdentifier) =
        prescriptionUseCase.scannedTasks(profileIdentifier).shareIn(viewModelScope, SharingStarted.Eagerly)

    override fun syncedTasks(profileIdentifier: ProfileIdentifier) =
        prescriptionUseCase.syncedTasks(profileIdentifier).shareIn(viewModelScope, SharingStarted.Eagerly)

    override fun allowRedeemWithoutTiFeatureEnabled() =
        toggleManager.isFeatureEnabled(Features.REDEEM_WITHOUT_TI.featureName)
}

@Stable
class RedeemState(
    private val redeemStateBridge: RedeemStateBridge
) {
    @Stable
    private class InternalState(
        val onPremiseRedeemableTaskIds: List<String>,
        val onlineRedeemableTaskIds: List<String>,
        val redeemedMedicationNames: List<String>
    )

    private var internalState by mutableStateOf(InternalState(emptyList(), emptyList(), emptyList()))

    val localTaskIds by derivedStateOf { internalState.onPremiseRedeemableTaskIds }

    val onlineTaskIds by derivedStateOf { internalState.onlineRedeemableTaskIds }

    val alreadyRedeemedMedications by derivedStateOf { internalState.redeemedMedicationNames }

    val hasRedeemableTasks by derivedStateOf { onlineTaskIds.isNotEmpty() || localTaskIds.isNotEmpty() }

    suspend fun produceState(profileIdentifier: ProfileIdentifier) {
        combine(
            redeemStateBridge.allowRedeemWithoutTiFeatureEnabled(),
            redeemStateBridge.scannedTasks(profileIdentifier),
            redeemStateBridge.syncedTasks(profileIdentifier)
        ) { allowWithout, scannedTasks, syncedTasks ->
            val redeemableSyncedTasks = syncedTasks
                .asSequence()
                .filter {
                    it.redeemState().isRedeemable()
                }

            val alreadyRedeemedSyncedTasks = syncedTasks
                .asSequence()
                .filter {
                    it.redeemState() == SyncedTaskData.SyncedTask.RedeemState.RedeemableAfterDelta
                }
                .map {
                    it.medicationRequestMedicationName() ?: ""
                }
                .take(2) // we only require at least two

            val allRedeemableTasks =
                scannedTasks.filter { it.isRedeemable() }.map { it.taskId } + redeemableSyncedTasks.map { it.taskId }

            InternalState(
                onPremiseRedeemableTaskIds = allRedeemableTasks,
                onlineRedeemableTaskIds = if (allowWithout) {
                    allRedeemableTasks
                } else {
                    redeemableSyncedTasks.map { it.taskId }.toList()
                },
                redeemedMedicationNames = alreadyRedeemedSyncedTasks.toList()
            )
        }.collect {
            internalState = it
        }
    }
}

@Composable
fun rememberRedeemState(profile: ProfilesUseCaseData.Profile): RedeemState {
    val redeemStateViewModel by rememberViewModel<RedeemStateViewModel>()
    val state = remember { RedeemState(redeemStateViewModel) }
    LaunchedEffect(profile.id) {
        state.produceState(profile.id)
    }
    return state
}

@Composable
fun RedeemBottomSheetContent(
    redeemState: RedeemState,
    onClickLocalRedeem: (taskIds: List<String>) -> Unit,
    onClickOnlineRedeem: (taskIds: List<String>) -> Unit
) {
    val onlineRedeemButtonEnabled by derivedStateOf {
        redeemState.onlineTaskIds.isNotEmpty()
    }

    val shouldShowAlreadySentDialog by derivedStateOf {
        redeemState.alreadyRedeemedMedications.isNotEmpty()
    }

    var showAlreadySentDialog by remember { mutableStateOf(false) }

    if (showAlreadySentDialog) {
        SendTasksAgainDialog(
            redeemedMedicationNames = redeemState.alreadyRedeemedMedications,
            onSendAgain = {
                onClickOnlineRedeem(redeemState.onlineTaskIds)
                showAlreadySentDialog = false
            },
            onCancel = {
                showAlreadySentDialog = false
            }
        )
    }

    BottomSheetAction(
        icon = Icons.Rounded.QrCode,
        title = stringResource(R.string.dialog_redeem_headline),
        info = stringResource(R.string.dialog_redeem_info),
        modifier = Modifier.testTag("main/redeemInLocalPharmacyButton")
    ) {
        onClickLocalRedeem(redeemState.localTaskIds)
    }

    BottomSheetAction(
        enabled = onlineRedeemButtonEnabled,
        icon = Icons.Rounded.ShoppingBag,
        title = stringResource(R.string.dialog_order_headline),
        info = stringResource(R.string.dialog_order_info),
        modifier = Modifier.testTag("main/redeemRemoteButton")
    ) {
        if (shouldShowAlreadySentDialog) {
            showAlreadySentDialog = true
        } else {
            onClickOnlineRedeem(redeemState.onlineTaskIds)
        }
    }

    Box(Modifier.navigationBarsPadding())
}

@Composable
fun SendTasksAgainDialog(
    redeemedMedicationNames: List<String>,
    onSendAgain: () -> Unit,
    onCancel: () -> Unit
) {
    val medication = remember(redeemedMedicationNames) { redeemedMedicationNames.first() }

    val taskAlreadySentInfo = buildAnnotatedString {
        append(
            annotatedPluralsResource(
                R.plurals.task_already_sent_info,
                redeemedMedicationNames.size,
                AnnotatedString(medication)
            )
        )
        append("\n\n")
        append(stringResource(R.string.task_already_sent_sub_info))
    }

    CommonAlertDialog(
        header = AnnotatedString(stringResource(R.string.task_already_sent_header)),
        info = taskAlreadySentInfo,
        cancelText = stringResource(R.string.cancel_sent_task_again),
        actionText = stringResource(R.string.sent_task_again),
        onCancel = onCancel,
        onClickAction = onSendAgain
    )
}
