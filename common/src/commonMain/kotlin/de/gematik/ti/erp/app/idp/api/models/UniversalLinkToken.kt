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

package de.gematik.ti.erp.app.idp.api.models

import de.gematik.ti.erp.app.idp.api.models.UniversalLinkToken.TokenIdentifier.Code
import de.gematik.ti.erp.app.idp.api.models.UniversalLinkToken.TokenIdentifier.Kk_app_redirect_uri
import de.gematik.ti.erp.app.idp.api.models.UniversalLinkToken.TokenIdentifier.State
import de.gematik.ti.erp.app.idp.extension.getQueryPairs
import de.gematik.ti.erp.app.utils.letNotNull
import io.github.aakira.napier.Napier
import java.net.URI

/**
 * @param code
 * The code to that obtained from the query parameter
 * @param state
 * The state obtained from the query parameter
 * @param redirectUri
 * These kk_app_redirect_uri obtained from the query parameter and
 * "isGid" is constructed based on the presence and absence of the [redirectUri]
 * This token is generated from the answer from the health insurance app for the universal link sent to it.
 * The presence and absence of the [redirectUri] decides if it is a gID or fast-track process
 */
data class UniversalLinkToken(
    val code: String,
    val state: String,
    val redirectUri: String? = null // this is present only in non-gID state
) {

    val isGid = redirectUri == null

    @Suppress("EnumEntryNameCase", "EnumEntry")
    enum class TokenIdentifier {
        Code,
        State,
        Kk_app_redirect_uri; // ktlint-disable enum-entry-name-case
    }

    companion object {
        fun URI.toUniversalLinkToken(): UniversalLinkToken? {
            val entries = getQueryPairs()

            return ((entries findIdentifierFor Code) to (entries findIdentifierFor State))
                .letNotNull { code, state ->
                    UniversalLinkToken(
                        code = code,
                        state = state,
                        redirectUri = (entries findIdentifierFor Kk_app_redirect_uri)
                            .also {
                                // this needs to be null for gid process.
                                Napier.i { "redirect Uri presence = ${it.isNullOrBlank()}" }
                            }
                    )
                } ?: run {
                Napier.e { "mandatory parameters ${Code.name.lowercase()} and ${State.name.lowercase()} missing" }
                null
            }
        }

        fun URI.requireUniversalLinkToken(): UniversalLinkToken =
            requireNotNull(this.toUniversalLinkToken()) {
                "missing UniversalLinkToken mandatory parameters"
            }

        private infix fun List<Pair<String, String>?>.findIdentifierFor(identifier: TokenIdentifier): String? =
            find { it?.first == identifier.name.lowercase() && it.second.isNotBlank() }?.second
    }
}
