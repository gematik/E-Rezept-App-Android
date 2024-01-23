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

package de.gematik.ti.erp.app.cardwall.mini.ui

import android.nfc.Tag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcHealthCard
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.cardwall.usecase.MiniCardWallUseCase
import de.gematik.ti.erp.app.idp.api.models.AuthenticationId
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.kodein.di.compose.rememberInstance
import java.net.URI

/**
 * The [MiniCardWallController] is used for refreshing tokens of several authentication methods.
 * While the actual mini card wall is just the prompt for authentication with health card or external authentication,
 * the biometric/alternate authentication uses the prompt provided by the system.
 */

@Stable
class MiniCardWallController(
    private val useCase: MiniCardWallUseCase,
    private val authenticationUseCase: AuthenticationUseCase,
    private val idpUseCase: IdpUseCase,
    private val idpRepository: IdpRepository,
    private val dispatchers: DispatchProvider
) : AuthenticationBridge {
    private fun PromptAuthenticator.AuthScope.toIdpScope() =
        when (this) {
            PromptAuthenticator.AuthScope.Prescriptions -> IdpScope.Default
            PromptAuthenticator.AuthScope.PairedDevices -> IdpScope.BiometricPairing
        }

    override suspend fun authenticateFor(
        profileId: ProfileIdentifier
    ): AuthenticationBridge.InitialAuthenticationData {
        val profile = useCase.profileData(profileId).first()
        return when (val ssoTokenScope = useCase.authenticationData(profileId).first().singleSignOnTokenScope) {
            is IdpData.ExternalAuthenticationToken -> AuthenticationBridge.External(
                authenticatorId = ssoTokenScope.authenticatorId,
                authenticatorName = ssoTokenScope.authenticatorName,
                profile = profile
            )

            is IdpData.AlternateAuthenticationToken,
            is IdpData.AlternateAuthenticationWithoutToken -> AuthenticationBridge.SecureElement(profile = profile)

            is IdpData.DefaultToken -> AuthenticationBridge.HealthCard(
                can = ssoTokenScope.cardAccessNumber,
                profile = profile
            )

            null -> AuthenticationBridge.None(profile = profile)
        }
    }

    override fun doSecureElementAuthentication(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope
    ): Flow<AuthenticationState> {
        return authenticationUseCase.authenticateWithSecureElement(
            profileId = profileId,
            scope = scope.toIdpScope()
        ).flowOn(dispatchers.IO)
    }

    override fun doHealthCardAuthentication(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope,
        can: String,
        pin: String,
        tag: Tag
    ): Flow<AuthenticationState> {
        return authenticationUseCase.authenticateWithHealthCard(
            profileId = profileId,
            scope = scope.toIdpScope(),
            can = can,
            pin = pin,
            cardChannel = flow { emit(NfcHealthCard.connect(tag)) }
        ).flowOn(dispatchers.IO)
    }

    override suspend fun loadExternalAuthenticators(): List<AuthenticationId> =
        withContext(dispatchers.IO) {
            idpUseCase.loadExternAuthenticatorIDs()
        }

    override suspend fun doExternalAuthentication(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope,
        authenticatorId: String,
        authenticatorName: String
    ): Result<URI> = withContext(dispatchers.IO) {
        runCatching {
            idpUseCase.getUniversalLinkForExternalAuthorization(
                profileId = profileId,
                scope = scope.toIdpScope(),
                authenticatorId = authenticatorId,
                authenticatorName = authenticatorName
            )
        }
    }

    override suspend fun doExternalAuthorization(redirect: URI): Result<Unit> = withContext(dispatchers.IO) {
        runCatching {
            idpUseCase.authenticateWithExternalAppAuthorization(redirect)
        }
    }

    override suspend fun doRemoveAuthentication(profileId: ProfileIdentifier) {
        withContext(dispatchers.IO) {
            idpRepository.invalidate(profileId)
        }
    }
}

@Composable
fun rememberMiniCardWallController(): MiniCardWallController {
    val useCase by rememberInstance<MiniCardWallUseCase>()
    val authenticationUseCase by rememberInstance<AuthenticationUseCase>()
    val idpUseCase by rememberInstance<IdpUseCase>()
    val idpRepository by rememberInstance<IdpRepository>()
    val dispatchers by rememberInstance<DispatchProvider>()

    return remember {
        MiniCardWallController(
            useCase,
            authenticationUseCase,
            idpUseCase,
            idpRepository,
            dispatchers
        )
    }
}
