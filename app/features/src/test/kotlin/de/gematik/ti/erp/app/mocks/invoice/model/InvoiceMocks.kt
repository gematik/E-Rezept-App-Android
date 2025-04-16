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

package de.gematik.ti.erp.app.mocks.invoice.model

import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.mocks.TASK_ID
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Organization
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

val MOCK_CHARGEABLE_ITEM = InvoiceData.ChargeableItem(
    description = InvoiceData.ChargeableItem.Description.PZN("pzn"),
    text = "text",
    factor = 1.0,
    price = InvoiceData.PriceComponent(
        value = 10.0,
        tax = 10.0
    )
)

val MOCK_INVOICE = InvoiceData.Invoice(
    totalAdditionalFee = 10.0,
    totalBruttoAmount = 10.0,
    currency = "EUR",
    chargeableItems = listOf(MOCK_CHARGEABLE_ITEM),
    additionalDispenseItems = listOf(MOCK_CHARGEABLE_ITEM),
    additionalInformation = listOf("additionalInformation")
)

val MOCK_MEDICATION_REQUEST = SyncedTaskData.MedicationRequest(
    null, null, null, SyncedTaskData.AccidentType.None,
    null, null, false, null,
    SyncedTaskData.MultiplePrescriptionInfo(false), 1, null, null, SyncedTaskData.AdditionalFee.None
)

val MOCK_INVOICE_ADDRESS = SyncedTaskData.Address(
    line1 = "line1",
    line2 = "line2",
    postalCode = "postalCode",
    city = "city"
)

fun mockPkvInvoiceRecord(
    profileId: String = PROFILE_ID,
    taskId: String = TASK_ID
) = InvoiceData.PKVInvoiceRecord(
    profileId = profileId,
    taskId = taskId,
    accessCode = "accessCode",
    timestamp = DATE_2024_01_01,
    pharmacyOrganization = Organization(
        name = "pharmacyName",
        address = MOCK_INVOICE_ADDRESS,
        uniqueIdentifier = "uniqueIdentifier",
        phone = "phone",
        mail = "mail"
    ),
    practitionerOrganization = Organization(
        name = "pharmacyName",
        address = MOCK_INVOICE_ADDRESS,
        uniqueIdentifier = "uniqueIdentifier",
        phone = "phone",
        mail = "mail"
    ),
    practitioner = SyncedTaskData.Practitioner(
        name = "name",
        qualification = "qualification",
        practitionerIdentifier = "practitionerIdentifier"
    ),
    patient = SyncedTaskData.Patient(
        name = "patientName",
        address = MOCK_INVOICE_ADDRESS,
        birthdate = null,
        insuranceIdentifier = "insuranceIdentifier"
    ),
    medicationRequest = MOCK_MEDICATION_REQUEST,
    whenHandedOver = de.gematik.ti.erp.app.utils.FhirTemporal.Instant(Instant.DISTANT_PAST),
    invoice = MOCK_INVOICE,
    consumed = true
)

fun mockedInvoiceChargeItemBundle(taskIds: List<String>): JsonElement {
    val entries = taskIds.map { taskId ->
        JsonObject(
            mapOf(
                "fullUrl" to JsonPrimitive("http://hapi.fhir.org/baseR4/ChargeItem/$taskId"),
                "resource" to JsonObject(
                    mapOf(
                        "resourceType" to JsonPrimitive("ChargeItem"),
                        "id" to JsonPrimitive(taskId),
                        "meta" to JsonObject(
                            mapOf(
                                "profile" to JsonArray(
                                    listOf(
                                        JsonPrimitive("https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_ChargeItem|1.0")
                                    )
                                )
                            )
                        ),
                        "status" to JsonPrimitive("billable"),
                        "extension" to JsonArray(
                            listOf(
                                JsonObject(
                                    mapOf(
                                        "url" to JsonPrimitive("https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_EX_MarkingFlag"),
                                        "extension" to JsonArray(
                                            listOf(
                                                JsonObject(
                                                    mapOf("url" to JsonPrimitive("insuranceProvider"), "valueBoolean" to JsonPrimitive(false))
                                                ),
                                                JsonObject(
                                                    mapOf("url" to JsonPrimitive("subsidy"), "valueBoolean" to JsonPrimitive(false))
                                                ),
                                                JsonObject(
                                                    mapOf("url" to JsonPrimitive("taxOffice"), "valueBoolean" to JsonPrimitive(false))
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        "enterer" to JsonObject(
                            mapOf(
                                "identifier" to JsonObject(
                                    mapOf(
                                        "system" to JsonPrimitive("https://gematik.de/fhir/sid/telematik-id"),
                                        "value" to JsonPrimitive("3-15.2.1456789123.191")
                                    )
                                )
                            )
                        ),
                        "identifier" to JsonArray(
                            listOf(
                                JsonObject(
                                    mapOf(
                                        "system" to JsonPrimitive("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId"),
                                        "value" to JsonPrimitive(taskId)
                                    )
                                ),
                                JsonObject(
                                    mapOf(
                                        "system" to JsonPrimitive("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode"),
                                        "value" to JsonPrimitive("access-code-placeholder")
                                    )
                                )
                            )
                        ),
                        "code" to JsonObject(
                            mapOf(
                                "coding" to JsonArray(
                                    listOf(
                                        JsonObject(
                                            mapOf(
                                                "code" to JsonPrimitive("not-applicable"),
                                                "system" to JsonPrimitive("http://terminology.hl7.org/CodeSystem/data-absent-reason")
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        "subject" to JsonObject(
                            mapOf(
                                "identifier" to JsonObject(
                                    mapOf(
                                        "system" to JsonPrimitive("http://fhir.de/sid/gkv/kvid-10"),
                                        "value" to JsonPrimitive("X234567890"),
                                        "assigner" to JsonObject(
                                            mapOf("display" to JsonPrimitive("Name einer privaten Krankenversicherung"))
                                        )
                                    )
                                )
                            )
                        ),
                        "enteredDate" to JsonPrimitive("2021-06-01T07:13:00+05:00"),
                        "supportingInformation" to JsonArray(
                            listOf(
                                JsonObject(
                                    mapOf(
                                        "reference" to JsonPrimitive("Bundle/0428d416-149e-48a4-977c-394887b3d85c"),
                                        "display" to JsonPrimitive("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle")
                                    )
                                ),
                                JsonObject(
                                    mapOf(
                                        "reference" to JsonPrimitive("Bundle/72bd741c-7ad8-41d8-97c3-9aabbdd0f5b4"),
                                        "display" to JsonPrimitive(
                                            "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenBundle"
                                        )
                                    )
                                ),
                                JsonObject(
                                    mapOf(
                                        "reference" to JsonPrimitive("Bundle/2fbc0103-1d1b-4be6-8ed8-6faf87bcc09b"),
                                        "display" to JsonPrimitive("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Bundle")
                                    )
                                )
                            )
                        )
                    )
                ),
                "search" to JsonObject(
                    mapOf("mode" to JsonPrimitive("match"))
                )
            )
        )
    }

    return JsonObject(
        mapOf(
            "resourceType" to JsonPrimitive("Bundle"),
            "id" to JsonPrimitive("200e3c55-b154-4335-a0ec-65addd39a3b6"),
            "meta" to JsonObject(
                mapOf("lastUpdated" to JsonPrimitive("2021-09-02T11:38:42.557+00:00"))
            ),
            "type" to JsonPrimitive("searchset"),
            "total" to JsonPrimitive(taskIds.size),
            "entry" to JsonArray(entries)
        )
    )
}
