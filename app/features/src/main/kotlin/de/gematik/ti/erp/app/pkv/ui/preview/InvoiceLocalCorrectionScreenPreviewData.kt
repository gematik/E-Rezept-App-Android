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
import de.gematik.ti.erp.app.invoice.model.InvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.pkv.ui.preview.InvoiceLocalCorrectionScreenPreviewData.pkvInvoiceErpModel
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import kotlinx.datetime.Instant

class InvoiceLocalCorrectionScreenPreviewParameterProvider : PreviewParameterProvider<PKVInvoiceErpModel?> {
    override val values = sequenceOf(
        pkvInvoiceErpModel,
        null
    )
}

object InvoiceLocalCorrectionScreenPreviewData {

    val time: Instant = Instant.parse("2023-06-14T10:15:30Z")

    val pkvInvoiceErpModel = PKVInvoiceErpModel(
        profileId = "1234",
        taskId = "01234",
        accessCode = "98765",
        timestamp = time,
        invoice = InvoiceErpModel(
            totalAdditionalFee = 2.30,
            totalBruttoAmount = 6.80,
            currency = "EUR",
            chargeableItems = listOf(),
            additionalDispenseItems = listOf()
        ),
        pharmacyOrganization = OrganizationErpModel(
            name = "Pharmacy"
        ),
        practitionerOrganization = OrganizationErpModel(
            name = "Practitioner"
        ),
        practitioner = null,
        patient = null,
        medicationRequest = null,
        whenHandedOver = time.asFhirTemporal(),
        consumed = false
    )
}
