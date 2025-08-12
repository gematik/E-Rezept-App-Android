/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.cardwall.ui.components

import android.content.Intent
import android.nfc.Tag
import android.provider.Settings
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.trackCardCommunication
import de.gematik.ti.erp.app.base.onNfcNotEnabled
import de.gematik.ti.erp.app.base.retryOnNfcEnabled
import de.gematik.ti.erp.app.cardwall.presentation.CardWallController
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.LocalCardCommunicationAnalytics
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.troubleshooting.ui.components.TroubleShootingInfo
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.toAnnotatedString
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException

@Stable
class CardWallAuthenticationDialogState {
    val toggleAuth = MutableSharedFlow<ToggleAuth>()

    suspend fun show() {
        toggleAuth.emit(ToggleAuth.ToggleByUser(true))
    }
}

@Composable
fun rememberCardWallAuthenticationDialogState(): CardWallAuthenticationDialogState {
    return remember { CardWallAuthenticationDialogState() }
}

@Suppress("LongMethod", "ComplexMethod")
@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
fun CardWallAuthenticationDialog(
    dialogState: CardWallAuthenticationDialogState = rememberCardWallAuthenticationDialogState(),
    cardWallController: CardWallController,
    authenticationData: CardWallAuthenticationData,
    profileId: ProfileIdentifier,
    troubleShootingEnabled: Boolean = false,
    allowUserCancellation: Boolean = true,
    onFinal: () -> Unit,
    onRetryCan: () -> Unit,
    onRetryPin: () -> Unit,
    onUnlockEgk: () -> Unit,
    onClickTroubleshooting: (() -> Unit)? = null,
    onStateChange: ((AuthenticationState) -> Unit)? = null
) {
    val activity = LocalActivity.current as MainActivity
    val coroutineScope = rememberCoroutineScope()
    val toggleAuth = dialogState.toggleAuth
    var showEnableNfcDialog by remember { mutableStateOf(false) }
    var errorCount by remember(troubleShootingEnabled) { mutableStateOf(0) }

    val tracker = LocalCardCommunicationAnalytics.current

    var cancelEnabled by remember { mutableStateOf(true) }
    val state by produceState<AuthenticationState>(initialValue = AuthenticationState.None) {
        toggleAuth.transformLatest {
            emit(AuthenticationState.None)
            cancelEnabled = true
            when (it) {
                is ToggleAuth.ToggleByUser -> {
                    if (it.value) {
                        emitAll(
                            cardWallController.doAuthentication(
                                profileId = profileId,
                                authenticationData = authenticationData,
                                activity
                                    .nfcTagFlow
                                    .onNfcNotEnabled {
                                        showEnableNfcDialog = true
                                    }
                            )
                        )
                    } else {
                        value = AuthenticationState.None
                    }
                }

                is ToggleAuth.ToggleByHealthCard -> {
                    val collectedOnce = AtomicBoolean(false)
                    val tagFlow = flow {
                        if (collectedOnce.get()) {
                            activity.nfcTagFlow.collect {
                                emit(it)
                            }
                        } else {
                            collectedOnce.set(true)
                            emit(it.tag)
                        }
                    }
                    emitAll(
                        cardWallController.doAuthentication(
                            profileId = profileId,
                            authenticationData = authenticationData,
                            tagFlow
                        )
                    )
                }
            }
        }.catch {
            Napier.e("Something unforeseen happened", it)
            // if this happens we can't recover from here
            emit(AuthenticationState.HealthCardCommunicationInterrupted)
            delay(1000)
        }.onCompletion { cause ->
            if (cause is CancellationException) {
                value = AuthenticationState.None
            }
        }.collect {
            errorCount += if (it == AuthenticationState.HealthCardCommunicationInterrupted) 1 else 0
            value = it
            launch(Dispatchers.IO) { tracker.trackCardCommunication(it) }
        }
    }

    LaunchedEffect(Unit) {
        activity.nfcTagFlow
            .retryOnNfcEnabled()
            .onNfcNotEnabled {
                showEnableNfcDialog = true
            }
            .filter {
                // only let interrupted communications through
                !(state.isFailure() && state != AuthenticationState.HealthCardCommunicationInterrupted)
            }
            .collect {
                toggleAuth.emit(ToggleAuth.ToggleByHealthCard(it))
            }
    }

    var showAuthDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        onStateChange?.run { onStateChange(state) }

        when {
            state.isInProgress() -> showAuthDialog = true
            state.isReady() -> showAuthDialog = false
            state.isFinal() -> {
                tracker.trackIdentifiedWithIDP()
                onFinal()
            }
        }
    }

    if (showAuthDialog) {
        AuthenticationDialog(
            state = state,
            showTroubleshooting = troubleShootingEnabled && errorCount > 2 && !state.isInProgress(),
            onCancelEnabled = allowUserCancellation && cancelEnabled,
            onCancel = {
                coroutineScope.launch {
                    cancelEnabled = false
                    toggleAuth.emit(ToggleAuth.ToggleByUser(false))
                }
            },
            onClickTroubleshooting = onClickTroubleshooting
        )
    }

    val nextText = extractNextText(state)
    val retryText = extractRetryText(state)

    if (showEnableNfcDialog) {
        EnableNfcDialog {
            showEnableNfcDialog = false
        }
    }

    retryText?.let {
        ErrorDialog(
            header = it.first,
            info = it.second,
            retryButtonText = nextText,
            onCancel = {
                coroutineScope.launch { toggleAuth.emit(ToggleAuth.ToggleByUser(false)) }
            },
            onRetry = {
                when (state) {
                    AuthenticationState.HealthCardCardAccessNumberWrong -> onRetryCan()
                    AuthenticationState.HealthCardPin2RetriesLeft,
                    AuthenticationState.HealthCardPin1RetryLeft -> onRetryPin()

                    AuthenticationState.HealthCardBlocked -> onUnlockEgk()
                    else -> if (cardWallController.isNfcEnabled()) {
                        coroutineScope.launch {
                            toggleAuth.emit(ToggleAuth.ToggleByUser(true))
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun extractRetryText(state: AuthenticationState): Pair<AnnotatedString, AnnotatedString>? =
    when (val s = state) {
        AuthenticationState.IDPCommunicationFailed -> Pair(
            stringResource(R.string.cdw_nfc_intro_step1_header_on_error).toAnnotatedString(),
            stringResource(R.string.cdw_idp_error_time_and_connection).toAnnotatedString()
        )

        AuthenticationState.IDPCommunicationInvalidCertificate -> Pair(
            stringResource(R.string.cdw_nfc_error_title_invalid_certificate).toAnnotatedString(),
            stringResource(R.string.cdw_nfc_error_body_invalid_certificate).toAnnotatedString()
        )

        AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate -> Pair(
            stringResource(R.string.cdw_nfc_error_title_invalid_ocsp_response_of_health_card_certificate)
                .toAnnotatedString(),
            stringResource(R.string.cdw_nfc_error_body_invalid_ocsp_response_of_health_card_certificate)
                .toAnnotatedString()
        )

        AuthenticationState.HealthCardCardAccessNumberWrong -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_can_error_alert).toAnnotatedString(),
            stringResource(R.string.cdw_nfc_intro_step2_info_on_can_error).toAnnotatedString()
        )

        AuthenticationState.HealthCardPin2RetriesLeft -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_pin_error_alert).toAnnotatedString(),
            pinRetriesLeft(2)
        )

        AuthenticationState.HealthCardPin1RetryLeft -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_pin_error_alert).toAnnotatedString(),
            pinRetriesLeft(1)
        )

        AuthenticationState.HealthCardBlocked -> Pair(
            stringResource(R.string.cdw_header_on_card_blocked).toAnnotatedString(),
            stringResource(R.string.cdw_info_on_card_blocked).toAnnotatedString()
        )

        is AuthenticationState.InsuranceIdentifierAlreadyExists -> {
            Pair(
                stringResource(R.string.cdw_nfc_error_assign_title).toAnnotatedString(),
                if (s.inActiveProfile) {
                    stringResource(R.string.cdw_nfc_error_assign_subtitle, s.insuranceIdentifier).toAnnotatedString()
                } else {
                    stringResource(R.string.cdw_nfc_error_assign_other_subtitle, s.profileName).toAnnotatedString()
                }
            )
        }

        else -> null
    }

