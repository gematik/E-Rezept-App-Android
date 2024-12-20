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

package de.gematik.ti.erp.app.debugsettings.usecase

import de.gematik.ti.erp.app.debugsettings.model.SsoTokenHeader
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.navigation.json
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import org.jose4j.base64url.Base64Url
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.hours

class BreakSsoTokenUseCase(
    private val profileRepository: ProfileRepository,
    private val idpRepository: IdpRepository
) {
    suspend operator fun invoke(
        onResult: (Result<Unit>) -> Unit
    ) {
        try {
            val profile = profileRepository.activeProfile().first()
            idpRepository.authenticationData(profile.id).first().singleSignOnTokenScope?.let {
                val newToken = when (it) {
                    is IdpData.AlternateAuthenticationToken ->
                        IdpData.AlternateAuthenticationToken(
                            token = it.token?.breakToken(),
                            cardAccessNumber = it.cardAccessNumber,
                            aliasOfSecureElementEntry = it.aliasOfSecureElementEntry,
                            healthCardCertificate = it.healthCardCertificate.encoded
                        )

                    is IdpData.DefaultToken ->
                        IdpData.DefaultToken(
                            token = it.token?.breakToken(),
                            cardAccessNumber = it.cardAccessNumber,
                            healthCardCertificate = it.healthCardCertificate.encoded
                        )

                    is IdpData.ExternalAuthenticationToken ->
                        IdpData.ExternalAuthenticationToken(
                            token = it.token?.breakToken(),
                            authenticatorName = it.authenticatorName,
                            authenticatorId = it.authenticatorId
                        )

                    else -> it
                }
                idpRepository.saveSingleSignOnToken(
                    profileId = profile.id,
                    token = newToken
                )
                idpRepository.decryptedAccessToken(profile.id).firstOrNull()?.let { accessToken ->
                    val updatedAccessToken = accessToken.copy(
                        accessToken = accessToken.accessToken,
                        expiresOn = Clock.System.now().minus(12.hours)
                    )
                    idpRepository.saveDecryptedAccessToken(
                        profileId = profile.id,
                        accessToken = updatedAccessToken
                    )
                }
                idpRepository.invalidateDecryptedAccessToken(profile.id)
                onResult(Result.success(Unit))
            } ?: {
                onResult(Result.failure(IllegalStateException("No SSO token found")))
            }
        } catch (e: Exception) {
            Napier.e { "SSO Token error ${e.message}" }
            onResult(Result.failure(e))
        }
    }

    // The SSO token does not expire, the signature changes which makes it invalid
    @Suppress("MagicNumber")
    private fun IdpData.SingleSignOnToken.breakToken(): IdpData.SingleSignOnToken {
        val (hours, rest) = token.split('.', limit = 2)
        val twelveHoursBefore = Instant.now().minus(12, ChronoUnit.HOURS).epochSecond
        val ssoTokenHeaderJsonString = Base64Url.decodeToUtf8String(hours)
        val ssoTokenHeader = SsoTokenHeader.toSsoTokenHeader(ssoTokenHeaderJsonString)
        val updatedSsoTokenHeader = ssoTokenHeader.copy(exp = twelveHoursBefore)
        val updatedSsoTokenHeaderJsonString = json.encodeToString<SsoTokenHeader>(updatedSsoTokenHeader)
        val encodedHeader = Base64Url.encodeUtf8ByteRepresentation(updatedSsoTokenHeaderJsonString)
        return IdpData.SingleSignOnToken("$encodedHeader.$rest")
    }
}
