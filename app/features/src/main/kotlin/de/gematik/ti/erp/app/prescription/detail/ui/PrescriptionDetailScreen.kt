/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.SnackbarResult
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.consent.model.ConsentContext
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.consent.model.ConsentState.Companion.isConsentGranted
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.toNavigationString
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.presentation.model.InvoiceCardUiState
import de.gematik.ti.erp.app.pkv.presentation.rememberConsentController
import de.gematik.ti.erp.app.pkv.presentation.rememberInvoiceController
import de.gematik.ti.erp.app.pkv.ui.screens.HandleConsentState
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberPrescriptionDetailController
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberSharePrescriptionController
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionDetailBottomSheetNavigationData
import de.gematik.ti.erp.app.prescription.detail.ui.preview.PrescriptionDetailPreview
import de.gematik.ti.erp.app.prescription.detail.ui.preview.PrescriptionDetailPreviewParameter
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.redeem.navigation.RedeemRoutes
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbarScaffold
import kotlinx.coroutines.launch

const val MISSING_VALUE = "---"

class PrescriptionDetailScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Suppress("CyclomaticComplexMethod")
    @Composable
    override fun Content() {
        val taskId =
            navBackStackEntry.arguments?.getString(
                PrescriptionDetailRoutes.PRESCRIPTION_DETAIL_NAV_TASK_ID
            ) ?: return

        val snackbar = LocalSnackbarScaffold.current
        val dialog = LocalDialog.current
        val context = LocalContext.current

        val prescriptionDetailsController = rememberPrescriptionDetailController(taskId)
        val profilePrescriptionData by prescriptionDetailsController.profilePrescription.collectAsStateWithLifecycle()
        val isMedicationPlanEnabled by prescriptionDetailsController.isMedicationPlanEnabled.collectAsStateWithLifecycle()

        UiStateMachine(
            state = profilePrescriptionData,
            onLoading = {
                Center {
                    CircularProgressIndicator()
                }
            },
            onEmpty = {
                ErrorScreenComponent()
            },
            onError = {
                ErrorScreenComponent()
            },
            onContent = { (profile, prescription) ->
                // TODO: Too many controllers in one screen, consider refactoring
                val consentController = rememberConsentController()
                val invoicesController = rememberInvoiceController(profileId = profile.id)

                val activeProfileIsPKVProfile =
                    profile.insurance.insuranceType == ProfilesUseCaseData.InsuranceType.PKV

                val invoice by produceState<InvoiceData.PKVInvoiceRecord?>(null) {
                    invoicesController.getInvoiceForTaskId(prescription.taskId).collect {
                        value = it
                    }
                }

                val consentState by consentController.consentState.collectAsStateWithLifecycle()
                val consentGranted = remember(consentState) { consentState.isConsentGranted() }
                val deletePrescriptionState by
                prescriptionDetailsController.prescriptionDeleted.collectAsStateWithLifecycle()
                val onClickDeletePrescriptionEvent = ComposableEvent<Unit>()

                val shareHandler = rememberSharePrescriptionController(profile.id)

                val mailAddress = stringResource(R.string.settings_contact_mail_address)
                val subject = stringResource(R.string.settings_feedback_mail_subject)

                val scope = rememberCoroutineScope()

                val ssoTokenValid =
                    remember(profile.ssoTokenScope) {
                        profile.isSSOTokenValid()
                    }

                val invoiceState = invoicesController.uiState(consentState, ssoTokenValid, invoice)

                LaunchedEffect(Unit) {
                    if (activeProfileIsPKVProfile &&
                        ssoTokenValid &&
                        consentState == ConsentState.ValidState.UnknownConsent &&
                        !consentGranted
                    ) {
                        consentController.getChargeConsent(profile.id)
                    }
                }

                HandleDeletePrescriptionState(
                    state = deletePrescriptionState,
                    dialog = dialog,
                    onShowCardWall = {
                        navController.navigate(CardWallRoutes.CardWallIntroScreen.path(profile.id))
                    },
                    onConfirmDialogRequest = {
                            (sendFeedBackMessage, errorMessage): Pair<Boolean, String>,
                            deletePrescriptionLocally: Boolean
                        ->
                        if (deletePrescriptionLocally) {
                            prescriptionDetailsController.deletePrescriptionFromLocal(profile.id, taskId)
                            navController.popBackStack(
                                PrescriptionDetailRoutes.PrescriptionDetailScreen.route,
                                true
                            )
                        }
                        if (sendFeedBackMessage) {
                            context.handleIntent(
                                provideEmailIntent(
                                    address = mailAddress,
                                    body = errorMessage,
                                    subject = subject
                                )
                            )
                        }
                    },
                    onRetry = {
                        prescriptionDetailsController.deletePrescription(profile.id, taskId)
                    },
                    onDismiss = {
                        prescriptionDetailsController.resetDeletePrescriptionState()
                    },
                    onBack = {
                        navController.popBackStack(
                            PrescriptionDetailRoutes.PrescriptionDetailScreen.route,
                            true
                        )
                    }
                )

                if (activeProfileIsPKVProfile) {
                    val actionString = stringResource(R.string.consent_action_to_invoices)
                    val consentRevokedInfo = stringResource(R.string.consent_revoked_info)
                    val consentGrantedInfo = stringResource(R.string.consent_granted_info)

                    HandleConsentState(
                        consentState = consentState,
                        dialog = dialog,
                        onShowCardWall = {
                            navController.navigate(CardWallRoutes.CardWallIntroScreen.path(profile.id))
                        },
                        onRetry = { consentContext ->
                            when (consentContext) {
                                ConsentContext.GetConsent -> consentController.getChargeConsent(profile.id)
                                ConsentContext.GrantConsent -> consentController.grantChargeConsent(profile.id)
                                ConsentContext.RevokeConsent -> {} // revoke is not available on prescription details
                            }
                        },
                        onConsentGranted = {
                            scope.launch {
                                val result =
                                    snackbar.showSnackbar(
                                        message = consentGrantedInfo,
                                        actionLabel = actionString
                                    )
                                when (result) {
                                    SnackbarResult.Dismissed -> {}
                                    SnackbarResult.ActionPerformed ->
                                        navController.navigate(PkvRoutes.InvoiceListScreen.path(profile.id))
                                }
                            }
                        },
                        onConsentRevoked = {
                            scope.launch {
                                val result =
                                    snackbar.showSnackbar(
                                        message = consentRevokedInfo,
                                        actionLabel = actionString
                                    )
                                when (result) {
                                    SnackbarResult.Dismissed -> {}
                                    SnackbarResult.ActionPerformed ->
                                        navController.navigate(PkvRoutes.InvoiceListScreen.path(profile.id))
                                }
                            }
                        }
                    )
                }

                val scaffoldState = rememberScaffoldState()
                val listState = rememberLazyListState()
                val medicationSchedule by prescriptionDetailsController.medicationSchedule.collectAsStateWithLifecycle()

                DeletePrescriptionDialog(
                    dialog = dialog,
                    event = onClickDeletePrescriptionEvent,
                    isPKVProfile = activeProfileIsPKVProfile,
                    isPrescriptionRedeemed = prescription.redeemedOn != null
                ) {
                    prescriptionDetailsController.deletePrescription(
                        profile.id,
                        prescription.taskId
                    )
                }
                PrescriptionDetailScreenScaffold(
                    activeProfile = profile,
                    prescription = prescription,
                    medicationSchedule = medicationSchedule,
                    invoiceCardState = invoiceState,
                    scaffoldState = scaffoldState,
                    listState = listState,
                    isMedicationPlanEnabled = isMedicationPlanEnabled,
                    onClickMedication = { medication ->
                        navController.navigate(
                            PrescriptionDetailRoutes.PrescriptionDetailMedicationScreen.path(
                                taskId = taskId,
                                selectedMedication = medication.toNavigationString()
                            )
                        )
                    },
                    onGrantConsent = {
                        consentController.grantChargeConsent(profile.id)
                    },
                    onClickTechnicalInformation = {
                        navController.navigate(
                            PrescriptionDetailRoutes.PrescriptionDetailTechnicalInformationScreen.path(
                                taskId = prescription.taskId
                            )
                        )
                    },
                    onChangePrescriptionName = { newName ->
                        prescriptionDetailsController.updateScannedTaskName(prescription.taskId, newName)
                    },
                    onSwitchRedeemed = { redeem ->
                        prescriptionDetailsController.redeemScannedTask(prescription.taskId, redeem)
                    },
                    onClickDeletePrescription = {
                        onClickDeletePrescriptionEvent.trigger(Unit)
                    },
                    onSharePrescription = {
                        shareHandler.share(taskId = prescription.taskId, prescription.accessCode)
                    },
                    onClickRedeemLocal = {
                        navController.navigate(
                            RedeemRoutes.RedeemLocal.path(
                                taskId = prescription.taskId
                            )
                        )
                    },
                    onClickRedeemOnline = {
                        navController.navigate(
                            PharmacyRoutes.PharmacyStartScreenModal.path(taskId = prescription.taskId)
                        )
                    },
                    onClickMedicationPlan = {
                        navController.navigate(
                            MedicationPlanRoutes.MedicationPlanPerPrescription.path(
                                taskId = taskId
                            )
                        )
                    },
                    onNavigateToRoute = { route ->
                        navController.navigate(route)
                    },
                    onShowHowLongValidBottomSheet = {
                        navController.navigate(
                            PrescriptionDetailRoutes.HowLongValidBottomSheetScreen.path(
                                taskId = prescription.taskId
                            )
                        )
                    },
                    onShowInfoBottomSheet =
                    PrescriptionDetailBottomSheetNavigationData(
                        selPayerPrescriptionBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.SelPayerPrescriptionBottomSheetScreen.path(
                                    titleId = R.string.pres_details_exp_sel_payer_prescription,
                                    infoId = R.string.pres_details_exp_sel_payer_info
                                )
                            )
                        },
                        additionalFeeNotExemptBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.AdditionalFeeNotExemptBottomSheetScreen.path(
                                    titleId = R.string.pres_details_exp_add_fee_title,
                                    infoId = R.string.pres_details_exp_add_fee_info
                                )
                            )
                        },
                        additionalFeeExemptBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.AdditionalFeeExemptBottomSheetScreen.path(
                                    titleId = R.string.pres_details_exp_no_add_fee_title,
                                    infoId = R.string.pres_details_exp_no_add_fee_info
                                )
                            )
                        },
                        failureBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.FailureBottomSheetScreen.path(
                                    titleId = R.string.pres_details_exp_failure_title,
                                    infoId = R.string.pres_details_exp_failure_info
                                )
                            )
                        },
                        scannedPrescriptionBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.ScannedBottomSheetScreen.path(
                                    titleId = R.string.pres_details_exp_scanned_title,
                                    infoId = R.string.pres_details_exp_scanned_info
                                )
                            )
                        },
                        directAssignmentBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.DirectAssignmentBottomSheetScreen.path(
                                    titleId = R.string.pres_details_exp_da_title,
                                    infoId = R.string.pres_details_exp_da_info
                                )
                            )
                        },
                        substitutionAllowedBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.SubstitutionAllowedBottomSheetScreen.path(
                                    titleId = R.string.prescription_details_substitution_allowed,
                                    infoId = R.string.prescription_details_substitution_allowed_info
                                )
                            )
                        },
                        substitutionNotAllowedBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.SubstitutionNotAllowedBottomSheetScreen.path(
                                    titleId = R.string.prescription_details_substitution_not_allowed,
                                    infoId = R.string.prescription_details_substitution_not_allowed_info
                                )
                            )
                        },
                        emergencyFeeExemptBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.EmergencyFeeExemptBottomSheetScreen.path(
                                    titleId = R.string.pres_details_exp_em_fee_title,
                                    infoId = R.string.pres_details_exp_em_fee_info
                                )
                            )
                        },
                        emergencyFeeNotExemptBottomSheet = {
                            navController.navigate(
                                PrescriptionDetailRoutes.EmergencyFeeNotExemptBottomSheetScreen.path(
                                    titleId = R.string.pres_details_exp_no_em_fee_title,
                                    infoId = R.string.pres_details_exp_no_em_fee_info
                                )
                            )
                        }
                    ),
                    onClickInvoice = {
                        navController.navigate(
                            PkvRoutes.InvoiceDetailsScreen.path(
                                profileId = profile.id,
                                taskId = prescription.taskId
                            )
                        )
                    },
                    onBack = navController::popBackStack
                )
            }
        )
    }
}

