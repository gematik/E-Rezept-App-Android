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

package de.gematik.ti.erp.app.idp.repository

import de.gematik.ti.erp.app.idp.api.models.Challenge
import de.gematik.ti.erp.app.idp.api.models.IdpNonce
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.api.models.IdpState
import de.gematik.ti.erp.app.idp.api.models.JWSPublicKey
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntries
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.api.models.RemoteFederationIdp
import de.gematik.ti.erp.app.idp.api.models.TokenResponse
import de.gematik.ti.erp.app.idp.api.models.UniversalLinkToken
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import java.net.URI
import java.security.PublicKey

interface IdpRepository {
    fun decryptedAccessToken(profileId: ProfileIdentifier): Flow<AccessToken?>
    fun authenticationData(profileId: ProfileIdentifier): Flow<IdpData.AuthenticationData>
    fun saveDecryptedAccessToken(profileId: ProfileIdentifier, accessToken: AccessToken)
    suspend fun saveSingleSignOnToken(profileId: ProfileIdentifier, token: IdpData.SingleSignOnTokenScope)
    suspend fun fetchChallenge(
        url: String,
        codeChallenge: String,
        state: String,
        nonce: String,
        isDeviceRegistration: Boolean,
        redirectUri: String
    ): Result<Challenge>

    suspend fun loadUncheckedIdpConfiguration(): IdpData.IdpConfiguration
    suspend fun postSignedChallenge(url: String, signedChallenge: String): Result<String>
    suspend fun postUnsignedChallengeWithSso(url: String, ssoToken: String, unsignedChallenge: String): Result<String>
    suspend fun postToken(url: String, keyVerifier: String, code: String, redirectUri: String): Result<TokenResponse>
    suspend fun fetchFederationIDList(url: String, idpPukSigKey: PublicKey): List<RemoteFederationIdp>
    suspend fun fetchIdpPukSig(url: String): Result<JWSPublicKey>
    suspend fun fetchIdpPukEnc(url: String): Result<JWSPublicKey>
    suspend fun postPairing(url: String, encryptedRegistrationData: String, token: String): Result<PairingResponseEntry>
    suspend fun getPairing(url: String, token: String): Result<PairingResponseEntries>
    suspend fun deletePairing(url: String, token: String, alias: String): Result<Unit>
    suspend fun postBiometricAuthenticationData(url: String, encryptedSignedAuthenticationData: String): Result<String>
    suspend fun authorizeExternalHealthInsuranceAppDataAsGiD(url: String, token: UniversalLinkToken): Result<String>
    suspend fun invalidate(profileId: ProfileIdentifier)
    suspend fun invalidateConfig()
    suspend fun invalidateSingleSignOnTokenRetainingScope(profileId: ProfileIdentifier)
    fun invalidateDecryptedAccessToken(profileId: ProfileIdentifier)
    suspend fun getGidAuthorizationRedirect(
        url: String,
        state: IdpState,
        codeChallenge: String,
        nonce: IdpNonce,
        externalAppId: String,
        idpScope: IdpScope
    ): Result<URI>
}
