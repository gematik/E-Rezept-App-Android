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

package de.gematik.ti.erp.app.fhir.dispense.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension
import de.gematik.ti.erp.app.fhir.common.model.original.FhirExtension.Companion.findExtensionByUrl
import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.constant.FhirConstants.PZN_IDENTIFIER
import de.gematik.ti.erp.app.utils.sanitize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class FhirMedicationDispenseReference {

    private val DECLINE_EXTENSION_URL = "http://hl7.org/fhir/StructureDefinition/data-absent-reason"

    val medicationReferenceId: String?
        get() = when (this) {
            is MedicationReferenceByReference -> {
                when {
                    reference.startsWith("Medication/") -> reference.removePrefix("Medication/")
                    reference.startsWith("urn:uuid:") -> reference.sanitize()
                    else -> reference // fallback as-is
                }
            }

            is MedicationReferenceByIdentifier -> this.identifier.value
            else -> null
        }

    val declineCode: String?
        get() = when (this) {
            is MedicationReferenceByExtension -> {
                extension.findExtensionByUrl(DECLINE_EXTENSION_URL)?.valueCode
            }

            else -> null
        }

    val pzn: String?
        get() = when (this) {
            // http://fhir.de/CodeSystem/ifa/pzn is the system url for the value
            is MedicationReferenceByIdentifier -> if (identifier.system == PZN_IDENTIFIER) identifier.value else null
            else -> null
        }

    val displayInfo: String?
        get() = when (this) {
            // http://fhir.de/CodeSystem/ifa/pzn is the system url for the value
            is MedicationReferenceByIdentifier -> display
            else -> null
        }
}

@Serializable
@SerialName("ByIdentifier")
internal data class MedicationReferenceByIdentifier(
    val identifier: FhirIdentifier,
    val display: String? = null
) : FhirMedicationDispenseReference()

@Serializable
@SerialName("ByExtension")
internal data class MedicationReferenceByExtension(
    val extension: List<FhirExtension>
) : FhirMedicationDispenseReference()

@Serializable
@SerialName("ByReference")
internal data class MedicationReferenceByReference(
    val reference: String
) : FhirMedicationDispenseReference()