@Composable
fun extractNextText(state: AuthenticationState): String =
    when (state) {
        AuthenticationState.HealthCardCardAccessNumberWrong -> stringResource(R.string.cdw_auth_retry_pin_can)
        AuthenticationState.HealthCardPin2RetriesLeft,
        AuthenticationState.HealthCardPin1RetryLeft -> stringResource(R.string.cdw_auth_retry_pin_can)

        AuthenticationState.HealthCardBlocked -> stringResource(R.string.cdw_auth_retry_unlock_egk)
        else -> stringResource(R.string.cdw_auth_retry)
    }

@Composable
fun EnableNfcDialog(onClickAction: () -> Unit = {}, onCancel: () -> Unit) {
    val context = LocalContext.current
    val header = stringResource(R.string.cdw_enable_nfc_header)
    val info = stringResource(R.string.cdw_enable_nfc_info)
    val enableNfcButtonText = stringResource(R.string.cdw_enable_nfc_btn_text)
    val cancelText = stringResource(R.string.cdw_enable_nfc_abort_button_text)

    CommonAlertDialog(
        header = header,
        info = info,
        cancelText = cancelText,
        actionText = enableNfcButtonText,
        onCancel = onCancel,
        onClickAction = {
            context.handleIntent(Intent(Settings.ACTION_NFC_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            onClickAction()
        }
    )
}

@Composable
fun ErrorDialog(
    header: AnnotatedString,
    info: AnnotatedString,
    retryButtonText: String,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) =
    CommonAlertDialog(
        header = header,
        info = info,
        onCancel = onCancel,
        onClickAction = onRetry,
        cancelText = stringResource(R.string.cancel),
        actionText = retryButtonText
    )

@Composable
fun pinRetriesLeft(count: Int) =
    annotatedPluralsResource(
        R.plurals.cdw_nfc_intro_step2_info_on_pin_error,
        count,
        buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(count.toString()) } }
    )

