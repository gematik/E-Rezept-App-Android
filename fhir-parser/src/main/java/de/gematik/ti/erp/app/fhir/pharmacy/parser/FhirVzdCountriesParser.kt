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

package de.gematik.ti.erp.app.fhir.pharmacy.parser

import de.gematik.ti.erp.app.fhir.BundleParser
import de.gematik.ti.erp.app.fhir.FhirCountryErpModel
import de.gematik.ti.erp.app.fhir.FhirCountryErpModelCollection
import de.gematik.ti.erp.app.fhir.common.model.original.FhirFullUrlResourceEntry.Companion.getFhirVzdResourceType
import de.gematik.ti.erp.app.fhir.constant.euprescription.FhirEuExtensions
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVZDBundle.Companion.getBundle
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdOrganization.Companion.getOrganization
import de.gematik.ti.erp.app.fhir.pharmacy.model.original.FhirVzdResourceType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

class FhirVzdCountriesParser : BundleParser {
    override fun extract(bundle: JsonElement): FhirCountryErpModelCollection? {
        return runCatching {
            val bundleElement = bundle.jsonObject
            val fhirVzdBundle = bundleElement.getBundle()

            val countries = fhirVzdBundle.entries
                .filter { it.getFhirVzdResourceType() == FhirVzdResourceType.Organization }
                .mapNotNull { entry ->
                    entry.resource
                        ?.getOrganization()
                        ?.extensions
                        ?.filter { it.url == FhirEuExtensions.COUNTRY_EXTENSION_URL }
                        ?.map { extension ->
                            FhirCountryErpModel(
                                code = extension.valueCoding?.code,
                                name = extension.valueCoding?.display
                            )
                        }
                }.flatten()

            FhirCountryErpModelCollection(countries)
        }.getOrNull()
    }
}
