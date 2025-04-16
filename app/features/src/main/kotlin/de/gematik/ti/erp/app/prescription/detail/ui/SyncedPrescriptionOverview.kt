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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.medicationplan.components.MedicationPlanLineItem
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.pkv.presentation.model.InvoiceCardUiState
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.ui.components.SelfPayPrescriptionDetailsChip
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionDetailBottomSheetNavigationData
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.DirectAssignmentChip
import de.gematik.ti.erp.app.prescription.ui.FailureDetailsStatusChip
import de.gematik.ti.erp.app.prescription.ui.SubstitutionNotAllowedChip
import de.gematik.ti.erp.app.prescription.ui.components.PrescriptionStateInfo
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerShortMedium
import de.gematik.ti.erp.app.utils.compose.HealthPortalLink
import de.gematik.ti.erp.app.utils.compose.Label
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonTiny
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Suppress("LongMethod", "CyclomaticComplexMethod", "LongParameterList")
@Composable
fun SyncedPrescriptionOverview(
    listState: LazyListState,
    invoiceCardState: InvoiceCardUiState,
    medicationSchedule: MedicationSchedule?,
    activeProfile: ProfilesUseCaseData.Profile,
    prescription: PrescriptionData.Synced,
    now: Instant = Clock.System.now(),
    isMedicationPlanEnabled: Boolean,
    onClickMedication: (PrescriptionData.Medication) -> Unit,
    onGrantConsent: () -> Unit,
    onClickInvoice: () -> Unit,
    onNavigateToRoute: (String) -> Unit, // TODO: remove, hides the navigation
    onClickRedeemLocal: () -> Unit,
    onClickRedeemOnline: () -> Unit,
    onShowInfoBottomSheet: PrescriptionDetailBottomSheetNavigationData,
    onShowHowLongValidBottomSheet: () -> Unit,
    onClickMedicationPlan: () -> Unit

) {
    val noValueText = stringResource(R.string.pres_details_no_value)
    val isAccident =
        remember(prescription) {
            prescription.medicationRequest.accidentType != SyncedTaskData.AccidentType.None
        }

    LazyColumn(
        state = listState,
        modifier =
        Modifier
            .fillMaxWidth()
            .testTag(TestTag.Prescriptions.Details.Content),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { PrescriptionName(prescription.name) }

        if (prescription.isIncomplete) {
            item {
                SpacerShortMedium()
                FailureDetailsStatusChip {
                    onShowInfoBottomSheet.failureBottomSheet()
                }
            }
        }

        if (prescription.insurance.coverageType == SyncedTaskData.CoverageType.SEL) {
            item {
                SpacerShortMedium()
                SelfPayPrescriptionDetailsChip {
                    onShowInfoBottomSheet.selPayerPrescriptionBottomSheet()
                }
            }
        }

        if (prescription.isDirectAssignment) {
            item {
                SpacerShortMedium()
                DirectAssignmentChip {
                    onShowInfoBottomSheet.directAssignmentBottomSheet()
                }
                SpacerMedium()
                DirectAssignmentInfo(prescription.isDispensed)
            }
        }

        if (!prescription.isSubstitutionAllowed) {
            item {
                SpacerShortMedium()
                SubstitutionNotAllowedChip {
                    onShowInfoBottomSheet.substitutionNotAllowedBottomSheet()
                }
            }
        }

        item {
            SpacerShortMedium()
            SyncedPrescriptionStateInfo(
                prescriptionState = prescription.state,
                now = now,
                onClick = when {
                    prescription.state is SyncedTaskData.SyncedTask.Ready ||
                        prescription.state is SyncedTaskData.SyncedTask.LaterRedeemable
                    -> {
                        {
                            onShowHowLongValidBottomSheet()
                        }
                    }

                    else -> null
                }
            )
            SpacerShortMedium()
        }

        if (activeProfile.insurance.insuranceType == ProfilesUseCaseData.InsuranceType.PKV) {
            item {
                InvoiceCardSection(
                    ssoTokenValid = activeProfile.isSSOTokenValid(),
                    invoiceCardState = invoiceCardState,
                    onGrantConsent = onGrantConsent,
                    onClickInvoice = onClickInvoice
                )
            }
        }
        if (prescription.state is SyncedTaskData.SyncedTask.Ready &&
            !prescription.isDirectAssignment &&
            prescription.redeemState == SyncedTaskData.SyncedTask.RedeemState.RedeemableAndValid
        ) {
            item {
                RedeemFromDetailSection(
                    onClickRedeemLocal = onClickRedeemLocal,
                    onClickRedeemOnline = onClickRedeemOnline
                )
                SpacerLarge()
            }
        }

        if (isMedicationPlanEnabled) {
            item {
                MedicationPlanLineItem(medicationSchedule, onClickMedicationPlan)
            }
        }

        item {
            val text = additionalFeeText(prescription.medicationRequest.additionalFee) ?: noValueText
            Label(
                text = text,
                label = stringResource(R.string.pres_details_additional_fee),
                onClick = {
                    when (prescription.medicationRequest.additionalFee) {
                        SyncedTaskData.AdditionalFee.NotExempt -> {
                            onShowInfoBottomSheet.additionalFeeNotExemptBottomSheet()
                        }

                        SyncedTaskData.AdditionalFee.Exempt -> {
                            onShowInfoBottomSheet.additionalFeeExemptBottomSheet()
                        }

                        else -> {}
                    }
                }
            )
        }

        prescription.medicationRequest.emergencyFee?.let { emergencyFee ->
            item {
                Label(
                    text =
                    stringResource(
                        if (emergencyFee) R.string.pres_detail_noctu_no else R.string.pres_detail_noctu_yes
                    ),
                    label = stringResource(R.string.pres_details_emergency_fee),
                    onClick = {
                        if (emergencyFee) {
                            onShowInfoBottomSheet.emergencyFeeNotExemptBottomSheet()
                        } else {
                            onShowInfoBottomSheet.emergencyFeeExemptBottomSheet()
                        }
                    }
                )
            }
        }

        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.SubstitutionButton),
                text =
                stringResource(
                    if (prescription.isSubstitutionAllowed) {
                        R.string.prescription_details_substitution_allowed
                    } else {
                        R.string.prescription_details_substitution_not_allowed
                    }
                ),
                label = stringResource(R.string.prescription_details_aut_idem_label),
                onClick = {
                    if (prescription.isSubstitutionAllowed) {
                        onShowInfoBottomSheet.substitutionAllowedBottomSheet()
                    } else {
                        onShowInfoBottomSheet.substitutionNotAllowedBottomSheet()
                    }
                }
            )
        }

        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.MedicationButton),
                text = prescription.name ?: noValueText,
                label = stringResource(R.string.pres_details_medication),
                onClick = onClickMedication(prescription, onClickMedication, onNavigateToRoute)
            )
        }

        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.PatientButton),
                text = prescription.patient.name ?: noValueText,
                label = stringResource(R.string.pres_detail_patient_header),
                onClick = {
                    onNavigateToRoute(
                        PrescriptionDetailRoutes.PrescriptionDetailPatientScreen.path(prescription.taskId)
                    )
                }
            )
        }

        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.PrescriberButton),
                text = prescription.practitioner.name ?: noValueText,
                label = stringResource(R.string.pres_detail_practitioner_header),
                onClick = {
                    onNavigateToRoute(
                        PrescriptionDetailRoutes.PrescriptionDetailPrescriberScreen.path(prescription.taskId)
                    )
                }
            )
        }

        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.OrganizationButton),
                text = prescription.organization.name ?: noValueText,
                label = stringResource(R.string.pres_detail_organization_header),
                onClick = {
                    onNavigateToRoute(
                        PrescriptionDetailRoutes.PrescriptionDetailOrganizationScreen.path(prescription.taskId)
                    )
                }
            )
        }

        if (isAccident) {
            item {
                Label(
                    text = stringResource(R.string.pres_detail_accident_title),
                    onClick = {
                        onNavigateToRoute(
                            PrescriptionDetailRoutes.PrescriptionDetailAccidentInfoScreen.path(prescription.taskId)
                        )
                    }
                )
            }
        }

        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformationButton),
                text = stringResource(R.string.pres_detail_technical_information),
                onClick = {
                    onNavigateToRoute(
                        PrescriptionDetailRoutes.PrescriptionDetailTechnicalInformationScreen.path(
                            prescription.taskId
                        )
                    )
                }
            )
        }

        item {
            HealthPortalLink(
                Modifier.padding(
                    horizontal = PaddingDefaults.Medium,
                    vertical = PaddingDefaults.XXLarge
                )
            )
        }
    }

    if (prescription.isIncomplete) {
        FailureBanner(
            Modifier
                .fillMaxWidth(),
            prescription
        )
    }
}

