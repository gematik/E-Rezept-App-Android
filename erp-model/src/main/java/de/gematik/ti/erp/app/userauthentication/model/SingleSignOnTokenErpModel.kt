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

package de.gematik.ti.erp.app.userauthentication.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jose4j.jwx.JsonWebStructure
import kotlin.time.Duration.Companion.hours

@Serializable
data class SingleSignOnTokenErpModel(
    val token: String,
    val expiresOn: Instant = extractExpirationTimestamp(token),
    val validOn: Instant = extractValidOnTimestamp(token)
) {
    fun isValid(instant: Instant = Clock.System.now()) =
        instant in validOn..expiresOn
}

private fun extractExpirationTimestamp(ssoToken: String): Instant =
    Instant.fromEpochSeconds(
        JsonWebStructure
            .fromCompactSerialization(ssoToken)
            .headers
            .getLongHeaderValue("exp")
    )

private fun extractValidOnTimestamp(ssoToken: String): Instant =
    extractExpirationTimestamp(ssoToken) - 24.hours