@ExperimentalAnimationApi
@Composable
private fun AuthenticationDialog(
    state: AuthenticationState,
    showTroubleshooting: Boolean,
    onCancelEnabled: Boolean,
    onCancel: () -> Unit,
    onClickTroubleshooting: (() -> Unit)? = null
) {
    Box(
        Modifier
            .testTag(TestTag.CardWall.Nfc.CardReadingDialog)
            .semantics(false) { }
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium),
            color = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(28.dp),
            elevation = 8.dp
        ) {
            val screen = remember(state) {
                when (state) {
                    AuthenticationState.AuthenticationFlowInitialized -> 0
                    AuthenticationState.HealthCardCommunicationChannelReady,
                    AuthenticationState.HealthCardCommunicationTrustedChannelEstablished,
                    AuthenticationState.HealthCardCommunicationCertificateLoaded,
                    AuthenticationState.HealthCardCommunicationFinished,
                    AuthenticationState.IDPCommunicationFinished,
                    AuthenticationState.AuthenticationFlowFinished -> 1

                    AuthenticationState.HealthCardCommunicationInterrupted -> 2
                    else -> 1
                }
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .wrapContentSize()
                    .testTag("cdw_auth_nfc_bottom_sheet"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    enabled = onCancelEnabled,
                    onClick = onCancel
                ) {
                    Text(stringResource(R.string.cdw_nfc_dlg_cancel).uppercase(Locale.getDefault()))
                }
                CardAnimationBox(screen)

                // how to hold your card
                val rotatingScanCardAssistance = rotatingScanCardAssistance()

                var info by remember { mutableStateOf(rotatingScanCardAssistance.first()) }

                LaunchedEffect(state) {
                    if (state == AuthenticationState.AuthenticationFlowInitialized) {
                        var i = 0
                        while (true) {
                            info = rotatingScanCardAssistance[i]

                            i = if (i < rotatingScanCardAssistance.size - 1) {
                                i + 1
                            } else {
                                0
                            }

                            delay(5000)
                        }
                    }
                }

                info = extractInfo(state) ?: info

                InfoText(
                    showTroubleshooting,
                    info,
                    onClickTroubleshooting = {
                        onClickTroubleshooting?.run { onClickTroubleshooting() }
                    }
                )
            }
        }
    }
}

@Composable
fun InfoText(showTroubleshooting: Boolean, info: Pair<String, String>, onClickTroubleshooting: () -> Unit?) {
    if (showTroubleshooting) {
        TroubleShootingInfo(
            onClick = { onClickTroubleshooting.run { onClickTroubleshooting() } }
        )
    } else {
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
fun extractInfo(state: AuthenticationState): Pair<String, String>? =
    when (state) {
        AuthenticationState.HealthCardCommunicationChannelReady -> Pair(
            stringResource(R.string.cdw_nfc_found_headline),
            stringResource(R.string.cdw_nfc_found_info)
        )

        AuthenticationState.HealthCardCommunicationTrustedChannelEstablished -> Pair(
            stringResource(R.string.cdw_nfc_communication_headline_trusted_channel_established),
            stringResource(R.string.cdw_nfc_communication_info)
        )

        AuthenticationState.HealthCardCommunicationFinished -> Pair(
            stringResource(R.string.cdw_nfc_communication_headline_certificate_loaded),
            stringResource(R.string.cdw_nfc_communication_info)
        )

        AuthenticationState.IDPCommunicationFinished -> Pair(
            stringResource(R.string.cdw_nfc_communication_headline_pin_verified),
            stringResource(R.string.cdw_nfc_communication_info)
        )

        AuthenticationState.AuthenticationFlowFinished -> Pair(
            stringResource(R.string.cdw_nfc_communication_headline_challenge_signed),
            stringResource(R.string.cdw_nfc_communication_info)
        )

        AuthenticationState.HealthCardCommunicationInterrupted -> Pair(
            stringResource(R.string.cdw_nfc_tag_lost_headline),
            stringResource(R.string.cdw_nfc_tag_lost_info)
        )

        else -> null
    }

sealed class ToggleAuth {
    data class ToggleByUser(val value: Boolean) : ToggleAuth()
    data class ToggleByHealthCard(val tag: Tag) : ToggleAuth()
}
