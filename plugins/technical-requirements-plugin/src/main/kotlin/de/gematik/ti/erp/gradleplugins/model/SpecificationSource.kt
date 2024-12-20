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

@file: Suppress("ktlint:max-line-length", "MaxLineLength")

package de.gematik.ti.erp.gradleplugins.model

/**
 * Enum class for AFO specifications
 */
enum class SpecificationSource(
    val spec: String,
    val url: String,
    val isFromGemSpec: Boolean = true
) {
    GEM_SPEC_KRYPT(
        spec = "gemSpec_Krypt",
        url = "https://gemspec.gematik.de/docs/gemSpec/gemSpec_Krypt/latest/index.html"
    ),
    GEM_SPEC_IDP_FRONTEND(
        spec = "gemSpec_IDP_Frontend",
        url = "https://gemspec.gematik.de/docs/gemSpec/gemSpec_IDP_Frontend/latest/index.html"
    ),
    GEM_SPEC_ERP_FDV(
        spec = "gemSpec_eRp_FdV",
        url = "https://gemspec.gematik.de/docs/gemSpec/gemSpec_eRp_FdV/latest/index.html"
    ),
    GEM_SPEC_IDP_SEK(
        spec = "gemSpec_IDP_Sek",
        url = "https://gemspec.gematik.de/docs/gemSpec/gemSpec_IDP_Sek/latest/index.html"
    ),
    GEM_SPEC_IDP_DIENSTE(
        spec = "gemSpec_IDP_Dienst",
        url = "https://gemspec.gematik.de/docs/gemSpec/gemSpec_IDP_Dienst/latest/index.html"
    ),
    GEM_SPEC_ERP_APOVZD(
        spec = "gemSpec_eRp_APOVZD",
        url = "https://gemspec.gematik.de/docs/gemSpec/gemSpec_eRp_APOVZD/latest/index.html"
    ),
    GEM_SPEC_FD_ERP(
        spec = "gemSpec_FD_eRp",
        url = "https://gemspec.gematik.de/docs/gemSpec/gemSpec_FD_eRp/latest/index.html"
    ),

    @Suppress("ktlint:max-line-length", "MaxLineLength")
    BSI_ERP_EPA(
        spec = "BSI-eRp-ePA",
        url = "https://gitlab.prod.ccs.gematik.solutions/api/v4/projects/833/repository/files/security_review%2Frequirements%2F2024.html/raw?ref=main",
        isFromGemSpec = false
    );

    companion object {
        fun fromSpec(spec: String): SpecificationSource? = values().find { it.spec == spec.trim() }
    }
}
