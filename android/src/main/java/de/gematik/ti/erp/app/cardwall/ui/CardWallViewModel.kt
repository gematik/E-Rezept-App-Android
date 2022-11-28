/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.ui

import android.nfc.Tag
import android.os.Build
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcHealthCard
import de.gematik.ti.erp.app.cardwall.ui.model.CardWallData
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import androidx.lifecycle.ViewModel
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class CardWallViewModel(
    private val cardWallUseCase: CardWallUseCase,
    private val authenticationUseCase: AuthenticationUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel() {

    val defaultState = CardWallData.State(
        hardwareRequirementsFulfilled = cardWallUseCase.deviceHasNFCAndAndroidMOrHigher
    )

    fun state(): Flow<CardWallData.State> = flowOf(defaultState)

    fun doAuthentication(
        profileId: ProfileIdentifier,
        authenticationData: CardWallAuthenticationData,
        tag: Flow<Tag>
    ): Flow<AuthenticationState> {
        val cardChannel = tag.map { NfcHealthCard.connect(it) }

        return when (authenticationData) {
            is CardWallAuthenticationData.AltPairingWithHealthCard ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    authenticationUseCase.pairDeviceWithHealthCardAndSecureElement(
                        profileId = profileId,
                        can = authenticationData.cardAccessNumber,
                        pin = authenticationData.personalIdentificationNumber,
                        publicKeyOfSecureElementEntry = authenticationData.initialPairingData.publicKey,
                        aliasOfSecureElementEntry = authenticationData.initialPairingData.aliasOfSecureElementEntry,
                        cardChannel = cardChannel
                    ).onEach {
                        if (it.isFinal()) {
                            // silent fail; user has the alternative on the main screen
                            authenticationUseCase.authenticateWithSecureElement(
                                profileId = profileId,
                                scope = IdpScope.Default
                            ).collect {
                                Napier.d { "Auth after pairing: $it" }
                            }
                        }
                    }
                } else {
                    error("Can't use biometric authentication below Android P")
                }

            is CardWallAuthenticationData.HealthCard ->
                authenticationUseCase.authenticateWithHealthCard(
                    profileId = profileId,
                    can = authenticationData.cardAccessNumber,
                    pin = authenticationData.personalIdentificationNumber,
                    cardChannel = cardChannel
                )
        }
            .flowOn(dispatchers.IO)
    }

    fun isNFCEnabled() = cardWallUseCase.deviceHasNFCEnabled
}
