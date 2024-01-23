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
import de.gematik.ti.erp.app.authentication.mapper.PromptAuthenticationProvider
import de.gematik.ti.erp.app.authentication.model.External
import de.gematik.ti.erp.app.authentication.model.HealthCard
import de.gematik.ti.erp.app.authentication.model.InitialAuthenticationData
import de.gematik.ti.erp.app.authentication.model.None
import de.gematik.ti.erp.app.authentication.model.PromptAuthenticator
import de.gematik.ti.erp.app.authentication.model.SecureElement
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.core.IntentHandler
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.kodein.di.compose.rememberInstance
import java.net.URI

class NoneEnrolledException : IllegalStateException()
class UserNotAuthenticatedException : IllegalStateException()
class RedirectUrlWrongException : IllegalStateException()

interface AuthenticationBridge {
    suspend fun authenticateFor(
        profileId: ProfileIdentifier
    ): InitialAuthenticationData

    fun doSecureElementAuthentication(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope
    ): Flow<AuthenticationState>

    fun doHealthCardAuthentication(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope,
        can: String,
        pin: String,
        tag: Tag
    ): Flow<AuthenticationState>

    suspend fun loadExternalAuthenticators(): List<HealthInsuranceData>

    suspend fun doExternalAuthentication(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope,
        authenticatorId: String,
        authenticatorName: String
    ): Result<URI>

    suspend fun doExternalAuthorization(
        redirect: URI
    ): Result<Unit>

    suspend fun doRemoveAuthentication(profileId: ProfileIdentifier)
}

/**
 * TODO: Modify implementation
 * Implementation does not follow a particular design pattern by
 * having controllers calling controllers and having composable in the same file as
 * the controllers and having the whole feature hidden inside the cardwall.ui
 */
@Stable
class Authenticator(
    val authenticatorSecureElement: SecureHardwarePromptAuthenticator,
    val authenticatorHealthCard: HealthCardPromptAuthenticator,
    val authenticatorExternal: ExternalPromptAuthenticator,
    val mapper: PromptAuthenticationProvider,
    private val bridge: AuthenticationBridge
) {
    fun authenticateForPrescriptions(profileId: ProfileIdentifier): Flow<PromptAuthenticator.AuthResult> =
        flow {
            val initialAuthenticationData = bridge.authenticateFor(profileId)
            emitAll(
                mapper.mapAuthenticationResult(
                    id = profileId,
                    initialAuthenticationData = initialAuthenticationData,
                    scope = PromptAuthenticator.AuthScope.Prescriptions,
                    authenticators = listOf(
                        authenticatorHealthCard,
                        authenticatorSecureElement,
                        authenticatorExternal
                    )
                )
            )
        }

    fun authenticateForPairedDevices(profileId: ProfileIdentifier): Flow<PromptAuthenticator.AuthResult> =
        flow {
            emitAll(
                when (bridge.authenticateFor(profileId)) {
                    is HealthCard ->
                        authenticatorHealthCard.authenticate(profileId, PromptAuthenticator.AuthScope.PairedDevices)

                    is SecureElement ->
                        authenticatorSecureElement.authenticate(profileId, PromptAuthenticator.AuthScope.PairedDevices)

                    is External ->
                        authenticatorExternal.authenticate(profileId, PromptAuthenticator.AuthScope.PairedDevices)

                    is None -> flowOf(PromptAuthenticator.AuthResult.NoneEnrolled)
                }
            )
        }

    suspend fun cancelAllAuthentications() {
        authenticatorSecureElement.cancelAuthentication()
        authenticatorHealthCard.cancelAuthentication()
    }
}

@Composable
fun rememberAuthenticator(intentHandler: IntentHandler): Authenticator {
    val bridge = rememberMiniCardWallController()
    val promptSE = rememberSecureHardwarePromptAuthenticator(bridge)
    val promptHC = rememberHealthCardPromptAuthenticator(bridge)
    val promptEX = rememberExternalPromptAuthenticator(bridge, intentHandler)
    val mapper by rememberInstance<PromptAuthenticationProvider>()
    return remember {
        Authenticator(
            authenticatorSecureElement = promptSE,
            authenticatorHealthCard = promptHC,
            authenticatorExternal = promptEX,
            bridge = bridge,
            mapper = mapper
        )
    }
}
