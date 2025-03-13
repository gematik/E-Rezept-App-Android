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

package de.gematik.ti.erp.app.fhir.prescription.parser

import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.model.TaskStatus
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskEntryDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.erp.FhirTaskEntryParserResultErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirBundle.Companion.getBundleEntries
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirBundleMetaProfile.Companion.containsExpectedProfileVersionForTaskEntryPhase
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirBundleTaskData.Companion.getTaskData
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirIdentifier.Companion.findPrescriptionId
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTaskResource.Companion.getResourceIdentifiers
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
class TaskEntryParser : BundleParser {
    override fun extract(bundle: JsonElement): FhirTaskEntryParserResultErpModel? {
        val resources = bundle.getBundleEntries()
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

        return FhirTaskEntryParserResultErpModel(bundleTotal, taskEntries)
    }
}
