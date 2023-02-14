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
import de.gematik.ti.erp.app.fhir.parser.asFhirLocalDate
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.fhir.parser.stringValue
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

enum class TaskStatus {
    Ready,
    InProgress,
    Completed,
    Other,
    Draft,
    Requested,
    Received,
    Accepted,
    Rejected,
    Canceled,
    OnHold,
    Failed;
}

fun extractTaskIds(
    bundle: JsonElement
): Pair<Int, List<String>> {
    val bundleTotal = bundle.containedArrayOrNull("entry")?.size ?: 0
    val resources = bundle
        .findAll("entry.resource")

    val taskIds = resources.mapNotNull { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        when {
            profileString.isProfileValue("https://gematik.de/fhir/StructureDefinition/ErxTask", "1.1.1") ->
                resource
                    .findAll("identifier")
                    .filterWith("system", stringValue("https://gematik.de/fhir/NamingSystem/PrescriptionID"))
                    .first()
                    .containedString("value")

            profileString.isProfileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task",
                "1.2"
            ) ->
                resource
                    .findAll("identifier")
                    .filterWith(
                        "system",
                        stringValue("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId")
                    )
                    .first()
                    .containedString("value")

            else -> null
        }
    }

    return bundleTotal to taskIds.toList()
}

fun extractTaskAndKBVBundle(
    bundle: JsonElement,
    process: (
        taskResource: JsonElement,
        bundleResource: JsonElement
    ) -> Unit
) {
    val resources = bundle
        .findAll("entry.resource")

    lateinit var task: JsonElement
    lateinit var kbvBundle: JsonElement

    resources.forEach { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        when {
            profileString.isProfileValue(
                "https://gematik.de/fhir/StructureDefinition/ErxTask",
                "1.1.1"
            ) || profileString.isProfileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task",
                "1.2"
            ) -> {
                task = resource
            }

            profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle",
                "1.0.2"
            ) || profileString.isProfileValue(
                "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle",
                "1.1.0"
            ) -> {
                kbvBundle = resource
            }
        }
    }

    process(task, kbvBundle)
}

fun extractTask(
    task: JsonElement,
    process: (
        taskId: String,
        accessCode: String?,
        lastModified: FhirTemporal.Instant,
        expiresOn: FhirTemporal.LocalDate?,
        acceptUntil: FhirTemporal.LocalDate?,
        authoredOn: FhirTemporal.Instant,
        status: TaskStatus
    ) -> Unit
) {
    val profileString = task
        .contained("meta")
        .contained("profile")
        .contained()

    when {
        profileString.isProfileValue(
            "https://gematik.de/fhir/StructureDefinition/ErxTask",
            "1.1.1"
        ) -> {
            extractTaskVersion111(task, process)
        }

        profileString.isProfileValue(
            "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task",
            "1.2"
        ) -> {
            extractTaskVersion12(task, process)
        }
    }
}

fun extractTaskVersion111(
    task: JsonElement,
    process: (
        taskId: String,
        accessCode: String?,
        lastModified: FhirTemporal.Instant,
        expiresOn: FhirTemporal.LocalDate?,
        acceptUntil: FhirTemporal.LocalDate?,
        authoredOn: FhirTemporal.Instant,
        status: TaskStatus
    ) -> Unit
) {
    val taskId = task
        .findAll("identifier")
        .filterWith("system", stringValue("https://gematik.de/fhir/NamingSystem/PrescriptionID"))
        .first()
        .containedString("value")

    val accessCode = task
        .findAll("identifier")
        .filterWith("system", stringValue("https://gematik.de/fhir/NamingSystem/AccessCode"))
        .firstOrNull()
        ?.containedString("value")

    val status = mapTaskstatus(task.containedString("status"))

    val authoredOn = requireNotNull(task.contained("authoredOn").jsonPrimitive.asFhirInstant()) {
        "Couldn't parse `authoredOn`"
    }
    val lastModified = requireNotNull(task.contained("lastModified").jsonPrimitive.asFhirInstant()) {
        "Couldn't parse `lastModified`"
    }

    val expiresOn = task
        .findAll("extension")
        .filterWith("url", stringValue("https://gematik.de/fhir/StructureDefinition/ExpiryDate"))
        .first()
        .contained("valueDate")
        .jsonPrimitive.asFhirLocalDate()

    val acceptUntil = task
        .findAll("extension")
        .filterWith("url", stringValue("https://gematik.de/fhir/StructureDefinition/AcceptDate"))
        .first()
        .contained("valueDate")
        .jsonPrimitive.asFhirLocalDate()

    process(
        taskId,
        accessCode,
        lastModified,
        expiresOn,
        acceptUntil,
        authoredOn,
        status
    )
}

fun extractTaskVersion12(
    task: JsonElement,
    process: (
        taskId: String,
        accessCode: String?,
        lastModified: FhirTemporal.Instant,
        expiresOn: FhirTemporal.LocalDate?,
        acceptUntil: FhirTemporal.LocalDate?,
        authoredOn: FhirTemporal.Instant,
        status: TaskStatus
    ) -> Unit
) {
    val taskId = task
        .findAll("identifier")
        .filterWith("system", stringValue("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId"))
        .first()
        .containedString("value")

    val accessCode = task
        .findAll("identifier")
        .filterWith("system", stringValue("https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_AccessCode"))
        .firstOrNull()
        ?.containedString("value")

    val status = mapTaskstatus(task.containedString("status"))

    val authoredOn = requireNotNull(task.contained("authoredOn").jsonPrimitive.asFhirInstant()) {
        "Couldn't parse `authoredOn`"
    }
    val lastModified = requireNotNull(task.contained("lastModified").jsonPrimitive.asFhirInstant()) {
        "Couldn't parse `lastModified`"
    }

    val expiresOn = task
        .findAll("extension")
        .filterWith("url", stringValue("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_ExpiryDate"))
        .first()
        .contained("valueDate")
        .jsonPrimitive.asFhirLocalDate()

    val acceptUntil = task
        .findAll("extension")
        .filterWith("url", stringValue("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_AcceptDate"))
        .first()
        .contained("valueDate")
        .jsonPrimitive.asFhirLocalDate()

    process(
        taskId,
        accessCode,
        lastModified,
        expiresOn,
        acceptUntil,
        authoredOn,
        status
    )
}

private fun mapTaskstatus(status: String): TaskStatus = when (status) {
    "ready" -> TaskStatus.Ready
    "in-progress" -> TaskStatus.InProgress
    "completed" -> TaskStatus.Completed
    "canceled" -> TaskStatus.Canceled
    "accepted" -> TaskStatus.Accepted
    "draft" -> TaskStatus.Draft
    "failed" -> TaskStatus.Failed
    "on-hold" -> TaskStatus.OnHold
    "requested" -> TaskStatus.Requested
    "received" -> TaskStatus.Received
    "rejected" -> TaskStatus.Rejected
    else -> TaskStatus.Other
}