@Composable
private fun DeletePrescriptionDialog(
    dialog: DialogScaffold,
    event: ComposableEvent<Unit>,
    isPrescriptionRedeemed: Boolean = false,
    isPKVProfile: Boolean = false,
    onConfirmRequest: () -> Unit
) {
    val title = if (isPKVProfile && isPrescriptionRedeemed) {
        stringResource(R.string.pres_detail_delete_prescription_and_recipe)
    } else {
        stringResource(R.string.pres_detail_delete_prescription)
    }
    val info = if (isPKVProfile) {
        stringResource(R.string.pres_detail_delete_and_recipe_msg)
    } else {
        stringResource(R.string.pres_detail_delete_msg)
    }
    event.listen {
        dialog.show { dialog ->
            ErezeptAlertDialog(
                title = title,
                bodyText = info,
                confirmText = stringResource(R.string.pres_detail_delete_yes),
                dismissText = stringResource(R.string.pres_detail_delete_no),
                onDismissRequest = {
                    dialog.dismiss()
                },
                onConfirmRequest = {
                    onConfirmRequest()
                    dialog.dismiss()
                }
            )
        }
    }
}

@LightDarkPreview
@Composable
fun PrescriptionDetailScreenPreview(
    @PreviewParameter(PrescriptionDetailPreviewParameter::class) previewData: PrescriptionDetailPreview
) {
    val listState = rememberLazyListState()
    val scaffoldState = rememberScaffoldState()
    PreviewAppTheme {
        PrescriptionDetailScreenScaffold(
            activeProfile = ProfilesUseCaseData.Profile(
                id = "1",
                name = "Max Mustermann",
                insurance = ProfileInsuranceInformation(
                    insurantName = "Max Mustermann",
                    insuranceIdentifier = "1234567890",
                    insuranceName = "Muster AG",
                    insuranceType = ProfilesUseCaseData.InsuranceType.GKV
                ),
                isActive = true,
                color = ProfilesData.ProfileColorNames.SUN_DEW,
                avatar = ProfilesData.Avatar.Baby,
                image = null,
                lastAuthenticated = null,
                ssoTokenScope = null
            ),
            scaffoldState = scaffoldState,
            listState = listState,
            prescription = previewData.prescription,
            medicationSchedule = null,
            invoiceCardState = InvoiceCardUiState.NoInvoice,
            onShowInfoBottomSheet = PrescriptionDetailBottomSheetNavigationData(),
            now = previewData.now,
            onSwitchRedeemed = {},
            onNavigateToRoute = {},
            onClickMedication = {},
            onChangePrescriptionName = {},
            onGrantConsent = {},
            onClickRedeemLocal = {},
            onClickRedeemOnline = {},
            onClickTechnicalInformation = {},
            onClickDeletePrescription = {},
            onClickMedicationPlan = {},
            onSharePrescription = {},
            onShowHowLongValidBottomSheet = {},
            onClickInvoice = {},
            onBack = {},
            isMedicationPlanEnabled = false
        )
    }
}
