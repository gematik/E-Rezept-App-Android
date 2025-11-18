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

package de.gematik.ti.erp.app.pharmacy.usecase.model

import de.gematik.ti.erp.app.Requirement

@Requirement(
    "A_20285#9",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The [TextFilter]filter is to filter by the text provided by the user."
)
data class TextFilter(
    val value: List<String> = emptyList()
) {
    companion object {

        // sanitization due to fhir-vzd rules:
        // https://github.com/gematik/api-vzd/blob/main/docs/FHIR_VZD_HOWTO_Search.adoc#fhir-vzd-search-endpoint-payload-type-attribute-display-text-based-search
        private fun String.sanitize(): String = replace(Regex("[.']"), "") // removing ., "" from the search string

        // adding quotes to the search string, to force the backend to search for the exact string
        private fun String.addQuotesSearch(): String? {
            return if (isEmpty()) null else "$this" // please do not remove the quotes, it is required for the search
        }

        fun String.toTextFilter() =
            TextFilter(
                value = split(" ").filter { it.isNotEmpty() }
            )

        fun TextFilter.toSanitizedSearchText(): String? = value.joinToString().sanitize().addQuotesSearch()
    }
}
