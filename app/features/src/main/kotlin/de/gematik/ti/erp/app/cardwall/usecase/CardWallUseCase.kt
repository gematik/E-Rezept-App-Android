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

package de.gematik.ti.erp.app.cardwall.usecase

import de.gematik.ti.erp.app.ErezeptApp.Companion.applicationModule
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.settings.repository.CardWallRepository
import de.gematik.ti.erp.app.utils.extensions.hasNFCTerminal
import de.gematik.ti.erp.app.utils.extensions.isNfcEnabled
import de.gematik.ti.erp.app.utils.extensions.riskyOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

open class CardWallUseCase(
    private val idpRepository: IdpRepository,
    private val cardWallRepository: CardWallRepository
) {
    private val deviceHasNFCFlow =
        MutableStateFlow(applicationModule.androidContext().hasNFCTerminal() || cardWallRepository.hasFakeNFCEnabled)

    val deviceHasNfcStateFlow: Flow<Boolean> = deviceHasNFCFlow.asStateFlow()
    fun updateDeviceNFCCapability(value: Boolean) {
        cardWallRepository.hasFakeNFCEnabled = value
        deviceHasNFCFlow.value = value
    }

    // On some devices, the isEnabled() method of the NFCManager throws an exception
    // https://stackoverflow.com/questions/23564475/check-programmatically-if-device-has-nfc-reader
    fun checkNfcEnabled(): Boolean = riskyOperation(
        block = applicationModule.androidContext()::isNfcEnabled,
        defaultValue = false
    ) ?: false

    fun authenticationData(profileId: ProfileIdentifier): Flow<IdpData.AuthenticationData> =
        idpRepository.authenticationData(profileId)
}
