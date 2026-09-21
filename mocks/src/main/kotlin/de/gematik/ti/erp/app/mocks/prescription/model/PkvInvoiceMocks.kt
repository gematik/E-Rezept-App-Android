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

package de.gematik.ti.erp.app.mocks.prescription.model

import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import de.gematik.ti.erp.app.invoice.model.InvoiceErpModel
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.mocks.TASK_ID
import de.gematik.ti.erp.app.task.model.OrganizationErpModel

fun mockPKVInvoiceErpModel(
    profileId: String = PROFILE_ID,
    taskId: String = TASK_ID
) = PKVInvoiceErpModel(
    profileId = profileId,
    taskId = taskId,
    accessCode = "98765",
    timestamp = DATE_2024_01_01,
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
    whenHandedOver = DATE_2024_01_01.asFhirTemporal(),
    consumed = false
)
