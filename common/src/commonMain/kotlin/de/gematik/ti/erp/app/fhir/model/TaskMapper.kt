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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedArrayOrNull
import de.gematik.ti.erp.app.fhir.parser.containedOrNull
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.fhir.parser.containedStringOrNull
import de.gematik.ti.erp.app.fhir.parser.filterWith
import de.gematik.ti.erp.app.fhir.parser.findAll
import de.gematik.ti.erp.app.fhir.parser.isProfileValue
import de.gematik.ti.erp.app.fhir.parser.stringValue
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.asFhirInstant
import de.gematik.ti.erp.app.utils.asFhirLocalDate
import de.gematik.ti.erp.app.utils.toFhirTemporal
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

data class TaskData(
    val taskId: String,
    val status: TaskStatus,
    val lastModified: FhirTemporal?
)

@Deprecated(
    "Use de.gematik.ti.erp.app.fhir.prescription.parser.TaskBundleParser.kt",
    replaceWith = ReplaceWith("de.gematik.ti.erp.app.fhir.prescription.parser.TaskEntryParser.kt"),
    level = DeprecationLevel.WARNING
)
@Requirement(
    "O.Source_2#5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Sanitization is also done for all FHIR mapping."
)
fun extractActualTaskData(
    bundle: JsonElement
): Pair<Int, List<TaskData>> {
    val bundleTotal = bundle.containedArrayOrNull("entry")?.size ?: 0
    val resources = bundle
        .findAll("entry.resource")

    val tasks = resources.mapNotNull { resource ->
        val profileString = resource
            .contained("meta")
            .contained("profile")
            .contained()

        val status = mapTaskStatus(resource.containedString("status"))
        val lastModified = resource.contained("lastModified").jsonPrimitive.toFhirTemporal()
        val taskId = when {
            profileString.isProfileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task",
                "1.2",
                "1.3",
                "1.4"
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

        taskId?.let { TaskData(it, status, lastModified) }
    }

    return bundleTotal to tasks.toList()
}

@Deprecated(
    "Use de.gematik.ti.erp.app.fhir.prescription.parser.TaskBundleParser.kt",
    replaceWith = ReplaceWith("de.gematik.ti.erp.app.fhir.prescription.parser.TaskBundleSeperationParser.kt"),
    level = DeprecationLevel.WARNING
)
fun extractTaskAndKBVBundle(
    bundle: JsonElement,
    process: (
        taskResource: JsonElement,
        bundleResource: JsonElement
    ) -> Unit
) {
    Napier.e { bundle.toString() }
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
            // TODO: remove Version 1.2 and 1.3 for GEM_ERP_PR_Task after 15.Jul.2025
            profileString.isProfileValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task",
                "1.2",
                "1.3",
                "1.4"
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

@Deprecated(
    "Use de.gematik.ti.erp.app.fhir.prescription.parser.TaskBundleParser.kt",
    replaceWith = ReplaceWith("de.gematik.ti.erp.app.fhir.prescription.parser.TaskBundleParser.kt"),
    level = DeprecationLevel.WARNING
)
fun extractTask(
    task: JsonElement,
    process: (
        taskId: String,
        accessCode: String,
        lastModified: FhirTemporal.Instant,
        expiresOn: FhirTemporal.LocalDate?,
        acceptUntil: FhirTemporal.LocalDate?,
        authoredOn: FhirTemporal.Instant,
        status: TaskStatus,
        lastMedicationDispense: FhirTemporal.Instant?
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
        ?.containedStringOrNull("value") ?: "" // 169 and 209 direct assignments have not an access code

    val status = mapTaskStatus(task.containedString("status"))

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

    val lastMedicationDispense = task
        .findAll("extension")
        .filterWith(
            "url",
            stringValue(
                "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_LastMedicationDispense"
            )
        )
        .firstOrNull()
        ?.containedOrNull("valueInstant")
        ?.jsonPrimitive?.asFhirInstant()

    process(
        taskId,
        accessCode,
        lastModified,
        expiresOn,
        acceptUntil,
        authoredOn,
        status,
        lastMedicationDispense
    )
}

private fun mapTaskStatus(status: String): TaskStatus = when (status) {
    "ready" -> TaskStatus.Ready
    "in-progress" -> TaskStatus.InProgress
    "completed" -> TaskStatus.Completed
    "cancelled" -> TaskStatus.Canceled
    "accepted" -> TaskStatus.Accepted
    "draft" -> TaskStatus.Draft
    "failed" -> TaskStatus.Failed
    "on-hold" -> TaskStatus.OnHold
    "requested" -> TaskStatus.Requested
    "received" -> TaskStatus.Received
    "rejected" -> TaskStatus.Rejected
    else -> TaskStatus.Other
}
