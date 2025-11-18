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

package de.gematik.ti.erp.app.fhir.common.model.original

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://gemspec.gematik.de/docs/gemSpec/gemSpec_FD_eRp/gemSpec_FD_eRp_V2.3.0/#A_24443-01
@Serializable
data class FhirBundleLink(
    @SerialName("relation") val linkRelation: String?,
    @SerialName("url") val linkUrl: String?
) {
    companion object Companion {
        internal const val RELATION_NEXT = "next"
        internal const val RELATION_PREVIOUS = "previous"
        internal const val RELATION_FIRST = "first"
        internal const val RELATION_SELF = "self"

        // Convenience methods to extract links from a list of FhirPaging objects
        internal fun List<FhirBundleLink>.firstPage(): String? = find { it.linkRelation == RELATION_FIRST }?.linkUrl
        internal fun List<FhirBundleLink>.previousPage(): String? = find { it.linkRelation == RELATION_PREVIOUS }?.linkUrl
        internal fun List<FhirBundleLink>.selfPage(): String? = find { it.linkRelation == RELATION_SELF }?.linkUrl
        internal fun List<FhirBundleLink>.nextPage(): String? = find { it.linkRelation == RELATION_NEXT }?.linkUrl
    }
}

enum class BundleLinkRelation(val value: String) {
    NEXT(FhirBundleLink.RELATION_NEXT),
    PREVIOUS(FhirBundleLink.RELATION_PREVIOUS),
    FIRST(FhirBundleLink.RELATION_FIRST),
    SELF(FhirBundleLink.RELATION_SELF)
    ;

    companion object Companion {
        fun fromValue(value: String): BundleLinkRelation? = BundleLinkRelation.entries.find { it.value == value }
    }
}
