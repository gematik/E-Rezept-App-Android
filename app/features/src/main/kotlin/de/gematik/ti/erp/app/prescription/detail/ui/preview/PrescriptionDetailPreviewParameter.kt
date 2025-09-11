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

@file:Suppress("UnusedPrivateProperty")

package de.gematik.ti.erp.app.prescription.detail.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.messages.model.CommunicationProfile.ErxCommunicationDispReq
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Medication
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Patient
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Practitioner
import kotlinx.datetime.Instant

data class PrescriptionDetailPreview(
    val name: String, // a description for better understanding
    val prescription: PrescriptionData.Prescription,
    val now: Instant
)

class PrescriptionDetailPreviewParameter : PreviewParameterProvider<PrescriptionDetailPreview> {

    override val values: Sequence<PrescriptionDetailPreview>
        get() = sequenceOf(
            PrescriptionDetailPreview(
                name = "in_progress_with_accepted_time",
                prescription = PREVIEW_SYNCED_PRESCRIPTION,
                now = hoursFromLastModifiedDate // for showing the accepted time
            ),
            PrescriptionDetailPreview(
                name = "deleted_with_last_modified_date",
                prescription = PREVIEW_SYNCED_PRESCRIPTION.copy(
                    task = SYNCED_TASK.copy(
                        status = SyncedTaskData.TaskStatus.Canceled
                    )
                ),
                now = hoursFromLastModifiedDate // for showing the accepted time
            ),
            PrescriptionDetailPreview(
                name = "provided_with_last_modified_date",
                prescription = PREVIEW_SYNCED_PRESCRIPTION.copy(
                    task = SYNCED_TASK.copy(
                        status = SyncedTaskData.TaskStatus.InProgress,
                        lastMedicationDispense = lastModifiedDate
                    )
                ),
                now = lastModifiedDate
            ),
            PrescriptionDetailPreview(
                name = "in_progress_with_accepted_date",
                prescription = PREVIEW_SYNCED_PRESCRIPTION,
                now = futureOfLastModifiedDate // for showing accepted date
            ),
            PrescriptionDetailPreview(
                name = "waiting_for_answer_from_pharmacy",
                prescription = PREVIEW_SYNCED_PRESCRIPTION.copy(
                    task = SYNCED_TASK.copy(
                        status = SyncedTaskData.TaskStatus.Ready,
                        currentTime = nowForWaitingState,
                        communications = listOf(
                            PREVIEW_REQUEST_COMMUNICATION
                        )
                    )
                ),
                now = nowForWaitingState
            ),
            PrescriptionDetailPreview(
                name = "synced_prescription_with_boolean_parameters_true",
                prescription = PREVIEW_SYNCED_PRESCRIPTION_BOOLEAN_PARAMETERS_TRUE,
                now = futureOfLastModifiedDate
            ),
            PrescriptionDetailPreview(
                name = "synced_prescription_with_boolean_parameters_false",
                prescription = PREVIEW_SYNCED_PRESCRIPTION_BOOLEAN_PARAMETERS_FALSE,
                now = futureOfLastModifiedDate
            ),
            PrescriptionDetailPreview(
                name = "synced_prescription_as_multiple_prescription",
                prescription = PREVIEW_SYNCED_PRESCRIPTION_MULTIPLE_PRESCRIPTION,
                now = futureOfLastModifiedDate
            ),
            PrescriptionDetailPreview(
                name = "synced_prescription_as_selfPayer_prescription",
                prescription = PREVIEW_SYNCED_PRESCRIPTION.copy(
                    task = SYNCED_TASK.copy(
                        insuranceInformation = SyncedTaskData.InsuranceInformation(
                            name = "Insurance",
                            status = "Selbstzahler",
                            coverageType = SyncedTaskData.CoverageType.SEL
                        )
                    )
                ),
                now = hoursFromLastModifiedDate // for showing the accepted time
            ),
            PrescriptionDetailPreview(
                name = "scanned_prescription",
                prescription = PREVIEW_SCANNED_PRESCRIPTION,
                now = futureOfLastModifiedDate
            ),
            PrescriptionDetailPreview(
                name = "scanned_redeemed_prescription",
                prescription = PREVIEW_SCANNED_REDEEMED_PRESCRIPTION,
                now = futureOfLastModifiedDate
            ),
            PrescriptionDetailPreview(
                name = "synced_prescription_direct_assignment",
                prescription = PREVIEW_SYNCED_PRESCRIPTION.copy(
                    task = SYNCED_TASK.copy(
                        taskId = "169task-id-1"
                    )
                ),
                now = hoursFromLastModifiedDate
            )
        )
}

private val lastModifiedDate = Instant.parse("2024-07-03T14:20:00Z")
private val sentOnDate = Instant.parse("2024-07-03T15:20:00Z") // lastModifiedDate < sentOnDate
private val nowForWaitingState = Instant.parse("2024-07-03T14:00:00Z")
private val hoursFromLastModifiedDate = Instant.parse("2024-07-03T15:20:00Z")
private val futureOfLastModifiedDate = Instant.parse("2024-09-03T15:20:00Z")
private val farAwayFutureDate = Instant.parse("3021-11-25T15:20:00Z")

