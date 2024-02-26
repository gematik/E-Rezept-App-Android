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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.LocalAuthenticator
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pkv.ui.CheckConsentState
import de.gematik.ti.erp.app.pkv.ui.GrantConsentDialog
import de.gematik.ti.erp.app.pkv.ui.onGrantConsent
import de.gematik.ti.erp.app.pkv.ui.rememberDeprecatedConsentController
import de.gematik.ti.erp.app.prescription.detail.presentation.PrescriptionDetailController
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberSharePrescriptionController
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceErrorState
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import kotlinx.coroutines.launch

@Composable
fun PrescriptionDetailScreenScaffold(
    activeProfile: ProfilesUseCaseData.Profile,
    scaffoldState: ScaffoldState,
    listState: LazyListState,
    prescriptionDetailController: PrescriptionDetailController,
    prescription: PrescriptionData.Prescription?,
    navController: NavController,
    onClickMedication: (PrescriptionData.Medication) -> Unit,
    onChangeSheetContent: (PrescriptionDetailBottomSheetContent?) -> Unit,
    onGrantConsent: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ssoTokenValid = rememberSaveable(activeProfile.ssoTokenScope) {
        activeProfile.isSSOTokenValid()
    }
    // TODO: (Fail on design pattern) Need to add this to the controller in the screen level
    val consentController = rememberDeprecatedConsentController(profile = activeProfile)
    val shareHandler = rememberSharePrescriptionController(activeProfile.id)

    var consentGranted: Boolean? by remember { mutableStateOf(null) }

    var showGrantConsentDialog by remember { mutableStateOf(false) }

    // TODO: Move check to viewmodel
    CheckConsentState(consentController, ssoTokenValid, scaffoldState, context) {
        consentGranted = it
    }
    if (showGrantConsentDialog && activeProfile.isSSOTokenValid()) {
        GrantConsentDialog(
            onCancel = onBack
        ) {
            onGrantConsent(context, scope, consentController, scaffoldState) {
                onGrantConsent()
                consentGranted = it
                showGrantConsentDialog = false
            }
        }
    }
    AnimatedElevationScaffold(
        scaffoldState = scaffoldState,
        listState = listState,
        onBack = onBack,
        topBarTitle = stringResource(R.string.prescription_details),
        navigationMode = NavigationBarMode.Close,
        snackbarHost = { SnackbarHost(it, modifier = Modifier.navigationBarsPadding()) },
        actions = {
            val authenticator = LocalAuthenticator.current
            val deletePrescriptionsHandle = remember {
                DeletePrescriptions(
                    prescriptionDetailsController = prescriptionDetailController,
                    authenticator = authenticator
                )
            }
            prescription?.let {
                prescription.accessCode?.let { accessCode ->
                    IconButton(onClick = {
                        shareHandler.share(taskId = prescription.taskId, accessCode)
                    }) {
                        Icon(Icons.Rounded.Share, null, tint = AppTheme.colors.primary700)
                    }
                }

                DeleteAction(it) {
                    // TODO: This needs to be done in the PrescriptionDetailsScreen,
                    //  please do stateHoisting so that it is not hidden inside, becase this also has a onBack.
                    //  It should be something like maybeOnBack with the deleteState sent with it and then in the
                    //  screen we decide what to do

                    val deleteState = deletePrescriptionsHandle.deletePrescription(
                        profileId = prescription.profileId,
                        taskId = prescription.taskId
                    )
                    when (deleteState) {
                        is PrescriptionServiceErrorState -> {
                            deleteErrorMessage(context, deleteState)?.let {
                                scaffoldState.snackbarHostState.showSnackbar(it)
                            }
                        }
                        is DeletePrescriptions.State.Deleted -> onBack()
                    }
                }
            }
        }
    ) {
        when (prescription) {
            is PrescriptionData.Synced ->
                SyncedPrescriptionOverview(
                    navController = navController,
                    consentGranted = consentGranted,
                    ssoTokenValid = ssoTokenValid,
                    onGrantConsent = { showGrantConsentDialog = true },
                    activeProfile = activeProfile,
                    listState = listState,
                    prescription = prescription,
                    onClickMedication = onClickMedication,
                    onShowInfo = {
                        onChangeSheetContent(it)
                    }
                )
            is PrescriptionData.Scanned ->
                ScannedPrescriptionOverview(
                    navController = navController,
                    listState = listState,
                    prescription = prescription,
                    onSwitchRedeemed = {
                        prescriptionDetailController.redeemScannedTask(
                            taskId = prescription.taskId,
                            redeem = it
                        )
                    },
                    onShowInfo = {
                        onChangeSheetContent(it)
                    },
                    onChangePrescriptionName = { newName ->
                        prescriptionDetailController.updateScannedTaskName(prescription.taskId, newName)
                    }
                )
            else -> {
                // do nothing
            }
        }
    }
}

@Composable
private fun DeleteAction(
    prescription: PrescriptionData.Prescription,
    onClickDelete: suspend () -> Unit
) {
    var showDeletePrescriptionDialog by remember { mutableStateOf(false) }
    var deletionInProgress by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val mutex = MutatorMutex()

    var dropdownExpanded by remember { mutableStateOf(false) }

    val isDeletable by remember {
        derivedStateOf {
            (prescription as? PrescriptionData.Synced)?.isDeletable ?: true
        }
    }

    IconButton(
        onClick = { dropdownExpanded = true },
        modifier = Modifier.testTag(TestTag.Prescriptions.Details.MoreButton)
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false },
        offset = DpOffset(24.dp, 0.dp)
    ) {
        DropdownMenuItem(
            modifier = Modifier.testTag(TestTag.Prescriptions.Details.DeleteButton),
            enabled = isDeletable,
            onClick = {
                dropdownExpanded = false
                showDeletePrescriptionDialog = true
            }
        ) {
            Text(
                text = stringResource(R.string.pres_detail_dropdown_delete),
                color = if (isDeletable) {
                    AppTheme.colors.red600
                } else {
                    AppTheme.colors.neutral400
                }
            )
        }
    }

    if (showDeletePrescriptionDialog) {
        val info = stringResource(R.string.pres_detail_delete_msg)
        val cancelText = stringResource(R.string.pres_detail_delete_no)
        val actionText = stringResource(R.string.pres_detail_delete_yes)

        CommonAlertDialog(
            header = null,
            info = info,
            cancelText = cancelText,
            actionText = actionText,
            enabled = !deletionInProgress,
            onCancel = {
                showDeletePrescriptionDialog = false
            },
            onClickAction = {
                coroutineScope.launch {
                    mutex.mutate {
                        try {
                            deletionInProgress = true
                            onClickDelete()
                        } finally {
                            showDeletePrescriptionDialog = false
                            deletionInProgress = false
                        }
                    }
                }
            }
        )
    }
}
