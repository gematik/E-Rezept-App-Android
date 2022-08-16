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
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class CardWallViewModel(
    private val cardWallUseCase: CardWallUseCase,
    private val authenticationUseCase: AuthenticationUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel() {
    private data class DelayedState(
        val profileId: ProfileIdentifier,
        val can: String,
        val pin: String,
        val authMethod: CardWallData.AuthenticationMethod
    )

    val defaultState = CardWallData.State(
        activeProfileId = "",
        hardwareRequirementsFulfilled = cardWallUseCase.deviceHasNFCAndAndroidMOrHigher,
        cardAccessNumber = "",
        selectedAuthenticationMethod = CardWallData.AuthenticationMethod.None,
        personalIdentificationNumber = ""
    )

    private var delayedState = MutableStateFlow(
        DelayedState(
            profileId = "",
            can = "",
            pin = "",
            authMethod = CardWallData.AuthenticationMethod.None
        )
    )

    fun state(profileId: ProfileIdentifier): Flow<CardWallData.State> {
        delayedState.update {
            DelayedState(
                profileId = profileId,
                can = it.can,
                pin = it.pin,
                authMethod = it.authMethod
            )
        }

        return delayedState.map { navState ->
            defaultState.copy(
                activeProfileId = navState.profileId,
                cardAccessNumber = navState.can,
                personalIdentificationNumber = navState.pin,
                selectedAuthenticationMethod = navState.authMethod
            )
        }.onStart {
            withContext(dispatchers.IO) {
                val authData = cardWallUseCase.authenticationData(profileId).first()
                (authData.singleSignOnTokenScope as? IdpData.TokenWithHealthCardScope)?.also { tokenScope ->
                    delayedState.update {
                        it.copy(
                            can = tokenScope.cardAccessNumber,
                            authMethod = when (authData.singleSignOnTokenScope) {
                                is IdpData.AlternateAuthenticationToken -> CardWallData.AuthenticationMethod.Alternative
                                is IdpData.AlternateAuthenticationWithoutToken -> CardWallData.AuthenticationMethod.Alternative
                                else -> CardWallData.AuthenticationMethod.HealthCard
                            }
                        )
                    }
                }
            }
        }
    }

    fun doAuthentication(
        profileId: ProfileIdentifier,
        can: String,
        pin: String,
        method: CardWallData.AuthenticationMethod,
        tag: Flow<Tag>
    ): Flow<AuthenticationState> {
        val cardChannel = tag.map { NfcHealthCard.connect(it) }

        return when {
            method == CardWallData.AuthenticationMethod.None -> error("Authentication method must be set")
            method == CardWallData.AuthenticationMethod.Alternative && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ->
                authenticationUseCase.pairDeviceWithHealthCardAndSecureElement(
                    profileId = profileId,
                    can = can,
                    pin = pin,
                    cardChannel = cardChannel
                )
            else ->
                authenticationUseCase.authenticateWithHealthCard(
                    profileId = profileId,
                    can = can,
                    pin = pin,
                    cardChannel = cardChannel
                )
        }
            .flowOn(dispatchers.IO)
    }

    fun isNFCEnabled() = cardWallUseCase.deviceHasNFCEnabled
}
