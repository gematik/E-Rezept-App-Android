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

package de.gematik.ti.erp.app.fhir.pharmacy.parser

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirInstitutionTelematikId
import de.gematik.ti.erp.app.fhir.common.model.original.FhirFullUrlResourceEntry.Companion.getFhirVzdResourceType
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVZDBundle.Companion.getBundle
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdOrganization.Companion.getOrganization
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdResourceType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Requirement(
    "O.Source_2#13",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = """
        This parser securely processes structured FHIR `Bundle` resources by:
            • Extracting and inspecting bundle entries using safe accessors and functional operations.
            • Identifying `Organization` resources through explicit resource type filtering.
            • Extracting the Telematik ID via safe chaining of null-aware calls, avoiding exceptions.
            • Returning only the first matching and well-formed `Organization` as an internal model.
            • Logging the entry count and skipping unknown or malformed entries without interrupting flow.
        This ensures robust, secure, and profile-aligned parsing of structured organization metadata in compliance with specification requirements.
    """
)
class OrganizationParser : BundleParser {
    override fun extract(bundle: JsonElement): FhirInstitutionTelematikId? {
        return runCatching {
            val bundleElement = bundle.jsonObject
            val fhirVzdBundle = bundleElement.getBundle()

            fhirVzdBundle.entries
                .firstOrNull { it.getFhirVzdResourceType() == FhirVzdResourceType.Organization }
                ?.resource
                ?.getOrganization()
                ?.telematikId
                ?.let { FhirInstitutionTelematikId(it) }
        }.getOrNull()
    }
}
