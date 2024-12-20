/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.protocol.repository

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.model.mapCatching
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import de.gematik.ti.erp.app.utils.asFhirInstant
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

class DefaultAuditEventsRepository(
    private val remoteDataSource: AuditEventRemoteDataSource
) : AuditEventsRepository {

    @Requirement(
        "A_19177#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "The audit events are downloaded and extracted."
    )
    override suspend fun downloadAuditEvents(
        profileId: ProfileIdentifier,
        count: Int?,
        offset: Int?
    ) =
        remoteDataSource.getAuditEvents(
            profileId = profileId,
            count = count,
            offset = offset
        ).map {
            extractAuditEvents(
                bundle = it,
                onError = { element, cause ->
                    Napier.e(cause) {
                        element.toString()
                    }
                }
            )
        }

    private fun extractAuditEvents(
        bundle: JsonElement,
        onError: (JsonElement, Exception) -> Unit = { _, _ -> }
    ): AuditEventData.AuditEventMappingResult {
        val bundleTotal = bundle.containedArrayOrNull("entry")?.size ?: 0
        val bundleId = bundle.containedString("id")
        val resources = bundle
            .findAll(listOf("entry", "resource"))

        val auditEvents = resources.mapCatching(onError) { resource ->
            val id = resource.containedString("id")
            val text = resource.contained("text").containedString("div")
            val taskId = resource
                .findAll(listOf("entity", "what", "identifier"))
                .filterWith(
                    "system",
                    stringValue("https://gematik.de/fhir/NamingSystem/PrescriptionID")
                )
                .firstOrNull()
                ?.containedString("value")
                ?: resource
                    .findAll(listOf("entity", "what", "identifier"))
                    .filterWith(
                        "system",
                        stringValue("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId")
                    )
                    .firstOrNull()
                    ?.containedString("value")

            val timestamp = requireNotNull(resource.contained("recorded").jsonPrimitive.asFhirInstant()) {
                "Audit event field `recorded` missing"
            }

            val description = text.removeSurrounding(
                "<div xmlns=\"http://www.w3.org/1999/xhtml\">",
                "</div>"
            )

            AuditEventData.AuditEvent(
                auditId = id,
                taskId = taskId,
                description = description,
                timestamp = timestamp.value
            )
        }

        return AuditEventData.AuditEventMappingResult(
            auditEvents = auditEvents.toList(),
            bundleId = bundleId,
            bundleResultCount = bundleTotal
        )
    }
}
