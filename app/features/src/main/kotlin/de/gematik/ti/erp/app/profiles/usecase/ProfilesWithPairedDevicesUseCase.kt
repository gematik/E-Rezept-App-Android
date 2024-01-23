/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.PairedDevice
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Instant

// TODO: Use the IdpUseCase and do this in the controller where it is called or do this in the IdpUseCase
class ProfilesWithPairedDevicesUseCase(
    private val idpUseCase: IdpUseCase,
    private val dispatchers: DispatchProvider
) {
    fun pairedDevices(profileId: ProfileIdentifier): Flow<ProfilesUseCaseData.PairedDevices> =
        flow {
            emit(
                ProfilesUseCaseData.PairedDevices(
                    idpUseCase.getPairedDevices(profileId).getOrThrow().map { (raw, pairingData) ->
                        PairedDevice(
                            name = raw.name,
                            alias = pairingData.keyAliasOfSecureElement,
                            connectedOn = Instant.fromEpochSeconds(raw.creationTime)
                        )
                    }.sortedByDescending {
                        it.connectedOn
                    }
                )
            )
        }.flowOn(dispatchers.io)

    suspend fun deletePairedDevices(
        profileId: ProfileIdentifier,
        device: PairedDevice
    ): Result<String> =
        idpUseCase.deletePairedDevice(profileId = profileId, deviceAlias = device.alias).map {
            device.name
        }
}
