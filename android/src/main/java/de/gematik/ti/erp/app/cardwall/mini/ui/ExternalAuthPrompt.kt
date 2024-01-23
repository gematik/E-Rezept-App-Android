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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.IntentHandler
import de.gematik.ti.erp.app.mainscreen.ui.ExternalAuthenticationDialog
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.Dialog
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import io.github.aakira.napier.Napier
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Stable
class ExternalPromptAuthenticator(
    private val intentHandler: IntentHandler,
    private val bridge: AuthenticationBridge
) : PromptAuthenticator {
    private sealed interface Request {
        object InsuranceSelected : Request
        object Cancel : Request
    }

    private val requestChannel = Channel<Request>(Channel.RENDEZVOUS)

    @Stable
    internal sealed interface State {
        object None : State
        data class SelectInsurance(val authenticatorName: String) : State
    }

    internal var state by mutableStateOf<State>(State.None)

    var profile by mutableStateOf<ProfilesUseCaseData.Profile?>(null)
        private set

    var isInProgress: Boolean = false
        private set

    override fun authenticate(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope
    ): Flow<PromptAuthenticator.AuthResult> = channelFlow {
        when (val authFor = bridge.authenticateFor(profileId)) {
            is AuthenticationBridge.External -> {
                state = State.SelectInsurance(authFor.authenticatorName)
                profile = authFor.profile

                requestChannel.receiveAsFlow().collectLatest {
                    when (it) {
                        Request.Cancel -> {
                            send(PromptAuthenticator.AuthResult.Cancelled)
                            cancel()
                        }

                        is Request.InsuranceSelected -> {
                            Napier.d("Fasttrack: doExternalAuthentication for $authFor")

                            bridge.doExternalAuthentication(
                                profileId = profileId,
                                scope = scope,
                                authenticatorId = authFor.authenticatorId,
                                authenticatorName = authFor.authenticatorName
                            ).onSuccess { redirect ->
                                intentHandler.startFastTrackApp(redirect)
                            }.onFailure {
                                Napier.e("doExternalAuthentication failed", it)
                                // TODO error handling
                                send(PromptAuthenticator.AuthResult.Cancelled)
                                cancel()
                            }

                            Napier.d("Fasttrack: wait for instant of $authFor")
                        }
                    }
                }
            }

            else -> {
                send(PromptAuthenticator.AuthResult.Cancelled)
            }
        }
    }.onStart {
        isInProgress = true
    }.onCompletion {
        isInProgress = false
        state = State.None
        profile = null
    }

    internal suspend fun onInsuranceSelected() {
        requestChannel.send(Request.InsuranceSelected)
    }

    internal suspend fun onCancel() {
        requestChannel.send(Request.Cancel)
    }

    override suspend fun cancelAuthentication() {
        requestChannel.send(Request.Cancel)
    }
}

@Composable
fun ExternalAuthPrompt(
    authenticator: ExternalPromptAuthenticator
) {
    val scope = rememberCoroutineScope()
    val state = authenticator.state
    val profile = authenticator.profile

    if (state is ExternalPromptAuthenticator.State.SelectInsurance) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                Modifier
                    .semantics(false) { }
                    .fillMaxSize()
                    .background(SolidColor(Color.Black), alpha = 0.5f)
                    .imePadding()
                    .systemBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                PromptScaffold(
                    title = stringResource(R.string.cdw_fasttrack_choose_insurance),
                    profile = profile,
                    onCancel = {
                        scope.launch {
                            authenticator.onCancel()
                        }
                    }
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PaddingDefaults.Medium)
                    ) {
                        OutlinedTextField(
                            value = state.authenticatorName,
                            onValueChange = {},
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Suche") },
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedLabelColor = AppTheme.colors.neutral400,
                                placeholderColor = AppTheme.colors.neutral400,
                                trailingIconColor = AppTheme.colors.neutral400
                            ),
                            readOnly = true
                        )
                        SpacerMedium()
                        PrimaryButtonSmall(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            onClick = {
                                scope.launch {
                                    authenticator.onInsuranceSelected()
                                }
                            }
                        ) {
                            Text(stringResource(R.string.mini_cdw_fasttrack_next))
                        }
                    }
                }
            }
        }
        ExternalAuthenticationDialog()
    }
}

@Composable
fun rememberExternalPromptAuthenticator(
    bridge: AuthenticationBridge,
    intentHandler: IntentHandler
): ExternalPromptAuthenticator {
    return remember {
        ExternalPromptAuthenticator(intentHandler, bridge)
    }
}
