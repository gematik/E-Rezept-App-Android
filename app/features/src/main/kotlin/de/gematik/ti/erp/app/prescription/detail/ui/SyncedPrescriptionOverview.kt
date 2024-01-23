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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.pkv.navigation.PkvRoutes
import de.gematik.ti.erp.app.pkv.presentation.rememberInvoiceController
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.ui.DirectAssignmentChip
import de.gematik.ti.erp.app.prescription.ui.FailureDetailsStatusChip
import de.gematik.ti.erp.app.prescription.ui.PrescriptionStateInfo
import de.gematik.ti.erp.app.prescription.ui.SubstitutionAllowedChip
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.HealthPortalLink
import de.gematik.ti.erp.app.utils.compose.Label
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonTiny
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerShortMedium
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import io.github.aakira.napier.Napier

@Suppress("LongMethod")
@Composable
fun SyncedPrescriptionOverview(
    navController: NavController,
    listState: LazyListState,
    consentGranted: Boolean?,
    ssoTokenValid: Boolean,
    onGrantConsent: () -> Unit,
    activeProfile: ProfilesUseCaseData.Profile,
    prescription: PrescriptionData.Synced,
    onClickMedication: (PrescriptionData.Medication) -> Unit,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
) {
    val noValueText = stringResource(R.string.pres_details_no_value)

    Column {
        val colPadding = if (prescription.isIncomplete) {
            PaddingValues()
        } else {
            WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag(TestTag.Prescriptions.Details.Content),
            contentPadding = colPadding
        ) {
            item {
                SyncedHeader(
                    prescription = prescription,
                    onShowInfo = onShowInfo
                )
            }

            if (activeProfile.insurance.insuranceType == ProfilesUseCaseData.InsuranceType.PKV) {
                item {
                    InvoiceCardSection(
                        consentGranted = consentGranted,
                        onGrantConsent = onGrantConsent,
                        ssoTokenValid = ssoTokenValid,
                        navController = navController,
                        profileId = activeProfile.id,
                        prescription = prescription
                    )
                }
            }

            item {
                val text = additionalFeeText(prescription.medicationRequest.additionalFee) ?: noValueText

                Label(
                    text = text,
                    label = stringResource(R.string.pres_details_additional_fee),
                    onClick = onClickAdditionalFee(
                        prescription.medicationRequest.additionalFee,
                        onShowInfo
                    )
                )
            }

            prescription.medicationRequest.emergencyFee?.let { emergencyFee ->
                item {
                    Label(
                        text = stringResource(
                            if (emergencyFee) R.string.pres_detail_noctu_no else R.string.pres_detail_noctu_yes
                        ),
                        label = stringResource(R.string.pres_details_emergency_fee),
                        onClick = onClickEmergencyFee(emergencyFee, onShowInfo)
                    )
                }
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.MedicationButton),
                    text = prescription.name ?: noValueText,
                    label = stringResource(R.string.pres_details_medication),
                    onClick = onClickMedication(prescription, onClickMedication, navController)
                )
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.PatientButton),
                    text = prescription.patient.name ?: noValueText,
                    label = stringResource(R.string.pres_detail_patient_header),
                    onClick = {
                        // TODO: Hoist it out
                        navController.navigate(
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
                        // TODO: Hoist it out
                        navController.navigate(
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
                        // TODO: Hoist it out
                        navController.navigate(
                            PrescriptionDetailRoutes.PrescriptionDetailOrganizationScreen.path(prescription.taskId)
                        )
                    }
                )
            }

            item {
                Label(
                    text = stringResource(R.string.pres_detail_accident_header),
                    onClick = {
                        // TODO: Hoist it out
                        navController.navigate(
                            PrescriptionDetailRoutes.PrescriptionDetailAccidentInfoScreen.path(prescription.taskId)
                        )
                    }
                )
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformationButton),
                    text = stringResource(R.string.pres_detail_technical_information),
                    onClick = {
                        // TODO: Hoist it out
                        navController.navigate(
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
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                prescription
            )
        }
    }
}

