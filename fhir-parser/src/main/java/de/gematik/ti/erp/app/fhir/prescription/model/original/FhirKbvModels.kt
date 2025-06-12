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

package de.gematik.ti.erp.app.fhir.prescription.model.original

import de.gematik.ti.erp.app.fhir.common.model.original.FhirResourceEntry
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun FhirResourceEntry.kbvResourceType(): FhirKbvResourceType? {
    val type = resource.jsonObject[resourceTypePlaceholder]?.jsonPrimitive?.content
    return type?.let {
        FhirKbvResourceType.entries.find { enumItem ->
            enumItem.name.equals(it, ignoreCase = true)
        }
    }
}

// mapped exactly from fhir "resourceType" in [FhirKbvEntry]
enum class FhirKbvResourceType {
    Composition,
    MedicationRequest,
    Medication,
    Patient,
    PractitionerRole,
    Practitioner,
    Organization,
    Coverage,
    DeviceRequest;
}
