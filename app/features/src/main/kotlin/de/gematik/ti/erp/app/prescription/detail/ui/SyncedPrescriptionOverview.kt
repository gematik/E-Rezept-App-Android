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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionDetailsNavigationScreens
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.HealthPortalLink
import de.gematik.ti.erp.app.utils.compose.Label
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonTiny
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent

@Composable
fun SyncedPrescriptionOverview(
    navController: NavController,
    listState: LazyListState,
    prescription: PrescriptionData.Synced,
    onSelectMedication: (PrescriptionData.Medication) -> Unit,
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
                    onClick = onClickMedication(prescription, onSelectMedication, navController)
                )
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.PatientButton),
                    text = prescription.patient.name ?: noValueText,
                    label = stringResource(R.string.pres_detail_patient_header),
                    onClick = {
                        navController.navigate(PrescriptionDetailsNavigationScreens.Patient.path())
                    }
                )
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.PrescriberButton),
                    text = prescription.practitioner.name ?: noValueText,
                    label = stringResource(R.string.pres_detail_practitioner_header),
                    onClick = {
                        navController.navigate(PrescriptionDetailsNavigationScreens.Prescriber.path())
                    }
                )
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.OrganizationButton),
                    text = prescription.organization.name ?: noValueText,
                    label = stringResource(R.string.pres_detail_organization_header),
                    onClick = {
                        navController.navigate(PrescriptionDetailsNavigationScreens.Organization.path())
                    }
                )
            }

            item {
                Label(
                    text = stringResource(R.string.pres_detail_accident_header),
                    onClick = {
                        navController.navigate(PrescriptionDetailsNavigationScreens.Accident.path())
                    }
                )
            }

            item {
                Label(
                    modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformationButton),
                    text = stringResource(R.string.pres_detail_technical_information),
                    onClick = {
                        navController.navigate(PrescriptionDetailsNavigationScreens.TechnicalInformation.path())
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

@Composable
private fun onClickMedication(
    prescription: PrescriptionData.Synced,
    onSelectMedication: (PrescriptionData.Medication) -> Unit,
    navController: NavController
): () -> Unit = {
    if (!prescription.isDispensed) {
        onSelectMedication(PrescriptionData.Medication.Request(prescription.medicationRequest))
    } else {
        navController.navigate(PrescriptionDetailsNavigationScreens.MedicationOverview.path())
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