@Composable
fun DirectAssignmentInfo(isDispensed: Boolean) {
    val text =
        if (isDispensed) {
            stringResource(R.string.pres_details_direct_assignment_received_state)
        } else {
            stringResource(R.string.pres_details_direct_assignment_state)
        }
    Text(
        text,
        style = AppTheme.typography.body2l,
        textAlign = TextAlign.Center
    )
}

@Composable
fun PrescriptionName(name: String?) {
    Text(
        name ?: stringResource(R.string.prescription_medication_default_name),
        style = AppTheme.typography.h5,
        textAlign = TextAlign.Center,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun SyncedPrescriptionStateInfo(
    modifier: Modifier = Modifier,
    prescriptionState: SyncedTaskData.SyncedTask.TaskState,
    now: Instant,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier =
        if (onClick != null) {
            Modifier
                .clickable(role = Role.Button, onClick = onClick)
                .padding(start = PaddingDefaults.Tiny)
        } else {
            Modifier
        }

    Column(
        modifier = modifier
            .then(clickableModifier)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PrescriptionStateInfo(
                state = prescriptionState,
                now = now,
                textAlign = TextAlign.Center
            )
            if (onClick != null) {
                Spacer(Modifier.padding(SizeDefaults.quarter))
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    null,
                    modifier = Modifier.size(SizeDefaults.double),
                    tint = AppTheme.colors.primary700
                )
            }
        }
    }
}

