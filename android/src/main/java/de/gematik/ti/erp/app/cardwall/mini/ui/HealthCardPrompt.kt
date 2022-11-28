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

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.NfcNotEnabledException
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.ui.ReadingCardAnimation
import de.gematik.ti.erp.app.cardwall.ui.SearchingCardAnimation
import de.gematik.ti.erp.app.cardwall.ui.TagLostCard
import de.gematik.ti.erp.app.cardwall.ui.pinRetriesLeft
import de.gematik.ti.erp.app.cardwall.ui.toAnnotatedString
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.Dialog
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Stable
class HealthCardPromptAuthenticator(
    val activity: MainActivity,
    private val bridge: AuthenticationBridge
) : PromptAuthenticator {
    private sealed interface Request {
        class CredentialsEntered(val pin: String) : Request
        object Cancel : Request
    }

    private val requestChannel = Channel<Request>(Channel.RENDEZVOUS)

    internal sealed interface State {
        object None : State
        object EnterCredentials : State

        sealed interface ReadState : State {
            object Searching : ReadState

            sealed interface Reading : ReadState {
                object Reading00 : Reading
                object Reading25 : Reading
                object Reading50 : Reading
                object Reading75 : Reading
                object Success : Reading
            }

            sealed interface Error : ReadState {
                object NfcDisabled : Error
                object TagLost : Error
                object RemoteCommunicationFailed : Error
                object CardAccessNumberWrong : Error
                class PersonalIdentificationWrong(val retriesLeft: Int) : Error
                object HealthCardBlocked : Error
                object RemoteCommunicationInvalidCertificate : Error
                object RemoteCommunicationInvalidOCSP : Error
            }
        }
    }

    internal var state by mutableStateOf<State>(State.None)
        private set

    var profile by mutableStateOf<ProfilesUseCaseData.Profile?>(null)
        private set

    private val tagFlow = activity.nfcTagFlow
        .filter {
            // only let interrupted communications through
            !(state is State.ReadState.Error && state!=State.ReadState.Error.TagLost)
        }

    override fun authenticate(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope
    ): Flow<PromptAuthenticator.AuthResult> = channelFlow {
        val requestChannelFlow = requestChannel.receiveAsFlow()

        when (val authFor = bridge.authenticateFor(profileId)) {
            is AuthenticationBridge.HealthCard -> {
                state = State.EnterCredentials
                profile = authFor.profile

                requestChannelFlow.collectLatest { req ->
                    when (req) {
                        Request.Cancel -> {
                            send(PromptAuthenticator.AuthResult.Cancelled)
                            cancel()
                        }

                        is Request.CredentialsEntered -> {
                            state = State.ReadState.Searching

                            tagFlow
                                .catch {
                                    if (it is NfcNotEnabledException) {
                                        state = State.ReadState.Error.NfcDisabled
                                    }
                                }
                                .collectLatest { tag ->
                                    bridge.doHealthCardAuthentication(
                                        profileId = profileId,
                                        scope = scope,
                                        can = authFor.can,
                                        pin = req.pin,
                                        tag = tag
                                    ).collect {
                                        it.emitAuthState()
                                        if (it.isFinal()) {
                                            send(PromptAuthenticator.AuthResult.Authenticated)
                                            cancel()
                                        }
                                    }
                                }
                        }
                    }
                }
            }

            else -> {
                send(PromptAuthenticator.AuthResult.Cancelled)
            }
        }
    }.onCompletion {
        state = State.None
        profile = null
    }

    private fun AuthenticationState.emitAuthState() {
        when {
            isInProgress() -> {
                when (this) {
                    AuthenticationState.HealthCardCommunicationChannelReady ->
                        state = State.ReadState.Reading.Reading00

                    AuthenticationState.HealthCardCommunicationTrustedChannelEstablished ->
                        state = State.ReadState.Reading.Reading25

                    AuthenticationState.HealthCardCommunicationFinished ->
                        state = State.ReadState.Reading.Reading50

                    AuthenticationState.IDPCommunicationFinished ->
                        state = State.ReadState.Reading.Reading75

                    else -> {}
                }
            }

            isFailure() -> {
                state = when (this) {
                    AuthenticationState.HealthCardCommunicationInterrupted ->
                        State.ReadState.Error.TagLost

                    AuthenticationState.HealthCardCardAccessNumberWrong ->
                        State.ReadState.Error.CardAccessNumberWrong

                    AuthenticationState.HealthCardPin2RetriesLeft ->
                        State.ReadState.Error.PersonalIdentificationWrong(2)

                    AuthenticationState.HealthCardPin1RetryLeft ->
                        State.ReadState.Error.PersonalIdentificationWrong(1)

                    AuthenticationState.HealthCardBlocked ->
                        State.ReadState.Error.HealthCardBlocked

                    AuthenticationState.IDPCommunicationFailed ->
                        State.ReadState.Error.RemoteCommunicationFailed

                    AuthenticationState.IDPCommunicationInvalidCertificate ->
                        State.ReadState.Error.RemoteCommunicationInvalidCertificate

                    AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate ->
                        State.ReadState.Error.RemoteCommunicationInvalidOCSP

                    else ->
                        State.ReadState.Error.TagLost
                }
            }
        }
    }

    override suspend fun cancelAuthentication() {
        requestChannel.trySend(Request.Cancel)
    }

    internal suspend fun onCancel() {
        requestChannel.send(Request.Cancel)
    }

    internal suspend fun onCredentialsEntered(pin: String) {
        requestChannel.send(Request.CredentialsEntered(pin))
    }
}

