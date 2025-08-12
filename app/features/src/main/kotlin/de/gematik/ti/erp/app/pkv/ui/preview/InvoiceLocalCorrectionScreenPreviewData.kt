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
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.pkv.ui.preview.InvoiceLocalCorrectionScreenPreviewData.pkvInvoiceRecord
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.datetime.Instant

class InvoiceLocalCorrectionScreenPreviewParameterProvider : PreviewParameterProvider<InvoiceData.PKVInvoiceRecord?> {
    override val values = sequenceOf(
        pkvInvoiceRecord,
        null
    )
}

object InvoiceLocalCorrectionScreenPreviewData {

    val time: Instant = Instant.parse("2023-06-14T10:15:30Z")

    val pkvInvoiceRecord = InvoiceData.PKVInvoiceRecord(
        profileId = "1234",
        taskId = "01234",
        accessCode = "98765",
        timestamp = time,
        invoice = InvoiceData.Invoice(
            2.30,
            6.80,
            "EUR",
            listOf(),
            listOf()
        ),
        pharmacyOrganization = SyncedTaskData.Organization(
            "Pharmacy",
            SyncedTaskData.Address("", "", "", ""),
            null,
            null,
            null
        ),
        practitionerOrganization = SyncedTaskData.Organization(
            "Practitioner",
            SyncedTaskData.Address("", "", "", ""),
            null,
            null,
            null
        ),
        practitioner = SyncedTaskData.Practitioner("Practitioner", "", ""),
        patient = SyncedTaskData.Patient(
            "Patient",
            SyncedTaskData.Address("", "", "", ""),
            null,
            null
        ),
        medicationRequest = SyncedTaskData.MedicationRequest(
            SyncedTaskData.Medication(
                category = SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL,
                vaccine = true,
                text = "Medication Name",
                form = "Form",
                lotNumber = "lot number",
                expirationDate = null,
                identifier = SyncedTaskData.Identifier("1234567890"),
                normSizeCode = "norm size code",
                amount = Ratio(
                    numerator = Quantity(
                        value = "2",
                        unit = "1"
                    ),
                    denominator = null
                ),
                ingredientMedications = emptyList(),
                ingredients = emptyList(),
                manufacturingInstructions = null,
                packaging = null
            ),
            null, null, SyncedTaskData.AccidentType.None,
            null, null, false, null,
            SyncedTaskData.MultiplePrescriptionInfo(false), 1, "Note", true, SyncedTaskData.AdditionalFee.NotExempt
        ),
        whenHandedOver = time.asFhirTemporal(),
        consumed = false
    )
}
