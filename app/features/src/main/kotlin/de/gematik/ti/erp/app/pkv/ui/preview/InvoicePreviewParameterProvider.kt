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
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.Year
import de.gematik.ti.erp.app.invoice.model.ChargeableItemDescriptionErpModel
import de.gematik.ti.erp.app.invoice.model.ChargeableItemErpModel
import de.gematik.ti.erp.app.invoice.model.InvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PriceComponentErpModel
import de.gematik.ti.erp.app.pkv.model.InvoiceState
import de.gematik.ti.erp.app.task.model.MedicationErpModel
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import kotlinx.datetime.Instant

data class InvoiceDetailScreenPreviewData(
    val isFromPrescriptionDetails: Boolean,
    val invoiceState: InvoiceState
)

data class InvoiceListScreenPreviewData(
    val invoices: Map<Year, List<PKVInvoiceErpModel>>,
    val isSsoTokenValid: Boolean,
    val isConsentGranted: Boolean
)

private val invoiceListData = InvoiceListScreenPreviewData(
    invoices = mapOf(
        Year(2023) to listOf(
            PkvMockData.erpModel.copy(
                timestamp = Instant.parse("2023-10-23T12:34:56Z"),
                medicationRequest = PkvMockData.medicationRequest.copy(
                    medication = PkvMockData.medication.copy(
                        text = "Medikament 1"
                    )
                )
            )
        ),
        Year(2022) to listOf(
            PkvMockData.erpModel.copy(
                timestamp = Instant.parse("2024-11-23T12:34:56Z"),
                medicationRequest = PkvMockData.medicationRequest.copy(
                    medication = PkvMockData.medication.copy(
                        text = "Medikament 2"
                    )
                )
            ),
            PkvMockData.erpModel.copy(
                timestamp = Instant.parse("2024-10-23T12:34:56Z"),
                medicationRequest = PkvMockData.medicationRequest.copy(
                    medication = PkvMockData.medication.copy(
                        text = "Medikament 3"
                    )
                )
            )
        )
    ),
    isSsoTokenValid = true,
    isConsentGranted = true
)

class InvoiceExpandedDetailsScreenPreviewParameterProvider : PreviewParameterProvider<PKVInvoiceErpModel?> {
    override val values: Sequence<PKVInvoiceErpModel?>
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
                record = PkvMockData.erpModel
            )
        )
    )
}

object PkvMockData {
    val timestamp = Instant.parse("1988-10-23T12:34:56Z")
    val handoverTimestamp = Instant.parse(("2021-11-25T15:20:00Z"))

    val chargeableItem = ChargeableItemErpModel(
        description = ChargeableItemDescriptionErpModel.PZN("pzn"),
        text = "text",
        factor = 2.0,
        price = PriceComponentErpModel(value = 1.0, tax = 1.0)
    )

    val invoice = InvoiceErpModel(
        totalAdditionalFee = 1.0,
        totalBruttoAmount = 489.73,
        currency = "currency",
        additionalInformation = listOf("additionalInformation"),
        chargeableItems = listOf(chargeableItem),
        additionalDispenseItems = listOf(chargeableItem)
    )

    val medication = MedicationErpModel(
        text = "Präparat"
    )

    val medicationRequest = MedicationRequestErpModel(
        medication = medication,
        authoredOn = FhirTemporal.Instant(timestamp),
        substitutionAllowed = true
    )

    val erpModel = PKVInvoiceErpModel(
        profileId = "profileId",
        taskId = "taskId",
        accessCode = "accessCode",
        timestamp = timestamp,
        pharmacyOrganization = OrganizationErpModel(name = "Medikamenten Apotheke"),
        practitionerOrganization = OrganizationErpModel(name = "practitionerOrganization"),
        practitioner = PractitionerErpModel(name = "Max Mustermann"),
        patient = PatientErpModel(name = "name"),
        medicationRequest = medicationRequest,
        whenHandedOver = FhirTemporal.Instant(value = handoverTimestamp),
        invoice = invoice,
        consumed = false
    )
}
