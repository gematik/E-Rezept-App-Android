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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.core.LocalNow
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.prescription.ui.CompletedStatusChip
import de.gematik.ti.erp.app.prescription.ui.DeletedStatusChip
import de.gematik.ti.erp.app.prescription.ui.DirectAssignmentStatusChip
import de.gematik.ti.erp.app.prescription.ui.ExpiredStatusChip
import de.gematik.ti.erp.app.prescription.ui.FailureStatusChip
import de.gematik.ti.erp.app.prescription.ui.InProgressStatusChip
import de.gematik.ti.erp.app.prescription.ui.LaterRedeemableStatusChip
import de.gematik.ti.erp.app.prescription.ui.NumeratorChip
import de.gematik.ti.erp.app.prescription.ui.PendingStatusChip
import de.gematik.ti.erp.app.prescription.ui.ProvidedStatusChip
import de.gematik.ti.erp.app.prescription.ui.ReadyStatusChip
import de.gematik.ti.erp.app.prescription.ui.SelfPayerPrescriptionChip
import de.gematik.ti.erp.app.prescription.ui.UnknownStatusChip
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.semantics.semanticsMergedButton
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStateErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.letNotNull
import kotlinx.datetime.Instant

// TODO: (dinesh) only prescription
@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun FullDetailMedication(
    prescription: TaskErpModel.Synced.Prescription,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val now = LocalNow.current
    val showDirectAssignmentLabel by remember(prescription) {
        derivedStateOf {
            val isCompleted =
                (prescription.state() as? TaskStateErpModel.Other)?.state == FhirTaskStatusErpModel.Completed

            prescription.isDirectAssignment() && !isCompleted
        }
    }

    val chipInfo = remember(prescription) {
        val multiplePrescriptionInfo = prescription.medicationRequest?.multiplePrescriptionInfo
        Triple(
            multiplePrescriptionInfo?.indicator == true,
            multiplePrescriptionInfo?.numbering?.numerator?.value,
            multiplePrescriptionInfo?.numbering?.denominator?.value
        )
    }
    val (isPartOfMultiplePrescription, numerator, denominator) = chipInfo
    val isSelfPayPrescription = remember(prescription) {
        prescription.insuranceInformation?.coverageType == InsuranceErpModelCoverageType.SEL
    }

    Box {
        Card(
            modifier =
            modifier
                .semantics {
                    prescriptionId = prescription.taskId
                }
                .semanticsMergedButton()
                .testTag(TestTag.Prescriptions.FullDetailPrescription),
            shape = RoundedCornerShape(SizeDefaults.double),
            border = BorderStroke(SizeDefaults.eighth, color = AppTheme.colors.neutral300),
            backgroundColor = AppTheme.colors.neutral050,
            elevation = SizeDefaults.zero,
            onClick = onClick
        ) {
            val textColor = AppTheme.colors.neutral900
            Row(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                Column(modifier = Modifier.weight(1f)) {
                    val medicationName =
                        prescription.name
                            ?: stringResource(R.string.prescription_medication_default_name)

                    Text(
                        modifier = Modifier.testTag(TestTag.Prescriptions.FullDetailPrescriptionName),
                        text = medicationName,
                        color = textColor,
                        style = AppTheme.typography.subtitle1,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    SpacerTiny()

                    if (!prescription.isDirectAssignment()) {
                        PrescriptionStateInfo(
                            state = prescription.state(),
                            now = now
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
                        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                    ) {
                        if (prescription.isIncomplete) {
                            FailureStatusChip()
                        } else if (showDirectAssignmentLabel) {
                            DirectAssignmentStatusChip(prescription.redeemedOn != null)
                        } else {
                            when (prescription.state()) {
                                is TaskStateErpModel.Ready -> ReadyStatusChip()

                                is TaskStateErpModel.InProgress -> InProgressStatusChip()

                                is TaskStateErpModel.Pending -> PendingStatusChip()
                                is TaskStateErpModel.Expired -> ExpiredStatusChip()
                                is TaskStateErpModel.LaterRedeemable -> LaterRedeemableStatusChip()

                                is TaskStateErpModel.Other -> {
                                    when ((prescription.state() as? TaskStateErpModel.Other)?.state) {
                                        FhirTaskStatusErpModel.Completed -> CompletedStatusChip()
                                        else -> UnknownStatusChip()
                                    }
                                }

                                is TaskStateErpModel.Deleted -> DeletedStatusChip()
                                is TaskStateErpModel.Provided -> ProvidedStatusChip()
                            }
                        }
                        if (isPartOfMultiplePrescription) {
                            letNotNull(numerator, denominator) { numerator, denominator ->
                                SpacerSmall()
                                NumeratorChip(numerator, denominator)
                            }
                        }
                        if (isSelfPayPrescription) {
                            SpacerSmall()
                            SelfPayerPrescriptionChip()
                        }
                    }
                }

                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = AppTheme.colors.neutral700,
                    modifier =
                    Modifier
                        .size(SizeDefaults.triple)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Suppress("FunctionNaming", "MagicNumber")
@LightDarkPreview
@Composable
fun FullDetailMedicationPreview() {
    PreviewAppTheme {
        FullDetailMedication(
            prescription = TaskErpModel.Synced.Prescription(
                profileId = "preview-profile",
                taskId = "1",
                name = "Ibuprofen",
                accessCode = "",
                isEuRedeemable = false,
                isEuRedeemableByPatientAuthorization = false,
                lastModified = Instant.fromEpochSeconds(123456),
                organization = null,
                practitioner = null,
                patient = null,
                insuranceInformation = null,
                expiresOn = Instant.DISTANT_FUTURE,
                acceptUntil = Instant.DISTANT_FUTURE,
                authoredOn = Instant.fromEpochSeconds(123456),
                status = TaskStatusEnum.Ready,
                isIncomplete = false,
                pvsIdentifier = "",
                failureToReport = "",
                medicationRequest = de.gematik.ti.erp.app.task.model.MedicationRequestErpModel(
                    substitutionAllowed = false,
                    multiplePrescriptionInfo = de.gematik.ti.erp.app.task.model.MultiplePrescriptionInfo(
                        indicator = true,
                        numbering = de.gematik.ti.erp.app.task.model.RatioErpModel(
                            numerator = de.gematik.ti.erp.app.task.model.QuantityErpModel("1", ""),
                            denominator = de.gematik.ti.erp.app.task.model.QuantityErpModel("2", "")
                        )
                    ),
                    note = null
                ),
                medicationDispenses = emptyList()
            )
        ) { }
    }
}

@Suppress("FunctionNaming", "MagicNumber", "UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun FullDetailMedicationInProcessPreview() {
    PreviewAppTheme {
        FullDetailMedication(
            prescription = TaskErpModel.Synced.Prescription(
                profileId = "preview-profile",
                taskId = "1",
                name = "Ibuprofen",
                accessCode = "",
                isEuRedeemable = false,
                isEuRedeemableByPatientAuthorization = false,
                lastModified = Instant.fromEpochSeconds(123456),
                organization = null,
                practitioner = null,
                patient = null,
                insuranceInformation = null,
                expiresOn = Instant.DISTANT_FUTURE,
                acceptUntil = Instant.DISTANT_FUTURE,
                authoredOn = Instant.fromEpochSeconds(123456),
                status = TaskStatusEnum.InProgress,
                isIncomplete = false,
                pvsIdentifier = "",
                failureToReport = "",
                medicationRequest = null,
                medicationDispenses = emptyList()
            )
        ) { }
    }
}

@Suppress("FunctionNaming", "MagicNumber", "UnusedPrivateMember")
@LightDarkPreview
@Composable
private fun FullDetailMedicationCompletedPreview() {
    PreviewAppTheme {
        FullDetailMedication(
            prescription = TaskErpModel.Synced.Prescription(
                profileId = "preview-profile",
                taskId = "1",
                name = "Ibuprofen",
                accessCode = "",
                isEuRedeemable = false,
                isEuRedeemableByPatientAuthorization = false,
                lastModified = Instant.fromEpochSeconds(123456),
                organization = null,
                practitioner = null,
                patient = null,
                insuranceInformation = null,
                expiresOn = Instant.DISTANT_FUTURE,
                acceptUntil = Instant.DISTANT_FUTURE,
                authoredOn = Instant.fromEpochSeconds(123456),
                status = TaskStatusEnum.Completed,
                isIncomplete = false,
                pvsIdentifier = "",
                failureToReport = "",
                medicationRequest = null,
                medicationDispenses = emptyList()
            )
        ) { }
    }
}
