/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.presentation

import android.nfc.Tag
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.DomainVerifier
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcHealthCard
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallAuthenticationData
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.cardwall.usecase.CardWallUseCase
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.kodein.di.compose.rememberInstance

@Stable
class CardWallController(
    private val cardWallUseCase: CardWallUseCase,
    private val authenticationUseCase: AuthenticationUseCase,
    private val domainVerifier: DomainVerifier
) : Controller() {

    fun doAuthentication(
        profileId: ProfileIdentifier,
        authenticationData: CardWallAuthenticationData,
        tag: Flow<Tag>
    ): Flow<AuthenticationState> {
        val cardChannel = tag.map { NfcHealthCard.connect(it) }

        return when (authenticationData) {
            is CardWallAuthenticationData.SaveCredentialsWithHealthCard ->
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
                @Requirement(
                    "O.Auth_3#1",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "Implementation of the EGK connection"
                )
                authenticationUseCase.authenticateWithHealthCard(
                    profileId = profileId,
                    can = authenticationData.cardAccessNumber,
                    pin = authenticationData.personalIdentificationNumber,
                    cardChannel = cardChannel
                )
        }
            .flowOn(Dispatchers.IO)
    }

    fun isNfcEnabled(): Boolean {
        return cardWallUseCase.isNfcEnabled()
    }

    val isDomainVerified by lazy { domainVerifier.areDomainsVerified }

    val isNFCAvailable
        @Composable
        get() = cardWallUseCase.deviceHasNfcStateFlow.collectAsStateWithLifecycle(false)
}

@Composable
fun rememberCardWallController(): CardWallController {
    val cardWallUseCase by rememberInstance<CardWallUseCase>()
    val authenticationUseCase by rememberInstance<AuthenticationUseCase>()
    val domainVerifier by rememberInstance<DomainVerifier>()
    return remember {
        CardWallController(
            cardWallUseCase = cardWallUseCase,
            authenticationUseCase = authenticationUseCase,
            domainVerifier = domainVerifier
        )
    }
}
