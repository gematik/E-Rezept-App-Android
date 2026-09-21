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

package de.gematik.ti.erp.app.debugsettings.usecase

import de.gematik.ti.erp.app.debugsettings.model.SsoTokenHeader
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.navigation.json
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
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
            idpRepository.getUserAuthentication(profile.id).first().let {
                if (it.singleSignOnTokenErpModel == null) {
                    onResult(Result.failure(IllegalStateException("No SSO token found")))
                } else {
                    val brokenToken = it.singleSignOnTokenErpModel?.breakToken()
                    val userAuthentication = when (it) {
                        is UserAuthenticationErpModel.External -> {
                            it.copy(singleSignOnTokenErpModel = brokenToken)
                        }
                        is UserAuthenticationErpModel.HealthCard -> {
                            it.copy(singleSignOnTokenErpModel = brokenToken)
                        }
                        is UserAuthenticationErpModel.HealthCardWithSavedCredentials -> {
                            it.copy(singleSignOnTokenErpModel = brokenToken)
                        }
                        is UserAuthenticationErpModel.NotInitialized -> {
                            UserAuthenticationErpModel.NotInitialized
                        }
                    }
                    idpRepository.saveUserAuthentication(
                        profileId = profile.id,
                        authentication = userAuthentication
                    )
                    idpRepository.decryptedAccessToken(profile.id).firstOrNull()?.let { accessToken ->
                        idpRepository.saveDecryptedAccessToken(
                            profileId = profile.id,
                            accessToken = accessToken.copy(
                                accessToken = accessToken.accessToken,
                                expiresOn = Clock.System.now().minus(12.hours)
                            )
                        )
                    }
                    idpRepository.invalidateDecryptedAccessToken(profile.id)
                    onResult(Result.success(Unit))
                }
            }
        } catch (e: Exception) {
            Napier.e { "SSO Token error ${e.message}" }
            onResult(Result.failure(e))
        }
    }

    // The SSO token does not expire, the signature changes which makes it invalid
    @Suppress("MagicNumber")
    private fun SingleSignOnTokenErpModel.breakToken(): SingleSignOnTokenErpModel {
        val (hours, rest) = token.split('.', limit = 2)
        val twelveHoursBefore = Instant.now().minus(12, ChronoUnit.HOURS).epochSecond
        val ssoTokenHeaderJsonString = Base64Url.decodeToUtf8String(hours)
        val ssoTokenHeader = SsoTokenHeader.toSsoTokenHeader(ssoTokenHeaderJsonString)
        val updatedSsoTokenHeader = ssoTokenHeader.copy(exp = twelveHoursBefore)
        val updatedSsoTokenHeaderJsonString = json.encodeToString<SsoTokenHeader>(updatedSsoTokenHeader)
        val encodedHeader = Base64Url.encodeUtf8ByteRepresentation(updatedSsoTokenHeaderJsonString)
        return SingleSignOnTokenErpModel("$encodedHeader.$rest")
    }
}
