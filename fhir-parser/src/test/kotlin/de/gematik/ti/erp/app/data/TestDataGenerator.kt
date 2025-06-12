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

package de.gematik.ti.erp.app.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

class TestDataGenerator {

    fun createMedicationDispenseBundleFromResources(resources: List<JsonElement>): JsonObject {
        val entries = resources.map { resource ->
            val resourceObj = resource.jsonObject
            val resourceType = resourceObj["resourceType"]?.jsonPrimitive?.content
                ?: error("Missing 'resourceType' in resource")
            val id = resourceObj["id"]?.jsonPrimitive?.content
                ?: error("Missing 'id' in resource")

            val fullUrl = when (resourceType) {
                "Medication" -> "urn:uuid:$id"
                else -> "https://erp-test.zentral.erp.splitdns.ti-dienste.de/$resourceType/$id"
            }

            buildJsonObject {
                put("fullUrl", JsonPrimitive(fullUrl))
                put("resource", resource)
                put(
                    "search",
                    buildJsonObject {
                        put("mode", JsonPrimitive(if (resourceType == "Medication") "include" else "match"))
                    }
                )
            }
        }

        return buildJsonObject {
            put("id", JsonPrimitive(UUID.randomUUID().toString()))
            put("type", JsonPrimitive("searchset"))
            put("timestamp", JsonPrimitive(ZonedDateTime.now().toOffsetDateTime().toString()))
            put("resourceType", JsonPrimitive("Bundle"))
            put("total", JsonPrimitive(resources.size))
            put("entry", JsonArray(entries))
        }
    }

    fun createDispenseBundleFromMedications(
        medications: List<JsonElement>,
        dispenseDate: LocalDate = LocalDate.parse("2025-04-04"),
        patientKvnr: String = "X110519788",
        performerTelematikId: String = "3-01.2.2023001.16.101"
    ): JsonObject {
        val dispenseEntries = medications.mapIndexed { index, medication ->
            val medicationId = medication.jsonObject["id"]?.jsonPrimitive?.content
                ?: error("Medication is missing 'id'")

            val dispenseId = "200.000.000.205.090.${70 + index}"

            // MedicationDispense resource
            val dispenseResource = buildJsonObject {
                put("resourceType", JsonPrimitive("MedicationDispense"))
                put("id", JsonPrimitive(dispenseId))
                put(
                    "meta",
                    buildJsonObject {
                        put(
                            "profile",
                            JsonArray(
                                listOf(
                                    JsonPrimitive("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense|1.4")
                                )
                            )
                        )
                    }
                )
                put(
                    "identifier",
                    JsonArray(
                        listOf(
                            buildJsonObject {
                                put("system", JsonPrimitive("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId"))
                                put("value", JsonPrimitive(dispenseId))
                            }
                        )
                    )
                )
                put("status", JsonPrimitive("completed"))
                put(
                    "medicationReference",
                    buildJsonObject {
                        put("reference", JsonPrimitive("urn:uuid:$medicationId"))
                    }
                )
                put(
                    "subject",
                    buildJsonObject {
                        put(
                            "identifier",
                            buildJsonObject {
                                put("system", JsonPrimitive("http://fhir.de/sid/gkv/kvid-10"))
                                put("value", JsonPrimitive(patientKvnr))
                            }
                        )
                    }
                )
                put(
                    "performer",
                    JsonArray(
                        listOf(
                            buildJsonObject {
                                put(
                                    "actor",
                                    buildJsonObject {
                                        put(
                                            "identifier",
                                            buildJsonObject {
                                                put("system", JsonPrimitive("https://gematik.de/fhir/sid/telematik-id"))
                                                put("value", JsonPrimitive(performerTelematikId))
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    )
                )
                put("whenPrepared", JsonPrimitive(dispenseDate.toString()))
                put("whenHandedOver", JsonPrimitive(dispenseDate.toString()))
                put(
                    "substitution",
                    buildJsonObject {
                        put("wasSubstituted", JsonPrimitive(false))
                    }
                )
            }

            listOf(
                // Entry for MedicationDispense
                buildJsonObject {
                    put("fullUrl", JsonPrimitive("https://example.com/MedicationDispense/$dispenseId"))
                    put("resource", dispenseResource)
                    put(
                        "search",
                        buildJsonObject {
                            put("mode", JsonPrimitive("match"))
                        }
                    )
                },
                // Entry for Medication
                buildJsonObject {
                    put("fullUrl", JsonPrimitive("urn:uuid:$medicationId"))
                    put("resource", medication)
                    put(
                        "search",
                        buildJsonObject {
                            put("mode", JsonPrimitive("include"))
                        }
                    )
                }
            )
        }.flatten()

        return buildJsonObject {
            put("id", JsonPrimitive(UUID.randomUUID().toString()))
            put("type", JsonPrimitive("searchset"))
            put("timestamp", JsonPrimitive(ZonedDateTime.now().toOffsetDateTime().toString()))
            put("resourceType", JsonPrimitive("Bundle"))
            put("total", JsonPrimitive(dispenseEntries.size))
            put("entry", JsonArray(dispenseEntries))
        }
    }
}
