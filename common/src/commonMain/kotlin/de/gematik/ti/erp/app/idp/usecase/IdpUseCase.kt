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

@file:Suppress("LongParameterList")

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import java.security.PublicKey

private typealias ProfileId = ProfileIdentifier

interface IdpUseCase {

    suspend fun loadAccessToken(
        profileId: ProfileIdentifier,
        refresh: Boolean,
        scope: IdpScope = IdpScope.Default
    ): String

    suspend fun authenticationFlowWithHealthCard(
        profileId: ProfileId,
        scope: IdpScope = IdpScope.Default,
        cardAccessNumber: String,
        healthCardCertificate: suspend () -> ByteArray,
        sign: suspend (hash: ByteArray) -> ByteArray
    )

    /**
     * Pairing flow fetching the sso & access token requiring the health card and generated key material.
     */
    suspend fun alternatePairingFlowWithSecureElement(
        profileId: ProfileId,
        cardAccessNumber: String,
        publicKeyOfSecureElementEntry: PublicKey,
        aliasOfSecureElementEntry: ByteArray,
        healthCardCertificate: suspend () -> ByteArray,
        signWithHealthCard: suspend (hash: ByteArray) -> ByteArray
    ): Unit

    suspend fun alternateAuthenticationFlowWithSecureElement(
        profileId: ProfileId,
        scope: IdpScope = IdpScope.Default
    )

    suspend fun getPairedDevices(profileId: ProfileId): Result<List<Pair<PairingResponseEntry, PairingData>>>

    suspend fun deletePairedDevice(profileId: ProfileId, deviceAlias: String): Result<Unit>
}