@Composable
fun rememberHealthCardPromptAuthenticator(
    bridge: AuthenticationBridge
): HealthCardPromptAuthenticator {
    val activity = LocalContext.current as MainActivity
    return remember {
        HealthCardPromptAuthenticator(activity, bridge)
    }
}

@Composable
fun HealthCardPrompt(
    authenticator: HealthCardPromptAuthenticator
) {
    val scope = rememberCoroutineScope()
    val state = authenticator.state
    val profile = authenticator.profile

    val isError = state is HealthCardPromptAuthenticator.State.ReadState.Error
    val isTagLost = state is HealthCardPromptAuthenticator.State.ReadState.Error.TagLost

    if (state != HealthCardPromptAuthenticator.State.None && (!isError || isTagLost)) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                Modifier
                    .semantics(false) { }
                    .fillMaxSize()
                    .background(SolidColor(Color.Black), alpha = 0.5f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .systemBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                PromptScaffold(
                    title = stringResource(R.string.mini_cdw_title),
                    profile = profile,
                    onCancel = {
                        scope.launch {
                            authenticator.onCancel()
                        }
                    }
                ) {
                    when (state) {
                        HealthCardPromptAuthenticator.State.EnterCredentials ->
                            HealthCardCredentials(
                                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                                onNext = {
                                    scope.launch {
                                        authenticator.onCredentialsEntered(it)
                                    }
                                }
                            )

                        is HealthCardPromptAuthenticator.State.ReadState ->
                            HealthCardAnimation(
                                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                                state = state
                            )

                        else -> {}
                    }
                }
            }
        }
    }
    if (isError) {
        HealthCardErrorDialog(
            state = state as HealthCardPromptAuthenticator.State.ReadState.Error,
            onCancel = {
                scope.launch {
                    authenticator.onCancel()
                }
            },
            onEnableNfc = {
                scope.launch(Dispatchers.Main) {
                    authenticator.activity.startActivity(
                        Intent(Settings.ACTION_NFC_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    authenticator.onCancel()
                }
            }
        )
    }
}

private val PinRegex = """^\d{0,8}$""".toRegex()
private val PinCorrectRegex = """^\d{6,8}$""".toRegex()

@Composable
private fun HealthCardCredentials(
    modifier: Modifier,
    onNext: (pin: String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    val pinCorrect by derivedStateOf { pin.matches(PinCorrectRegex) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)
    ) {
        Text(
            stringResource(R.string.mini_cdw_intro_description),
            style = AppTheme.typography.body2l
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = pin,
            onValueChange = {
                if (it.matches(PinRegex)) {
                    pin = it
                }
            },
            label = { Text(stringResource(R.string.mini_cdw_pin_input_label)) },
            placeholder = { Text(stringResource(R.string.mini_cdw_pin_input_placeholder)) },
            visualTransformation = if (pinVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                keyboardType = KeyboardType.NumberPassword
            ),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedLabelColor = AppTheme.colors.neutral400,
                placeholderColor = AppTheme.colors.neutral400,
                trailingIconColor = AppTheme.colors.neutral400
            ),
            keyboardActions = KeyboardActions {
                onNext(pin)
            },
            trailingIcon = {
                IconToggleButton(
                    checked = pinVisible,
                    onCheckedChange = { pinVisible = it }
                ) {
                    Icon(
                        if (pinVisible) {
                            Icons.Rounded.Visibility
                        } else {
                            Icons.Rounded.VisibilityOff
                        },
                        null
                    )
                }
            }
        )
        PrimaryButton(
            onClick = { onNext(pin) },
            enabled = pinCorrect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.mini_cdw_pin_next))
        }
    }
}

