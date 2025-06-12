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
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
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
import de.gematik.ti.erp.app.prescription.ui.UnknownStatusChip
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.SyncedPrescription
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus

@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun FullDetailMedication(
    prescription: SyncedPrescription,
    modifier: Modifier = Modifier,
    now: Instant = Clock.System.now(),
    onClick: () -> Unit
) {
    val showDirectAssignmentLabel by remember(prescription) {
        derivedStateOf {
            val isCompleted =
                (prescription.state as? SyncedTaskData.SyncedTask.Other)?.state == SyncedTaskData.TaskStatus.Completed

            prescription.isDirectAssignment && !isCompleted
        }
    }

    Box() {
        Card(
            modifier =
            modifier
                .semantics {
                    prescriptionId = prescription.taskId
                }
                .testTag(TestTag.Prescriptions.FullDetailPrescription),
            shape = RoundedCornerShape(SizeDefaults.double),
            border = BorderStroke(SizeDefaults.eighth, color = AppTheme.colors.neutral300),
            backgroundColor = AppTheme.colors.neutral050,
            elevation = SizeDefaults.zero,
            onClick = onClick
        ) {
            val textColor = AppTheme.colors.neutral800
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

                    if (!prescription.isDirectAssignment) {
                        PrescriptionStateInfo(
                            state = prescription.state,
                            now = now
                        )
                    }

                    SpacerSmall()

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(SizeDefaults.onefold)
                    ) {
                        if (prescription.isIncomplete) {
                            FailureStatusChip()
                        } else if (showDirectAssignmentLabel) {
                            DirectAssignmentStatusChip(prescription.redeemedOn != null)
                        } else {
                            when (prescription.state) {
                                is SyncedTaskData.SyncedTask.Ready -> ReadyStatusChip()

                                is SyncedTaskData.SyncedTask.InProgress -> InProgressStatusChip()

                                is SyncedTaskData.SyncedTask.Pending -> PendingStatusChip()
                                is SyncedTaskData.SyncedTask.Expired -> ExpiredStatusChip()
                                is SyncedTaskData.SyncedTask.LaterRedeemable -> LaterRedeemableStatusChip()

                                is SyncedTaskData.SyncedTask.Other -> {
                                    when ((prescription.state as? SyncedTaskData.SyncedTask.Other)?.state) {
                                        SyncedTaskData.TaskStatus.Completed -> CompletedStatusChip()
                                        else -> UnknownStatusChip()
                                    }
                                }

                                is SyncedTaskData.SyncedTask.Deleted -> DeletedStatusChip()
                                is SyncedTaskData.SyncedTask.Provided -> ProvidedStatusChip()
                            }
                        }
                        if (prescription.prescriptionChipInformation.isPartOfMultiplePrescription) {
                            prescription.prescriptionChipInformation.numerator?.let { numerator ->
                                prescription.prescriptionChipInformation.denominator?.let { denominator ->
                                    SpacerSmall()
                                    NumeratorChip(numerator, denominator)
                                }
                            }
                        }
                        if (prescription.prescriptionChipInformation.isSelfPayPrescription) {
                            SpacerSmall()
                            SelfPayerPrescriptionChip()
                        }
                    }
                }

                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = AppTheme.colors.neutral400,
                    modifier =
                    Modifier
                        .size(SizeDefaults.triple)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Suppress("FunctionNaming")
@LightDarkPreview
@Composable
fun FullDetailMedicationPreview() {
    val now = Clock.System.now()
    val later = now.plus(2, DateTimeUnit.HOUR)
    PreviewAppTheme {
        FullDetailMedication(
            prescription =
            SyncedPrescription(
                taskId = "1",
                name = "Ibuprofen",
                state =
                SyncedTaskData.SyncedTask.Ready(
                    expiresOn = Instant.DISTANT_FUTURE,
                    acceptUntil = Instant.DISTANT_FUTURE
                ),
                isDirectAssignment = false,
                isIncomplete = false,
                acceptUntil = later,
                authoredOn = now,
                expiresOn = later,
                redeemedOn = null,
                organization = "Organization",
                isDiga = false,
                deviceRequestState = DigaStatus.Ready,
                lastModified = Instant.fromEpochSeconds(123456),
                prescriptionChipInformation =
                Prescription.PrescriptionChipInformation(
                    isPartOfMultiplePrescription = true,
                    numerator = "1",
                    denominator = "2"
                )
            )
        ) { }
    }
}

@Suppress("FunctionNaming")
@LightDarkPreview
@Composable
fun FullDetailMedicationInProcessPreview() {
    val now = Clock.System.now()
    val later = now.plus(2, DateTimeUnit.HOUR)
    PreviewAppTheme {
        FullDetailMedication(
            prescription =
            SyncedPrescription(
                taskId = "1",
                name = "Ibuprofen",
                state =
                SyncedTaskData.SyncedTask.InProgress(SyncedTaskData.TaskStateSerializationType.InProgress, Instant.DISTANT_FUTURE),
                isDirectAssignment = false,
                isIncomplete = false,
                acceptUntil = later,
                authoredOn = now,
                expiresOn = later,
                redeemedOn = null,
                organization = "Organization",
                isDiga = false,
                deviceRequestState = DigaStatus.Ready,
                lastModified = Instant.fromEpochSeconds(123456),
                prescriptionChipInformation =
                Prescription.PrescriptionChipInformation(
                    isPartOfMultiplePrescription = false,
                    numerator = "1",
                    denominator = "2"
                )
            )
        ) { }
    }
}

@Suppress("FunctionNaming")
@LightDarkPreview
@Composable
fun FullDetailMedicationCompletedPreview() {
    val now = Clock.System.now()
    val later = now.plus(2, DateTimeUnit.HOUR)
    PreviewAppTheme {
        FullDetailMedication(
            prescription =
            SyncedPrescription(
                taskId = "1",
                name = "Ibuprofen",
                state =
                SyncedTaskData.SyncedTask.Other(state = SyncedTaskData.TaskStatus.Completed, lastModified = Instant.DISTANT_FUTURE),
                isDirectAssignment = false,
                isIncomplete = false,
                acceptUntil = later,
                authoredOn = now,
                expiresOn = later,
                redeemedOn = null,
                organization = "Organization",
                isDiga = false,
                deviceRequestState = DigaStatus.Ready,
                lastModified = Instant.fromEpochSeconds(123456),
                prescriptionChipInformation =
                Prescription.PrescriptionChipInformation(
                    isPartOfMultiplePrescription = false,
                    numerator = "1",
                    denominator = "2"
                )
            )
        ) { }
    }
}
