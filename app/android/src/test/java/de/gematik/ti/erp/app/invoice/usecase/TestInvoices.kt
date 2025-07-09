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

package de.gematik.ti.erp.app.invoice.usecase

import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.utils.asFhirTemporal
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

val now = Clock.System.now()
val later = now.plus(8760, DateTimeUnit.HOUR)

val pkvInvoiceRecord = InvoiceData.PKVInvoiceRecord(
    profileId = "1234",
    taskId = "01234",
    accessCode = "98765",
    timestamp = now,
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
        null, null, null, SyncedTaskData.AccidentType.None,
        null, null, false, null,
        SyncedTaskData.MultiplePrescriptionInfo(false), 1, null, null, SyncedTaskData.AdditionalFee.None
    ),
    whenHandedOver = now.asFhirTemporal(),
    consumed = false

)

val pkvInvoiceRecord2 = InvoiceData.PKVInvoiceRecord(
    profileId = "23456",
    taskId = "65432",
    accessCode = "98765",
    timestamp = later,
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
        null, null, null, SyncedTaskData.AccidentType.None,
        null, null, false, null,
        SyncedTaskData.MultiplePrescriptionInfo(false), 1, null, null, SyncedTaskData.AdditionalFee.None
    ),
    whenHandedOver = later.asFhirTemporal(),
    consumed = false

)
