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

package de.gematik.ti.erp.app.cardwall.mini.ui

import android.nfc.Tag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.authentication.model.Biometric
import de.gematik.ti.erp.app.authentication.model.External
import de.gematik.ti.erp.app.authentication.model.HealthCard
import de.gematik.ti.erp.app.authentication.model.InitialAuthenticationData
import de.gematik.ti.erp.app.authentication.model.None
import de.gematik.ti.erp.app.authentication.model.PromptAuthenticator
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcHealthCard
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.cardwall.usecase.MiniCardWallUseCase
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.model.UniversalLinkIdp
import de.gematik.ti.erp.app.idp.usecase.AuthenticateWithExternalHealthInsuranceAppUseCase
import de.gematik.ti.erp.app.idp.usecase.GetHealthInsuranceAppIdpsUseCase
import de.gematik.ti.erp.app.idp.usecase.GetUniversalLinkForHealthInsuranceAppsUseCase
import de.gematik.ti.erp.app.idp.usecase.RemoveAuthenticationUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
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

// TODO: Needs to be taken out of the bridge architecture
@Stable
class MiniCardWallController(
    private val miniCardWallUseCase: MiniCardWallUseCase,
    private val authenticationUseCase: AuthenticationUseCase,
    private val getHealthInsuranceAppIdpsUseCase: GetHealthInsuranceAppIdpsUseCase,
    private val getUniversalLinkUseCase: GetUniversalLinkForHealthInsuranceAppsUseCase,
    private val authenticateWithExternalHealthInsuranceAppUseCase: AuthenticateWithExternalHealthInsuranceAppUseCase,
    private val removeAuthenticationUseCase: RemoveAuthenticationUseCase,
    private val dispatchers: DispatchProvider
) : AuthenticationBridge {
    private fun PromptAuthenticator.AuthScope.toIdpScope() =
        when (this) {
            PromptAuthenticator.AuthScope.Prescriptions -> IdpScope.Default
            PromptAuthenticator.AuthScope.PairedDevices -> IdpScope.BiometricPairing
        }

    override suspend fun authenticateFor(
        profileId: ProfileIdentifier
    ): InitialAuthenticationData {
        val profile = miniCardWallUseCase.profileData(profileId).first()
        return when (
            val ssoTokenScope: IdpData.SingleSignOnTokenScope? = miniCardWallUseCase
                .authenticationData(profileId).first().singleSignOnTokenScope
        ) {
            is IdpData.ExternalAuthenticationToken -> External(
                authenticatorId = ssoTokenScope.authenticatorId,
                authenticatorName = ssoTokenScope.authenticatorName,
                profile = profile
            )

            is IdpData.AlternateAuthenticationToken,
            is IdpData.AlternateAuthenticationWithoutToken -> Biometric(profile = profile)

            is IdpData.DefaultToken -> HealthCard(
                can = ssoTokenScope.cardAccessNumber,
                profile = profile
            )

            null -> None(profile = profile)
        }
    }

    override fun doSecureElementAuthentication(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope
    ): Flow<AuthenticationState> {
        return authenticationUseCase.authenticateWithSecureElement(
            profileId = profileId,
            scope = scope.toIdpScope()
        ).flowOn(dispatchers.io)
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
        ).flowOn(dispatchers.io)
    }

    override suspend fun loadExternalAuthenticators(): List<HealthInsuranceData> =
        withContext(dispatchers.io) {
            getHealthInsuranceAppIdpsUseCase.invoke()
        }

    override suspend fun doExternalAuthentication(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope,
        authenticatorId: String,
        authenticatorName: String
    ): Result<URI> = getUniversalLinkUseCase.invoke(
        universalLinkIdp = UniversalLinkIdp(
            authenticatorId = authenticatorId,
            authenticatorName = authenticatorName,
            profileId = profileId,
            isGid = true
        ),
        idpScope = scope.toIdpScope()
    )

    override suspend fun doExternalAuthorization(redirect: URI): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            Napier.i { "call from minicard wall" }
            authenticateWithExternalHealthInsuranceAppUseCase.invoke(redirect)
        }
    }

    override suspend fun doRemoveAuthentication(profileId: ProfileIdentifier) {
        withContext(dispatchers.io) {
            removeAuthenticationUseCase(profileId)
        }
    }
}

@Composable
fun rememberMiniCardWallController(): MiniCardWallController {
    val miniCardWallUseCase by rememberInstance<MiniCardWallUseCase>()

    val authenticationUseCase by rememberInstance<AuthenticationUseCase>()

    val getHealthInsuranceAppIdpsUseCase by
    rememberInstance<GetHealthInsuranceAppIdpsUseCase>()

    val getUniversalLinkForHealthInsuranceAppsUseCase by
    rememberInstance<GetUniversalLinkForHealthInsuranceAppsUseCase>()

    val authenticateWithExternalHealthInsuranceAppUseCase by
    rememberInstance<AuthenticateWithExternalHealthInsuranceAppUseCase>()

    val removeAuthenticationUseCase by rememberInstance<RemoveAuthenticationUseCase>()

    val dispatchers by rememberInstance<DispatchProvider>()

    return remember {
        MiniCardWallController(
            miniCardWallUseCase = miniCardWallUseCase,
            authenticationUseCase = authenticationUseCase,
            getUniversalLinkUseCase = getUniversalLinkForHealthInsuranceAppsUseCase,
            getHealthInsuranceAppIdpsUseCase = getHealthInsuranceAppIdpsUseCase,
            authenticateWithExternalHealthInsuranceAppUseCase = authenticateWithExternalHealthInsuranceAppUseCase,
            removeAuthenticationUseCase = removeAuthenticationUseCase,
            dispatchers = dispatchers
        )
    }
}