private const val InfoTextRoundTime = 5000L

@Composable
private fun HealthCardAnimation(
    modifier: Modifier,
    state: HealthCardPromptAuthenticator.State.ReadState
) {
    Column(
        modifier = modifier
            .padding(PaddingDefaults.Large)
            .wrapContentSize()
            .testTag("cdw_auth_nfc_bottom_sheet"),
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .defaultMinSize(minHeight = 150.dp)
                .fillMaxWidth()
        ) {
            when (state) {
                HealthCardPromptAuthenticator.State.ReadState.Searching -> SearchingCardAnimation()
                is HealthCardPromptAuthenticator.State.ReadState.Reading -> ReadingCardAnimation()
                is HealthCardPromptAuthenticator.State.ReadState.Error -> TagLostCard()
            }
        }

        // how to hold your card
        val rotatingScanCardAssistance = listOf(
            Pair(
                stringResource(R.string.cdw_nfc_search1_headline),
                stringResource(R.string.cdw_nfc_search1_info)
            ),
            Pair(
                stringResource(R.string.cdw_nfc_search2_headline),
                stringResource(R.string.cdw_nfc_search2_info)
            ),
            Pair(
                stringResource(R.string.cdw_nfc_search3_headline),
                stringResource(R.string.cdw_nfc_search3_info)
            )
        )

        var info by remember { mutableStateOf(rotatingScanCardAssistance.first()) }

        LaunchedEffect(Unit) {
            while (true) {
                snapshotFlow { state }
                    .first {
                        state is HealthCardPromptAuthenticator.State.ReadState.Searching
                    }

                var i = 0
                while (state is HealthCardPromptAuthenticator.State.ReadState.Searching) {
                    info = rotatingScanCardAssistance[i]

                    i = if (i < rotatingScanCardAssistance.size - 1) {
                        i + 1
                    } else {
                        0
                    }

                    delay(InfoTextRoundTime)
                }
            }
        }

        info = when (state) {
            HealthCardPromptAuthenticator.State.ReadState.Reading.Reading00 -> Pair(
                stringResource(R.string.cdw_nfc_found_headline),
                stringResource(R.string.cdw_nfc_found_info)
            )

            HealthCardPromptAuthenticator.State.ReadState.Reading.Reading25 -> Pair(
                stringResource(R.string.cdw_nfc_communication_headline_trusted_channel_established),
                stringResource(R.string.cdw_nfc_communication_info)
            )

            HealthCardPromptAuthenticator.State.ReadState.Reading.Reading50 -> Pair(
                stringResource(R.string.cdw_nfc_communication_headline_certificate_loaded),
                stringResource(R.string.cdw_nfc_communication_info)
            )

            HealthCardPromptAuthenticator.State.ReadState.Reading.Reading75 -> Pair(
                stringResource(R.string.cdw_nfc_communication_headline_pin_verified),
                stringResource(R.string.cdw_nfc_communication_info)
            )

            HealthCardPromptAuthenticator.State.ReadState.Reading.Success -> Pair(
                stringResource(R.string.cdw_nfc_communication_headline_challenge_signed),
                stringResource(R.string.cdw_nfc_communication_info)
            )

            HealthCardPromptAuthenticator.State.ReadState.Error.TagLost -> Pair(
                stringResource(R.string.cdw_nfc_tag_lost_headline),
                stringResource(R.string.cdw_nfc_tag_lost_info)
            )

            else -> info
        }

        Text(
            info.first,
            style = AppTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            info.second,
            style = AppTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HealthCardErrorDialog(
    state: HealthCardPromptAuthenticator.State.ReadState.Error,
    onCancel: () -> Unit,
    onEnableNfc: () -> Unit
) {
    if (state == HealthCardPromptAuthenticator.State.ReadState.Error.NfcDisabled) {
        CommonAlertDialog(
            header = stringResource(R.string.cdw_enable_nfc_header),
            info = stringResource(R.string.cdw_enable_nfc_info),
            cancelText = stringResource(R.string.cancel),
            actionText = stringResource(R.string.cdw_enable_nfc_btn_text),
            onCancel = onCancel,
            onClickAction = onEnableNfc
        )
    } else {
        val retryText = when (state) {
            HealthCardPromptAuthenticator.State.ReadState.Error.RemoteCommunicationFailed -> Pair(
                stringResource(R.string.cdw_nfc_intro_step1_header_on_error).toAnnotatedString(),
                stringResource(R.string.cdw_idp_error_time_and_connection).toAnnotatedString()
            )

            HealthCardPromptAuthenticator.State.ReadState.Error.RemoteCommunicationInvalidCertificate -> Pair(
                stringResource(R.string.cdw_nfc_error_title_invalid_certificate).toAnnotatedString(),
                stringResource(R.string.cdw_nfc_error_body_invalid_certificate).toAnnotatedString()
            )

            HealthCardPromptAuthenticator.State.ReadState.Error.RemoteCommunicationInvalidOCSP -> Pair(
                stringResource(R.string.cdw_nfc_error_title_invalid_ocsp_response_of_health_card_certificate)
                    .toAnnotatedString(),
                stringResource(R.string.cdw_nfc_error_body_invalid_ocsp_response_of_health_card_certificate)
                    .toAnnotatedString()
            )

            HealthCardPromptAuthenticator.State.ReadState.Error.CardAccessNumberWrong -> Pair(
                stringResource(R.string.cdw_nfc_intro_step2_header_on_can_error).toAnnotatedString(),
                stringResource(R.string.cdw_nfc_intro_step2_info_on_can_error).toAnnotatedString()
            )

            is HealthCardPromptAuthenticator.State.ReadState.Error.PersonalIdentificationWrong -> Pair(
                stringResource(R.string.cdw_nfc_intro_step2_header_on_pin_error).toAnnotatedString(),
                pinRetriesLeft(state.retriesLeft)
            )

            HealthCardPromptAuthenticator.State.ReadState.Error.HealthCardBlocked -> Pair(
                stringResource(R.string.cdw_header_on_card_blocked).toAnnotatedString(),
                stringResource(R.string.cdw_info_on_card_blocked).toAnnotatedString()
            )

            else -> null
        }

        retryText?.let { (title, message) ->

            AcceptDialog(
                header = title,
                info = message,
                acceptText = stringResource(R.string.ok),
                onClickAccept = onCancel
            )
        }
    }
}
