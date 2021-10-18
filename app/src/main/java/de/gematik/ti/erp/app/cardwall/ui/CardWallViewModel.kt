/*
 * Copyright (c) 2021 gematik GmbH
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
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcHealthCard
import de.gematik.ti.erp.app.cardwall.ui.model.CardWallData
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.featuretoggle.Features
import de.gematik.ti.erp.app.prescription.usecase.PollingUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

private const val navStateKey = "cdwNavState"
@HiltViewModel
class CardWallViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val pollingUseCase: PollingUseCase,
    private val cardWallUseCase: CardWallUseCase,
    private val authenticationUseCase: AuthenticationUseCase,
    private val dispatchProvider: DispatchProvider,
    private val demoUseCase: DemoUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val toggleManager: FeatureToggleManager
) : BaseViewModel() {
    @Parcelize
    private data class NavState(
        val can: String,
        val pin: String,
        val authMethod: CardWallData.AuthenticationMethod
    ) : Parcelable

    val defaultState = CardWallData.State(
        hardwareRequirementsFulfilled = cardWallUseCase.deviceHasNFCAndAndroidMOrHigher,
        isIntroSeenByUser = cardWallUseCase.cardWallIntroIsAccepted,
        cardAccessNumber = cardWallUseCase.cardAccessNumber ?: "",
        selectedAuthenticationMethod = CardWallData.AuthenticationMethod.None,
        personalIdentificationNumber = "",
        demoMode = demoUseCase.demoModeActive.value,
    )

    private val navState = MutableStateFlow(
        savedStateHandle.get(navStateKey) ?: NavState(
            can = defaultState.cardAccessNumber,
            pin = defaultState.personalIdentificationNumber,
            authMethod = defaultState.selectedAuthenticationMethod,
        )
    )

    init {
        viewModelScope.launch {
            if (!savedStateHandle.contains(navStateKey)) {
                onSelectAuthenticationMethod(cardWallUseCase.getAuthenticationMethod(profilesUseCase.activeProfileName().first()))
            }
            navState.collect {
                savedStateHandle.set(navStateKey, it)
            }
        }
    }

    fun state(): Flow<CardWallData.State> =
        combine(
            navState,
            demoUseCase.demoModeActive
        ) { navState, demo ->
            defaultState.copy(
                cardAccessNumber = navState.can,
                personalIdentificationNumber = navState.pin,
                selectedAuthenticationMethod = navState.authMethod,
                demoMode = demo
            )
        }

    fun doAuthentication(
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
                    can = can,
                    pin = pin,
                    cardChannel = cardChannel
                )
            else ->
                authenticationUseCase.authenticateWithHealthCard(can = can, pin = pin, cardChannel = cardChannel)
                    .onEach {
                        if (it.isFinal() && !demoUseCase.isDemoModeActive) {
                            pollingUseCase.refreshNow()
                        }
                    }
        }
            .onEach {
                if (it.isFinal()) {
                    cardWallUseCase.cardAccessNumber = can
                }
            }
            .flowOn(dispatchProvider.io())
    }

    fun onCardAccessNumberChange(can: String) {
        navState.value = navState.value.copy(can = can)
    }

    fun onPersonalIdentificationChange(pin: String) {
        navState.value = navState.value.copy(pin = pin)
    }

    fun onSelectAuthenticationMethod(authMethod: CardWallData.AuthenticationMethod) {
        navState.value = navState.value.copy(authMethod = authMethod)
    }

    fun onIntroSeenByUser() {
        cardWallUseCase.cardWallIntroIsAccepted = true
    }

    fun isNFCEnabled() = cardWallUseCase.deviceHasNFCEnabled

    fun fastTrackOn() =
        toggleManager.featureState(Features.FAST_TRACK.featureName)
}
