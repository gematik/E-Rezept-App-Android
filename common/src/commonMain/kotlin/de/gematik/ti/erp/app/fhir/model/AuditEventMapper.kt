/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.FhirTemporal
import de.gematik.ti.erp.app.fhir.parser.asFhirInstant
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.fhir.parser.stringValue
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

fun extractAuditEvents(
    bundle: JsonElement,
    save: (id: String, taskId: String?, description: String, timestamp: FhirTemporal.Instant) -> Unit
): Int {
    val bundleTotal = bundle.containedArrayOrNull("entry")?.size ?: 0

    val resources = bundle
        .findAll(listOf("entry", "resource"))

    resources.forEach { resource ->

        val profileString = resource.contained("meta").contained("profile").contained()

        if (profileString.isProfileValue(
                "https://gematik.de/fhir/StructureDefinition/ErxAuditEvent",
                "1.1.1"
            ) ||
            profileString.isProfileValue(
                    "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_AuditEvent",
                    "1.2"
                )
        ) {
            extractAuditEvent(resource, save)
        }
    }

    return bundleTotal
}

fun extractAuditEvent(
    resource: JsonElement,
    save: (id: String, taskId: String?, description: String, timestamp: FhirTemporal.Instant) -> Unit
) {
    val id = resource.containedString("id")
    val text = resource.contained("text").containedString("div")
    val taskId = resource
        .findAll(listOf("entity", "what", "identifier"))
        .filterWith("system", stringValue("https://gematik.de/fhir/NamingSystem/PrescriptionID"))
        .firstOrNull()
        ?.containedString("value")

    val timestamp = requireNotNull(resource.contained("recorded").jsonPrimitive.asFhirInstant()) {
        "Audit event field `recorded` missing"
    }

    val description = text.removeSurrounding("<div xmlns=\"http://www.w3.org/1999/xhtml\">", "</div>")

    save(id, taskId, description, timestamp)
}