@Composable
private fun additionalFeeText(additionalFee: SyncedTaskData.AdditionalFee): String? =
    when (additionalFee) {
        SyncedTaskData.AdditionalFee.Exempt ->
            stringResource(R.string.pres_detail_no)

        SyncedTaskData.AdditionalFee.NotExempt ->
            stringResource(R.string.pres_detail_yes)

        else -> null
    }

@Composable
private fun FailureBanner(
    modifier: Modifier,
    prescription: PrescriptionData.Synced
) {
    val mailAddress = stringResource(R.string.settings_contact_mail_address)
    val subject = stringResource(R.string.settings_feedback_mail_subject)

    val context = LocalContext.current
    Row(
        modifier
            .fillMaxWidth()
            .background(AppTheme.colors.neutral050)
            .padding(PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.prescription_failure_info),
            style = AppTheme.typography.body2,
            modifier = Modifier.weight(1f)
        )
        SpacerMedium()
        PrimaryButtonTiny(
            onClick = {
                val body =
                    """
                    PVS ID: ${prescription.task.pvsIdentifier}
                    
                    ${prescription.failureToReport}
                    """.trimIndent()

                context.handleIntent(
                    provideEmailIntent(
                        address = mailAddress,
                        body = body,
                        subject = subject
                    )
                )
            },
            colors =
            ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.red600,
                contentColor = AppTheme.colors.neutral000
            )
        ) {
            Text(stringResource(R.string.report_prescription_failure))
        }
    }
}

private fun onClickMedication(
    prescription: PrescriptionData.Synced,
    onClickMedication: (PrescriptionData.Medication) -> Unit,
    onNavigateToRoute: (String) -> Unit
): () -> Unit =
    {
        if (!prescription.isDispensed) {
            onClickMedication(PrescriptionData.Medication.Request(prescription.medicationRequest))
        } else {
            onNavigateToRoute(
                PrescriptionDetailRoutes.PrescriptionDetailMedicationOverviewScreen.path(prescription.taskId)
            )
        }
    }

@Composable
private fun InvoiceCardSection(
    ssoTokenValid: Boolean,
    invoiceCardState: InvoiceCardUiState,
    onGrantConsent: () -> Unit,
    onClickInvoice: () -> Unit
) {
    AnimatedVisibility(ssoTokenValid) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Crossfade(
                modifier = Modifier.animateContentSize(),
                targetState = invoiceCardState,
                label = "invoice-states"
            ) { state ->
                when (state) {
                    InvoiceCardUiState.Loading -> InvoiceLoadingCard()
                    InvoiceCardUiState.NoConsent -> NoConsentGrantedCard(onGrantConsent)
                    InvoiceCardUiState.NoInvoice -> NoInvoiceConsentGrantedCard()
                    InvoiceCardUiState.ShowInvoice ->
                        PrimaryButtonSmall(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PaddingDefaults.XLarge)
                                .wrapContentHeight(),
                            onClick = onClickInvoice
                        ) {
                            Text(
                                stringResource(R.string.invoice_card_consent_invoice_button_text)
                            )
                        }
                }
            }
        }
        SpacerLarge()
    }
}
