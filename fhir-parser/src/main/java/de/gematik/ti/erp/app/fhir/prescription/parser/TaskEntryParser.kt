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

package de.gematik.ti.erp.app.fhir.prescription.parser

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirTaskEntryParserResultErpModel
import de.gematik.ti.erp.app.fhir.common.model.erp.support.FhirTaskEntryDataErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundle.Companion.getBundleEntries
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundle.Companion.getBundleLinks
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleLink.Companion.firstPage
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleLink.Companion.nextPage
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleLink.Companion.previousPage
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleLink.Companion.selfPage
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleMetaProfile.Companion.containsExpectedProfileVersionForTaskEntryPhase
import de.gematik.ti.erp.app.fhir.common.model.original.FhirBundleTaskData.Companion.getTaskData
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findPrescriptionId
import de.gematik.ti.erp.app.fhir.common.model.original.FhirTaskResource.Companion.getResourceIdentifiers
import de.gematik.ti.erp.app.fhir.model.TaskStatus
import de.gematik.ti.erp.app.utils.toFhirTemporal
import kotlinx.serialization.json.JsonElement

/**
 * Extracts FHIR task entries from a given JSON `bundle` and processes them into a structured model.
 *
 * This function:
 * - Retrieves the list of `BundleEntryResource` objects from the bundle.
 * - Determines the total number of entries in the bundle.
 * - Filters and processes entries that contain a valid FHIR profile version.
 * - Extracts `TaskEntryData` from valid task resources, including `taskId`, `status`, and `lastModified`.
 *
 * @param bundle The `JsonElement` representing the FHIR bundle.
 * @return A `TaskEntryParserResult` containing the total number of bundle entries and a list of extracted `TaskEntryData`.
 */
@Requirement(
    "O.Source_2#7",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = """
       The parser ensures structured FHIR input is sanitized and validated and safely handled by:
            • Using the `getBundleEntries` which only passes it, if it has resources. [sanitization]
            • Using `resource.getTaskData` which picks the `status`, and `lastModified` when the structure allows it. [sanitization]
            • Obtaining the `taskId` when the required identifier is present and matching it with the `PRESCRIPTION_ID_SYSTEM` when it is present. [sanitization]          
            • Accepting only resources that declare a known Task profile version via `containsExpectedProfileVersionForTaskEntryPhase`. [validation]           
            • Preventing mapping of malformed, incomplete, or untrusted JSON elements by rejecting entries that fail validation at any step. 
            This satisfies the requirement to escape, reject, or sanitize structured data before internal processing to protect against malformed or malicious FHIR content. 
    """
)
class TaskEntryParser : BundleParser {
    override fun extract(bundle: JsonElement): FhirTaskEntryParserResultErpModel {
        val resources = bundle.getBundleEntries()
        val pagingLinks = bundle.getBundleLinks()
        val bundleTotal = resources.size

        val taskEntries = resources.mapNotNull { entry ->
            entry.resource.takeIf { it.containsExpectedProfileVersionForTaskEntryPhase() }?.let { resource ->
                resource.getTaskData().let { taskData ->
                    val taskId = resource.getResourceIdentifiers().identifiers.findPrescriptionId()
                    taskId?.let {
                        FhirTaskEntryDataErpModel(
                            id = it,
                            status = TaskStatus.fromString(taskData.status),
                            lastModified = taskData.lastModified?.toFhirTemporal()
                        )
                    }
                }
            }
        }

        return FhirTaskEntryParserResultErpModel(
            bundleTotal = bundleTotal,
            taskEntries = taskEntries,
            firstPageUrl = pagingLinks.firstPage(),
            previousPageUrl = pagingLinks.previousPage(),
            nextPageUrl = pagingLinks.nextPage(),
            selfPageUrl = pagingLinks.selfPage()
        )
    }
}