@Composable
private fun SyncedHeader(
    prescription: PrescriptionData.Synced,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            prescription.name ?: stringResource(R.string.prescription_medication_default_name),
            style = AppTheme.typography.h5,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        when {
            prescription.isIncomplete -> {
                SpacerShortMedium()
                FailureDetailsStatusChip(
                    onClick = {
                        onShowInfo(PrescriptionDetailBottomSheetContent.Failure())
                    }
                )
            }

            prescription.isDirectAssignment -> {
                SpacerShortMedium()
                DirectAssignmentChip(
                    onClick = {
                        onShowInfo(
                            PrescriptionDetailBottomSheetContent.DirectAssignment()
                        )
                    }
                )
            }

            prescription.isSubstitutionAllowed -> {
                SpacerShortMedium()
                SubstitutionAllowedChip(
                    onClick = {
                        onShowInfo(
                            PrescriptionDetailBottomSheetContent.SubstitutionAllowed()
                        )
                    }
                )
            }
        }

        SpacerShortMedium()

        val onClick = when {
            !prescription.isDirectAssignment &&
                (
                    prescription.state is SyncedTaskData.SyncedTask.Ready ||
                        prescription.state is SyncedTaskData.SyncedTask.LaterRedeemable
                    ) -> {
                {
                    onShowInfo(
                        PrescriptionDetailBottomSheetContent.HowLongValid(
                            prescription
                        )
                    )
                }
            }

            else -> null
        }
        SyncedStatus(
            prescription = prescription,
            onClick = onClick
        )
        SpacerLarge()
    }
}

@Composable
private fun SyncedStatus(
    modifier: Modifier = Modifier,
    prescription: PrescriptionData.Synced,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = PaddingDefaults.Tiny)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .then(clickableModifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (prescription.isDirectAssignment) {
            val text = if (prescription.isDispensed) {
                stringResource(R.string.pres_details_direct_assignment_received_state)
            } else {
                stringResource(R.string.pres_details_direct_assignment_state)
            }
            Text(
                text,
                style = AppTheme.typography.body2l,
                textAlign = TextAlign.Center
            )
        } else {
            PrescriptionStateInfo(prescription.state, textAlign = TextAlign.Center)
        }
        if (onClick != null) {
            Spacer(Modifier.padding(2.dp))
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colors.primary600
            )
        }
    }
}

@Composable
private fun additionalFeeText(additionalFee: SyncedTaskData.AdditionalFee): String? = when (additionalFee) {
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
                val body = """
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
            colors = ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.red600,
                contentColor = AppTheme.colors.neutral000
            )
        ) {
            Text(stringResource(R.string.report_prescription_failure))
        }
    }
}

@Composable
private fun onClickAdditionalFee(
    additionalFee: SyncedTaskData.AdditionalFee,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
): () -> Unit = {
    when (additionalFee) {
        SyncedTaskData.AdditionalFee.NotExempt -> {
            onShowInfo(
                PrescriptionDetailBottomSheetContent.AdditionalFeeNotExempt()
            )
        }

        SyncedTaskData.AdditionalFee.Exempt -> {
            onShowInfo(
                PrescriptionDetailBottomSheetContent.AdditionalFeeExempt()
            )
        }

        else -> {}
    }
}

private fun onClickMedication(
    prescription: PrescriptionData.Synced,
    onClickMedication: (PrescriptionData.Medication) -> Unit,
    navController: NavController
): () -> Unit = {
    if (!prescription.isDispensed) {
        onClickMedication(PrescriptionData.Medication.Request(prescription.medicationRequest))
    } else {
        navController.navigate(
            PrescriptionDetailRoutes.PrescriptionDetailMedicationOverviewScreen.path(prescription.taskId)
        )
    }
}

@Composable
private fun onClickEmergencyFee(
    emergencyFee: Boolean,
    onShowInfo: (PrescriptionDetailBottomSheetContent) -> Unit
): () -> Unit = {
    if (emergencyFee) {
        onShowInfo(
            PrescriptionDetailBottomSheetContent.EmergencyFeeNotExempt()
        )
    } else {
        onShowInfo(
            PrescriptionDetailBottomSheetContent.EmergencyFee()
        )
    }
}

@Composable
private fun InvoiceCardSection(
    consentGranted: Boolean?,
    onGrantConsent: () -> Unit,
    ssoTokenValid: Boolean,
    navController: NavController,
    profileId: ProfileIdentifier,
    prescription: PrescriptionData.Synced
) {
    val invoicesController = rememberInvoiceController(profileId = profileId)
    val invoice by produceState<InvoiceData.PKVInvoice?>(null) {
        Napier.d { "invoice prescription.taskId ${prescription.taskId}" }
        invoicesController.detailState(prescription.taskId).collect {
            value = it
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                PaddingDefaults.Medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = consentGranted == null && ssoTokenValid) {
            InvoiceLoadingCard()
        }
        AnimatedVisibility(visible = consentGranted != null) {
            when {
                ssoTokenValid && consentGranted == false -> NoConsentGrantedCard(onGrantConsent)
                invoice == null && consentGranted == true -> NoInvoiceConsentGrantedCard()
                invoice != null -> PrimaryButtonSmall(
                    onClick = {
                        navController.navigate(
                            PkvRoutes.InvoiceDetailsScreen.path(
                                taskId = prescription.taskId,
                                profileId = profileId
                            )
                        )
                    }
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
