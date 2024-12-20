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

package de.gematik.ti.erp.app.profiles.usecase

import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.PairedDevice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class GetPairedDevicesUseCase(
    private val idpUseCase: IdpUseCase,
    formatStyle: FormatStyle = FormatStyle.LONG,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(
        profileId: ProfileIdentifier,
        keyStoreAlias: String
    ): Flow<List<PairedDevice>> =
        flow {
            emit(
                idpUseCase.getPairedDevices(profileId).getOrThrow()
                    .map { (pairingResponseEntry, pairingData) ->
                        val creationTime = Instant.fromEpochSeconds(pairingResponseEntry.creationTime)
                        PairedDevice(
                            name = pairingResponseEntry.name,
                            alias = pairingData.keyAliasOfSecureElement,
                            connectedOn = creationTime
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .toJavaLocalDateTime().format(dateTimeFormatter),
                            isCurrentDevice = keyStoreAlias == pairingData.keyAliasOfSecureElement
                        )
                    }.sortedByDescending(PairedDevice::connectedOn)
            )
        }.flowOn(dispatcher)

    private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(formatStyle)
}
