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
import de.gematik.ti.erp.app.fhir.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findAccessCode
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier.Companion.findPrescriptionId
import de.gematik.ti.erp.app.fhir.common.model.original.FhirTaskResource.Companion.getResourceIdentifiers
import de.gematik.ti.erp.app.fhir.error.fhirError
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTaskExtensionValues.Companion.getExtensionValues
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTaskLifeCycleMetadata.Companion.getAuthoredOn
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTaskLifeCycleMetadata.Companion.getLastModified
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirTaskStatus.Companion.getStatus
import de.gematik.ti.erp.app.task.model.TaskStatus
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.JsonElement

/**
 * **FHIR Task Metadata Parser**
 *
 * This class is responsible for extracting **metadata** from a FHIR **Task** resource.
 * It parses key details such as **task ID, status, timestamps, and prescription lifecycle dates**.
 *
 * Implements [BundleParser].
 */
@Requirement(
    "O.Source_2#9",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = """
        The parser securely extracts lifecycle metadata from FHIR `Task` resources by:
            • Decoding `authoredOn`, `lastModified`, `expiresOn`, and `acceptUntil` fields via structured extension processing.
            • Explicitly failing when required fields are missing, ensuring no untrusted or incomplete lifecycle data is processed.
            • Validating the presence of a task identifier [`taskId`] and safely assigning the access code when available.
            • Logging structured parsing errors to aid in traceability and debugging.       
    """,
    codeLines = 40
)
class TaskMetadataParser : BundleParser {

    /**
     * **Extracts Metadata from a FHIR Task Resource**
     *
     * This method decodes a **FHIR Task JSON element** and extracts **essential metadata**.
     * It retrieves key properties like:
     * - **Task ID** (prescription identifier)
     * - **Access Code**
     * - **Task Status**
     * - **Authored & Last Modified timestamps**
     * - **Expiration & Acceptance Dates**
     * - **Last Medication Dispense Timestamp**
     *
     * @param bundle A [JsonElement] representing a **FHIR Task resource**.
     * @return A [FhirTaskMetaDataErpModel] object containing the extracted metadata, or `null` if parsing fails.
     */
    override fun extract(bundle: JsonElement): FhirTaskMetaDataErpModel? {
        return runCatching {
            // Deserialize the task resource using Kotlinx Serialization
            val taskResource = bundle.getResourceIdentifiers()

            val taskId = taskResource.identifiers.findPrescriptionId()
                ?: return null.also { Napier.e(tag = "fhir-parser") { "Couldn't parse `taskId` from ${taskResource.identifiers}" } }

            val extensionValues = bundle.getExtensionValues()

            FhirTaskMetaDataErpModel(
                taskId = taskId,
                accessCode = taskResource.identifiers.findAccessCode() ?: "", // 169 and 209 direct assignments have no access code
                status = TaskStatus.fromString(bundle.getStatus()),
                authoredOn = bundle.getAuthoredOn() ?: fhirError("Couldn't parse `authoredOn`"),
                lastModified = bundle.getLastModified() ?: fhirError("Couldn't parse `lastModified`"),
                expiresOn = extensionValues.expiryDate(),
                acceptUntil = extensionValues.acceptedDate(),
                lastMedicationDispense = extensionValues.lastMedicationDispense(),
                isEuRedeemableByProperties = extensionValues.isEuRedeemableByProperties,
                isEuRedeemableByPatientAuthorization = extensionValues.isEuRedeemableByPatientAuthorization
            )
        }.onFailure {
            Napier.e(tag = "fhir-parser") { "FHIR Parsing Error : parsing 'task-bundle' from resource: $bundle ${it.message}" }
        }.getOrNull()
    }
}
