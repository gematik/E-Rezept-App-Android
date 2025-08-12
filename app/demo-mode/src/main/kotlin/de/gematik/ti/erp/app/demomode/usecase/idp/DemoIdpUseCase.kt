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

package de.gematik.ti.erp.app.demomode.usecase.idp

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import java.security.PublicKey

class DemoIdpUseCase(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IdpUseCase {
    override suspend fun loadAccessToken(
        profileId: ProfileIdentifier,
        refresh: Boolean,
        scope: IdpScope
    ) = "always-give-an-access-token"

    override suspend fun authenticationFlowWithHealthCard(
        profileId: ProfileIdentifier,
        scope: IdpScope,
        cardAccessNumber: String,
        healthCardCertificate: suspend () -> ByteArray,
        sign: suspend (hash: ByteArray) -> ByteArray
    ) {
        // no implementation for demo mode
    }

    override suspend fun alternatePairingFlowWithSecureElement(
        profileId: ProfileIdentifier,
        cardAccessNumber: String,
        publicKeyOfSecureElementEntry: PublicKey,
        aliasOfSecureElementEntry: ByteArray,
        healthCardCertificate: suspend () -> ByteArray,
        signWithHealthCard: suspend (hash: ByteArray) -> ByteArray
    ) {
        // no implementation for demo mode
    }

    override suspend fun alternateAuthenticationFlowWithSecureElement(profileId: ProfileIdentifier, scope: IdpScope) {
        // no implementation for demo mode
    }

    override suspend fun getPairedDevices(profileId: ProfileIdentifier):
        Result<List<Pair<PairingResponseEntry, PairingData>>> = withContext(dispatcher) {
        val device = dataSource.pairedDevices.map { it.toList() }.first()
        Result.success(device)
    }

    override suspend fun deletePairedDevice(profileId: ProfileIdentifier, deviceAlias: String): Result<Unit> =
        withContext(dispatcher) {
            dataSource.pairedDevices.updateAndGet {
                it.clear()
                it
            }
            Result.success(Unit)
        }
}
