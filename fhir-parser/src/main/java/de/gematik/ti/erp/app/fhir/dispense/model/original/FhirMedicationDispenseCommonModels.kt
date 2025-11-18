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

import de.gematik.ti.erp.app.fhir.common.model.original.FhirIdentifier
import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
internal data class FhirMedicationDispenseActor(
    @SerialName("actor") val actor: FhirMedicationDispenseIdentifier? = null
)

@Serializable
internal data class FhirMedicationDispenseIdentifier(
    @SerialName("identifier") val identifier: FhirIdentifier? = null,
    @SerialName("reference") val reference: String? = null
)

@Serializable
internal data class FhirMedicationDispenseSubstitution(
    @SerialName("wasSubstituted") val wasSubstituted: Boolean?
)

internal fun FhirResourceEntry.medicationDispenseResourceTypeForV14V15(): FhirMediationDispenseResourceType? {
    val type = resource.jsonObject[resourceTypePlaceholder]?.jsonPrimitive?.content
    return type?.let {
        FhirMediationDispenseResourceType.entries.find { enumItem ->
            enumItem.name.equals(it, ignoreCase = true)
        }
    }
}

// mapped exactly from fhir "resourceType" in [FhirKbvEntry]
@Serializable
enum class FhirMediationDispenseResourceType {
    MedicationDispense,
    Medication,
    Organization,
    PractitionerRole,
    Unknown
}
