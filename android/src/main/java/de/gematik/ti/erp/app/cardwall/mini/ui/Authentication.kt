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

package de.gematik.ti.erp.app.cardwall.mini.ui

import android.nfc.Tag
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.core.IntentHandler
import de.gematik.ti.erp.app.idp.api.models.AuthenticationId
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.Avatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.kodein.di.compose.rememberViewModel
import java.net.URI

class NoneEnrolledException : IllegalStateException()
class UserNotAuthenticatedException : IllegalStateException()

@Stable
interface PromptAuthenticator {
    enum class AuthResult {
        Authenticated,
        Cancelled,
        NoneEnrolled,
        UserNotAuthenticated
    }

    enum class AuthScope {
        Prescriptions, PairedDevices
    }

    fun authenticate(profileId: ProfileIdentifier, scope: AuthScope): Flow<AuthResult>

    suspend fun cancelAuthentication()
}

interface AuthenticationBridge {
    @Stable
    sealed interface InitialAuthenticationData {
        val profile: ProfilesUseCaseData.Profile
    }

    data class HealthCard(val can: String, override val profile: ProfilesUseCaseData.Profile) :
        InitialAuthenticationData

    data class SecureElement(override val profile: ProfilesUseCaseData.Profile) : InitialAuthenticationData
    data class External(
        val authenticatorId: String,
        val authenticatorName: String,
        override val profile: ProfilesUseCaseData.Profile
    ) : InitialAuthenticationData

    data class None(override val profile: ProfilesUseCaseData.Profile) : InitialAuthenticationData

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

    suspend fun loadExternalAuthenticators(): List<AuthenticationId>

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

@Stable
class Authenticator(
    val authenticatorSecureElement: SecureHardwarePromptAuthenticator,
    val authenticatorHealthCard: HealthCardPromptAuthenticator,
    val authenticatorExternal: ExternalPromptAuthenticator,
    private val bridge: AuthenticationBridge
) {
    fun authenticateForPrescriptions(profileId: ProfileIdentifier): Flow<PromptAuthenticator.AuthResult> =
        flow {
            emitAll(
                when (bridge.authenticateFor(profileId)) {
                    is AuthenticationBridge.HealthCard ->
                        authenticatorHealthCard.authenticate(profileId, PromptAuthenticator.AuthScope.Prescriptions)

                    is AuthenticationBridge.SecureElement ->
                        authenticatorSecureElement.authenticate(profileId, PromptAuthenticator.AuthScope.Prescriptions)

                    is AuthenticationBridge.External ->
                        authenticatorExternal.authenticate(profileId, PromptAuthenticator.AuthScope.Prescriptions)

                    is AuthenticationBridge.None -> flowOf(PromptAuthenticator.AuthResult.NoneEnrolled)
                }
            )
        }

    fun authenticateForPairedDevices(profileId: ProfileIdentifier): Flow<PromptAuthenticator.AuthResult> =
        flow {
            emitAll(
                when (bridge.authenticateFor(profileId)) {
                    is AuthenticationBridge.HealthCard ->
                        authenticatorHealthCard.authenticate(profileId, PromptAuthenticator.AuthScope.PairedDevices)

                    is AuthenticationBridge.SecureElement ->
                        authenticatorSecureElement.authenticate(profileId, PromptAuthenticator.AuthScope.PairedDevices)

                    is AuthenticationBridge.External ->
                        authenticatorExternal.authenticate(profileId, PromptAuthenticator.AuthScope.PairedDevices)

                    is AuthenticationBridge.None -> flowOf(PromptAuthenticator.AuthResult.NoneEnrolled)
                }
            )
        }

    suspend fun cancelAllAuthentications() {
        authenticatorSecureElement.cancelAuthentication()
        authenticatorHealthCard.cancelAuthentication()
    }
}

@Composable
fun PromptScaffold(
    title: String,
    profile: ProfilesUseCaseData.Profile?,
    onCancel: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium),
        color = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(16.dp),
        elevation = 8.dp
    ) {
        Column(
            Modifier
                .padding(vertical = PaddingDefaults.Medium)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                profile?.let {
                    Avatar(
                        avatarModifier = Modifier.size(36.dp),
                        emptyIcon = Icons.Rounded.PersonOutline,
                        iconModifier = Modifier.size(20.dp),
                        profile = profile,
                        ssoStatusColor = null
                    )
                    SpacerMedium()
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            style = AppTheme.typography.h6,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text(
                            profile.insuranceInformation.insuranceIdentifier,
                            style = AppTheme.typography.body2l
                        )
                    }
                }
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cdw_nfc_dlg_cancel))
                }
            }
            SpacerLarge()
            content()
        }
    }
}

@Composable
fun rememberAuthenticator(intentHandler: IntentHandler): Authenticator {
    val bridge by rememberViewModel<MiniCardWallViewModel>()
    val promptSE = rememberSecureHardwarePromptAuthenticator(bridge)
    val promptHC = rememberHealthCardPromptAuthenticator(bridge)
    val promptEX = rememberExternalPromptAuthenticator(bridge, intentHandler)
    return remember {
        Authenticator(
            authenticatorSecureElement = promptSE,
            authenticatorHealthCard = promptHC,
            authenticatorExternal = promptEX,
            bridge = bridge
        )
    }
}