internal val SYNCED_TASK = SyncedTaskData.SyncedTask(
    profileId = "profile-id-1",
    taskId = "task-id-1",
    accessCode = "access-code-1",
    lastModified = lastModifiedDate, // change in this changes the date in the detail screen
    organization = SyncedTaskData.Organization(
        name = "Muster Apotheke",
        address = SyncedTaskData.Address(
            line1 = "Musterstraße 1",
            line2 = "1. Stock",
            postalCode = "12345",
            city = "Musterstadt"
        ),
        uniqueIdentifier = "1234567890",
        phone = "0123456789",
        mail = "muster@muster.de"
    ),
    practitioner = Practitioner(
        name = "Dr. Max Mustermann",
        qualification = "Arzt",
        practitionerIdentifier = "1234567890"
    ),
    patient = Patient(
        name = "Max Mustermann",
        address = SyncedTaskData.Address(
            line1 = "Musterstraße 1",
            line2 = "1. Stock",
            postalCode = "12345",
            city = "Musterstadt"
        ),
        birthdate = null,
        insuranceIdentifier = "1234567890"
    ),
    insuranceInformation = SyncedTaskData.InsuranceInformation(
        name = null,
        status = null,
        coverageType = SyncedTaskData.CoverageType.GKV
    ),
    expiresOn = Instant.parse("2028-11-25T15:20:00Z"),
    acceptUntil = Instant.parse("3021-11-25T15:20:00Z"),
    authoredOn = Instant.parse("2021-11-25T15:20:00Z"),
    status = SyncedTaskData.TaskStatus.InProgress,
    medicationRequest = SyncedTaskData.MedicationRequest(
        medication = Medication(
            category = SyncedTaskData.MedicationCategory.entries[0],
            medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                type = ErpMedicationProfileType.PZN,
                version = ErpMedicationProfileVersion.V_110
            ),
            vaccine = true,
            text = "Ibuprofen",
            form = "AEO",
            lotNumber = "1234567890",
            expirationDate = FhirTemporal.Instant(farAwayFutureDate),
            identifier = SyncedTaskData.Identifier("1234567890"),
            normSizeCode = "KA",
            amount = Ratio(
                numerator = Quantity(
                    value = "1",
                    unit = "oz"
                ),
                denominator = null
            ),
            manufacturingInstructions = null,
            packaging = null,
            ingredientMedications = emptyList(),
            ingredients = emptyList()
        ),
        dateOfAccident = null,
        location = "Musterstadt",
        emergencyFee = true,
        dosageInstruction = "1-1-1",
        multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
        note = "Keine",
        substitutionAllowed = true,
        additionalFee = SyncedTaskData.AdditionalFee.None
    ),
    lastMedicationDispense = null,
    medicationDispenses = emptyList(),
    communications = emptyList(),
    failureToReport = "",
    isIncomplete = false,
    pvsIdentifier = "1234567890",
    isEuRedeemable = false
)

private val PREVIEW_SYNCED_PRESCRIPTION = PrescriptionData.Synced(
    task = SYNCED_TASK
)

private val PREVIEW_SYNCED_PRESCRIPTION_BOOLEAN_PARAMETERS_TRUE = PrescriptionData.Synced(
    task = SYNCED_TASK.copy(
        status = SyncedTaskData.TaskStatus.Ready,
        medicationRequest = SYNCED_TASK.medicationRequest.copy(
            emergencyFee = true,
            substitutionAllowed = true,
            additionalFee = SyncedTaskData.AdditionalFee.Exempt
        )
    )
)

private val PREVIEW_SYNCED_PRESCRIPTION_BOOLEAN_PARAMETERS_FALSE = PrescriptionData.Synced(
    task = SYNCED_TASK.copy(
        medicationRequest = SYNCED_TASK.medicationRequest.copy(
            emergencyFee = false,
            substitutionAllowed = false,
            additionalFee = SyncedTaskData.AdditionalFee.NotExempt
        )
    )
)

private val PREVIEW_SYNCED_PRESCRIPTION_MULTIPLE_PRESCRIPTION = PrescriptionData.Synced(
    task = SYNCED_TASK.copy(
        medicationRequest = SYNCED_TASK.medicationRequest.copy(
            multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(
                indicator = true,
                numbering = Ratio(
                    numerator = Quantity(
                        value = "1",
                        unit = ""
                    ),
                    denominator = Quantity(
                        value = "3",
                        unit = ""
                    )
                )
            )
        )
    )
)

private val PREVIEW_SCANNED_PRESCRIPTION = PrescriptionData.Scanned(
    task = ScannedTaskData.ScannedTask(
        profileId = "profile-id-1",
        taskId = "task-id-1",
        scannedOn = Instant.parse("2021-11-25T15:20:00Z"),
        index = 1,
        name = "Ibuprofen",
        accessCode = "access-code-1",
        redeemedOn = null
    )
)

private val PREVIEW_SCANNED_REDEEMED_PRESCRIPTION = PrescriptionData.Scanned(
    task = ScannedTaskData.ScannedTask(
        profileId = "profile-id-1",
        taskId = "task-id-1",
        scannedOn = Instant.parse("2021-11-25T15:20:00Z"),
        index = 1,
        name = "Ibuprofen",
        accessCode = "access-code-1",
        redeemedOn = Instant.parse("2021-11-25T15:20:00Z")
    )
)

private val PREVIEW_REQUEST_COMMUNICATION = Communication(
    taskId = "task-id-1",
    communicationId = "communication-Id-1",
    sentOn = sentOnDate,
    sender = "pharmacy-Id-1",
    consumed = false,
    profile = ErxCommunicationDispReq,
    orderId = "order-Id-1",
    payload = "",
    recipient = "Max Mustermann"
)
