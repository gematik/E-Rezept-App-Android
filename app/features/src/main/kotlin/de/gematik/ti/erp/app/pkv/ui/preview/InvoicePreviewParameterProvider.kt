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

package de.gematik.ti.erp.app.pkv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.Year
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.pkv.model.InvoiceState
import de.gematik.ti.erp.app.pkv.ui.preview.PkvMockData.invoiceRecord
import de.gematik.ti.erp.app.pkv.ui.preview.PkvMockData.medicationPzn
import de.gematik.ti.erp.app.pkv.ui.preview.PkvMockData.medicationRequest
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationRequest
import kotlinx.datetime.Instant
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal.Instant as FhirInstant

data class InvoiceDetailScreenPreviewData(
    val isFromPrescriptionDetails: Boolean,
    val invoiceState: InvoiceState
)

data class InvoiceListScreenPreviewData(
    val invoices: Map<Year, List<InvoiceData.PKVInvoiceRecord>>,
    val isSsoTokenValid: Boolean,
    val isConsentGranted: Boolean
)

private val invoiceListData = InvoiceListScreenPreviewData(
    invoices = mapOf(
        Year(2023) to listOf(
            invoiceRecord.copy(
                timestamp = Instant.parse("2023-10-23T12:34:56Z"),
                medicationRequest = medicationRequest.copy(
                    medication = medicationPzn.copy(
                        text = "Medikament 1"
                    )
                )
            )
        ),
        Year(2022) to listOf(
            invoiceRecord.copy(
                timestamp = Instant.parse("2024-11-23T12:34:56Z"),
                medicationRequest = medicationRequest.copy(
                    medication = medicationPzn.copy(
                        text = "Medikament 2"
                    )
                )
            ),
            invoiceRecord.copy(
                timestamp = Instant.parse("2024-10-23T12:34:56Z"),
                medicationRequest = medicationRequest.copy(
                    medication = medicationPzn.copy(
                        text = "Medikament 3"
                    )
                )
            )
        )
    ),
    isSsoTokenValid = true,
    isConsentGranted = true
)

class InvoiceExpandedDetailsScreenPreviewParameterProvider : PreviewParameterProvider<InvoiceData.PKVInvoiceRecord?> {
    override val values: Sequence<InvoiceData.PKVInvoiceRecord?>
        get() = sequenceOf(invoiceListData.invoices.values.first().first())
}

class InvoiceListScreenPreviewParameterProvider : PreviewParameterProvider<InvoiceListScreenPreviewData> {
    override val values: Sequence<InvoiceListScreenPreviewData>
        get() = sequenceOf(
            invoiceListData,
            InvoiceListScreenPreviewData(
                invoices = emptyMap(),
                isSsoTokenValid = false,
                isConsentGranted = false
            )
        )
}

class InvoiceDetailScreenPreviewParameterProvider : PreviewParameterProvider<InvoiceDetailScreenPreviewData> {
    override val values = sequenceOf(
        InvoiceDetailScreenPreviewData(
            isFromPrescriptionDetails = true,
            invoiceState = InvoiceState.NoInvoice
        ),
        InvoiceDetailScreenPreviewData(
            isFromPrescriptionDetails = false,
            invoiceState = InvoiceState.NoInvoice
        ),
        InvoiceDetailScreenPreviewData(
            isFromPrescriptionDetails = true,
            invoiceState = InvoiceState.InvoiceLoaded(
                record = invoiceRecord
            )
        )
    )
}

object PkvMockData {
    val chargeItem = InvoiceData.ChargeableItem(
        description = InvoiceData.ChargeableItem.Description.PZN("pzn"),
        text = "text",
        factor = 2.0,
        price = InvoiceData.PriceComponent(
            value = 1.0,
            tax = 1.0
        )
    )
    val invoice = InvoiceData.Invoice(
        totalAdditionalFee = 1.0,
        totalBruttoAmount = 489.73,
        currency = "currency",
        additionalInformation = listOf("additionalInformation"),
        chargeableItems = listOf(chargeItem),
        additionalDispenseItems = listOf(chargeItem)
    )
    val timestamp = Instant.parse("1988-10-23T12:34:56Z")
    val handoverTimestamp = Instant.parse(("2021-11-25T15:20:00Z"))
    val address = SyncedTaskData.Address(
        line1 = "line1",
        line2 = "line2",
        postalCode = "postalCode",
        city = "city"
    )
    val medicationPzn = SyncedTaskData.Medication(
        category = SyncedTaskData.MedicationCategory.entries[0],
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_110
        ),
        vaccine = true,
        text = "Präparat",
        form = "AEO",
        lotNumber = "lotNumber",
        expirationDate = FhirTemporal.Instant(timestamp),
        identifier = SyncedTaskData.Identifier("FJHE98383JGK"),
        normSizeCode = "FRE4347",
        amount = Ratio(
            numerator = Quantity(
                value = "2",
                unit = "oz"
            ),
            denominator = null
        ),
        ingredientMedications = emptyList(),
        ingredients = emptyList(),
        manufacturingInstructions = null,
        packaging = null
    )
    val medicationRequest = MedicationRequest(
        medication = medicationPzn,
        dateOfAccident = null,
        location = "location",
        emergencyFee = true,
        dosageInstruction = "dosageInstruction",
        multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
        note = "note",
        substitutionAllowed = true
    )
    val invoiceRecord = InvoiceData.PKVInvoiceRecord(
        profileId = "profileId",
        taskId = "taskId",
        accessCode = "accessCode",
        timestamp = timestamp,
        pharmacyOrganization = SyncedTaskData.Organization(
            name = "Medikamenten Apotheke",
            address = address,
            uniqueIdentifier = "uniqueIdentifier"
        ),
        practitionerOrganization = SyncedTaskData.Organization(
            name = "practitionerOrganization",
            address = address,
            uniqueIdentifier = "uniqueIdentifier"
        ),
        practitioner = SyncedTaskData.Practitioner(
            name = "Max Mustermann",
            qualification = "qualification",
            practitionerIdentifier = "practitionerIdentifier"
        ),
        patient = SyncedTaskData.Patient(
            name = "name",
            address = address,
            insuranceIdentifier = "insuranceIdentifier",
            birthdate = FhirInstant(value = timestamp)
        ),
        medicationRequest = medicationRequest,
        whenHandedOver = FhirTemporal.Instant(value = handoverTimestamp),
        invoice = invoice,
        consumed = false
    )
}
