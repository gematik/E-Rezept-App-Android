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

package de.gematik.ti.erp.app.cardunlock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.NfcNotEnabledException
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.usecase.UnlockEgkState
import de.gematik.ti.erp.app.cardwall.ui.CardAnimationBox
import de.gematik.ti.erp.app.cardwall.ui.EnableNfcDialog
import de.gematik.ti.erp.app.cardwall.ui.ErrorDialog
import de.gematik.ti.erp.app.cardwall.ui.InfoText
import de.gematik.ti.erp.app.cardwall.ui.pinRetriesLeft
import de.gematik.ti.erp.app.cardwall.ui.rotatingScanCardAssistance
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.settings.ui.buildFeedbackBodyWithDeviceInfo
import de.gematik.ti.erp.app.settings.ui.openMailClient
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.Dialog
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.toAnnotatedString
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

@Stable
class UnlockEgkDialogState {
    val toggleUnlock = MutableSharedFlow<ToggleUnlock>()

    suspend fun show() {
        toggleUnlock.emit(ToggleUnlock.ToggleByUser(true))
    }
}

@Composable
fun rememberUnlockEgkDialogState(): UnlockEgkDialogState {
    return remember { UnlockEgkDialogState() }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("LongMethod", "LongParameterList")
@Composable
fun UnlockEgkDialog(
    unlockMethod: UnlockMethod,
    dialogState: UnlockEgkDialogState,
    unlockEgkController: UnlockEgkController,
    cardAccessNumber: String,
    personalUnblockingKey: String,
    oldSecret: String,
    newSecret: String,
    buildConfig: BuildConfigInformation,
    onClickTroubleshooting: (() -> Unit)? = null,
    troubleShootingEnabled: Boolean = false,
    onRetryCan: () -> Unit,
    onRetryOldSecret: () -> Unit,
    onRetryPuk: () -> Unit,
    onFinishUnlock: () -> Unit,
    onAssignPin: () -> Unit
) {
    val activity = LocalActivity.current as MainActivity
    val coroutineScope = rememberCoroutineScope()
    val toggleUnlock = dialogState.toggleUnlock

    var showEnableNfcDialog by remember { mutableStateOf(false) }
    var errorCount by remember(troubleShootingEnabled) { mutableStateOf(0) }
    var showCardCommunicationDialog by remember { mutableStateOf(false) }

    val state by produceState(initialValue = UnlockEgkState.None) {
        toggleUnlock.transformLatest {
            emit(UnlockEgkState.None)
            when (it) {
                is ToggleUnlock.ToggleByUser -> {
                    if (it.value) {
                        showCardCommunicationDialog = true
                        emitAll(
                            unlockEgkController.unlockEgk(
                                unlockMethod = unlockMethod,
                                can = cardAccessNumber,
                                puk = personalUnblockingKey,
                                oldSecret = oldSecret,
                                newSecret = newSecret,
                                tag = activity
                                    .nfcTagFlow
                                    .catch {
                                        if (it is NfcNotEnabledException) {
                                            showEnableNfcDialog = true
                                        }
                                    }
                            )
                        )
                    } else {
                        value = UnlockEgkState.None
                    }
                }

                is ToggleUnlock.ToggleByHealthCard -> {
                    val collectedOnce = AtomicBoolean(false)
                    val tagFlow = flow {
                        if (collectedOnce.get()) {
                            activity.nfcTagFlow.collect { tag ->
                                emit(tag)
                            }
                        } else {
                            collectedOnce.set(true)
                            emit(it.tag)
                        }
                    }
                    emitAll(
                        unlockEgkController.unlockEgk(
                            unlockMethod = unlockMethod,
                            can = cardAccessNumber,
                            puk = personalUnblockingKey,
                            oldSecret = oldSecret,
                            newSecret = newSecret,
                            tagFlow
                        )
                    )
                }
            }
        }.catch {
            Napier.e("Something unforeseen happened", it)
            emit(UnlockEgkState.HealthCardCommunicationInterrupted)
            delay(1000)
        }.onCompletion { cause ->
            if (cause is CancellationException) {
                value = UnlockEgkState.None
            }
        }.collect {
            errorCount += if (it == UnlockEgkState.HealthCardCommunicationInterrupted) 1 else 0
            value = it
        }
    }

    LaunchedEffect(Unit) {
        activity.nfcTagFlow
            .retryWhen { cause, _ ->
                cause !is NfcNotEnabledException
            }
            .catch { cause ->
                if (cause is NfcNotEnabledException) {
                    showEnableNfcDialog = true
                }
            }
            .filter {
                !(state.isFailure() && state != UnlockEgkState.HealthCardCommunicationInterrupted)
            }
            .collect {
                toggleUnlock.emit(ToggleUnlock.ToggleByHealthCard(it))
            }
    }

    LaunchedEffect(state) {
        when {
            state.isInProgress() -> showCardCommunicationDialog = true
            state.isReady() -> showCardCommunicationDialog = false
        }
    }

    if (showCardCommunicationDialog) {
        CardCommunicationDialog(
            state,
            onCancel = {
                coroutineScope.launch { toggleUnlock.emit(ToggleUnlock.ToggleByUser(false)) }
            },
            showTroubleshooting = troubleShootingEnabled && errorCount > 2 && !state.isInProgress(),
            onClickTroubleshooting = onClickTroubleshooting
        )
    }

    if (showEnableNfcDialog) {
        EnableNfcDialog {
            showEnableNfcDialog = false
        }
    }

    val nextText = nextTextFromUnlockEgkState(state)

    val resumeText = resumeTextFromUnlockEgkState(unlockMethod, state)

    resumeText?.let {
        ResumeDialog(
            state = state,
            unlockMethod = unlockMethod,
            resumeText = it,
            onFinishUnlock = onFinishUnlock,
            nextText = nextText,
            buildConfig = buildConfig,
            onToggleUnlock = {
                coroutineScope.launch {
                    toggleUnlock.emit(ToggleUnlock.ToggleByUser(it))
                }
            },
            onRetryOldSecret = onRetryOldSecret,
            onRetryCan = onRetryCan,
            onRetryPuk = onRetryPuk,
            onAssignPin = onAssignPin
        )
    }
}

@Composable
private fun nextTextFromUnlockEgkState(state: UnlockEgkState): String {
    val nextText = when (state) {
        UnlockEgkState.HealthCardCardAccessNumberWrong,
        UnlockEgkState.HealthCardPinRetriesLeft,
        UnlockEgkState.HealthCardPukRetriesLeft -> stringResource(R.string.cdw_auth_retry_pin_can)

        UnlockEgkState.HealthCardCommunicationFinished,
        UnlockEgkState.HealthCardPasswordBlocked,
        UnlockEgkState.HealthCardPukBlocked -> stringResource(R.string.unlock_egk_finished_ok)

        UnlockEgkState.MemoryFailure,
        UnlockEgkState.SecurityStatusNotSatisfied,
        UnlockEgkState.PasswordNotFound -> stringResource(R.string.unlock_egk_report_error)

        UnlockEgkState.PasswordNotUsable -> stringResource(R.string.unlock_egk_assign_pin)

        else -> stringResource(R.string.cdw_auth_retry)
    }
    return nextText
}

@Composable
private fun resumeTextFromUnlockEgkState(
    unlockMethod: UnlockMethod,
    state: UnlockEgkState
): Pair<AnnotatedString, AnnotatedString>? {
    val resumeText = when (state) {
        UnlockEgkState.HealthCardCommunicationFinished -> Pair(
            if (unlockMethod == UnlockMethod.ChangeReferenceData ||
                unlockMethod == UnlockMethod.ResetRetryCounterWithNewSecret
            ) {
                stringResource(R.string.unlock_egk_dialog_new_secret_saved).toAnnotatedString()
            } else {
                stringResource(R.string.unlock_egk_unlock_success_header).toAnnotatedString()
            },
            if (unlockMethod == UnlockMethod.ChangeReferenceData) {
                "".toAnnotatedString()
            } else {
                stringResource(R.string.unlock_egk_unlock_success_info).toAnnotatedString()
            }
        )

        UnlockEgkState.HealthCardCardAccessNumberWrong -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_can_error_alert).toAnnotatedString(),
            stringResource(R.string.cdw_nfc_intro_step2_info_on_can_error).toAnnotatedString()
        )

        UnlockEgkState.HealthCardPukRetriesLeft -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_puk_error_alert).toAnnotatedString(),
            pukRetriesLeft(state.retriesLeft)
        )

        UnlockEgkState.HealthCardPinRetriesLeft -> Pair(
            stringResource(R.string.unlock_egk_wrong_pin).toAnnotatedString(),
            pinRetriesLeft(state.retriesLeft)
        )

        UnlockEgkState.HealthCardPasswordBlocked -> Pair(
            stringResource(R.string.unlock_egk_password_blocked).toAnnotatedString(),
            stringResource(R.string.unlock_egk_password_blocked_info).toAnnotatedString()
        )

        UnlockEgkState.HealthCardPukBlocked -> Pair(
            stringResource(R.string.unlock_not_possible_header).toAnnotatedString(),
            stringResource(R.string.unlock_not_possible_info).toAnnotatedString()
        )

        UnlockEgkState.MemoryFailure -> Pair(
            stringResource(R.string.unlock_memory_failure_header).toAnnotatedString(),
            stringResource(R.string.unlock_memory_failure_info).toAnnotatedString()
        )

        UnlockEgkState.SecurityStatusNotSatisfied -> Pair(
            stringResource(R.string.unlock_security_status_not_satisfied_header).toAnnotatedString(),
            stringResource(R.string.unlock_security_status_not_satisfied_info).toAnnotatedString()
        )

        UnlockEgkState.PasswordNotUsable -> Pair(
            stringResource(R.string.unlock_password_not_usable_header).toAnnotatedString(),
            stringResource(R.string.unlock_password_not_usable_info).toAnnotatedString()
        )

        UnlockEgkState.PasswordNotFound -> Pair(
            stringResource(R.string.unlock_password_not_found_header).toAnnotatedString(),
            stringResource(R.string.unlock_password_not_found_info).toAnnotatedString()
        )

        else -> null
    }
    return resumeText
}

@Composable
private fun ResumeDialog(
    state: UnlockEgkState,
    unlockMethod: UnlockMethod,
    resumeText: Pair<AnnotatedString, AnnotatedString>,
    onFinishUnlock: () -> Unit,
    nextText: String,
    onToggleUnlock: (Boolean) -> Unit,
    onRetryCan: () -> Unit,
    onRetryOldSecret: () -> Unit,
    onRetryPuk: () -> Unit,
    onAssignPin: () -> Unit,
    buildConfig: BuildConfigInformation
) {
    val context = LocalContext.current
    val mailAddress = stringResource(R.string.settings_contact_mail_address)
    val subject = stringResource(R.string.settings_feedback_mail_subject)
    val body = buildFeedbackBodyWithDeviceInfo(
        darkMode = buildConfig.inDarkTheme(),
        language = buildConfig.language(),
        versionName = buildConfig.versionName(),
        nfcInfo = buildConfig.nfcInformation(context),
        phoneModel = buildConfig.model(),
        errorState = remember(key1 = state.name) { state.name }
    )

    when (state) {
        UnlockEgkState.HealthCardCommunicationFinished,
        UnlockEgkState.HealthCardPasswordBlocked,
        UnlockEgkState.HealthCardPukBlocked ->
            AcceptDialog(
                header = resumeText.first,
                info = resumeText.second,
                acceptText = stringResource(R.string.unlock_egk_finished_ok),
                onClickAccept = { onFinishUnlock() }
            )

        else -> {
            ErrorDialog(
                header = resumeText.first,
                info = resumeText.second,
                retryButtonText = nextText,
                onCancel = {
                    // don't retry
                    onToggleUnlock(false)
                },
                onRetry = {
                    if (unlockMethod == UnlockMethod.ChangeReferenceData) {
                        when (state) {
                            UnlockEgkState.HealthCardCardAccessNumberWrong -> onRetryCan()
                            UnlockEgkState.HealthCardPinRetriesLeft -> onRetryOldSecret()
                            UnlockEgkState.PasswordNotFound,
                            UnlockEgkState.SecurityStatusNotSatisfied,
                            UnlockEgkState.MemoryFailure -> openMailClient(context, mailAddress, body, subject)

                            UnlockEgkState.PasswordNotUsable -> onAssignPin()
                            // retry
                            else -> onToggleUnlock(true)
                        }
                    } else {
                        when (state) {
                            UnlockEgkState.HealthCardCardAccessNumberWrong -> onRetryCan()
                            UnlockEgkState.HealthCardPukRetriesLeft -> onRetryPuk()
                            // retry
                            else -> onToggleUnlock(true)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CardCommunicationDialog(
    state: UnlockEgkState,
    onCancel: () -> Unit,
    showTroubleshooting: Boolean,
    onClickTroubleshooting: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = { onCancel() },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(SolidColor(Color.Black), alpha = 0.5f)
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
                        UnlockEgkState.None,
                        UnlockEgkState.UnlockFlowInitialized -> 0

                        UnlockEgkState.HealthCardCommunicationChannelReady,
                        UnlockEgkState.HealthCardCommunicationTrustedChannelEstablished,
                        UnlockEgkState.HealthCardCommunicationFinished -> 1

                        else -> 2
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .wrapContentSize()
                        .testTag("cdw_unlock_egk_bottom_sheet"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.unlock_egk_dialog_cancel).uppercase(Locale.getDefault()))
                    }

                    CardAnimationBox(screen)

                    // how to hold your card
                    val rotatingScanCardAssistance = rotatingScanCardAssistance()

                    var info by remember { mutableStateOf(rotatingScanCardAssistance.first()) }

                    LaunchedEffect(state) {
                        if (state == UnlockEgkState.UnlockFlowInitialized) {
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

                    info = when (state) {
                        UnlockEgkState.HealthCardCommunicationChannelReady -> Pair(
                            stringResource(R.string.cdw_nfc_found_headline),
                            stringResource(R.string.cdw_nfc_found_info)
                        )

                        UnlockEgkState.HealthCardCommunicationTrustedChannelEstablished -> Pair(
                            stringResource(R.string.cdw_nfc_communication_headline_certificate_loaded),
                            stringResource(R.string.cdw_nfc_communication_info)
                        )

                        UnlockEgkState.HealthCardCommunicationFinished -> Pair(
                            stringResource(R.string.cdw_nfc_communication_headline_challenge_signed),
                            stringResource(R.string.cdw_nfc_communication_info)
                        )

                        UnlockEgkState.HealthCardCommunicationInterrupted -> Pair(
                            stringResource(R.string.cdw_nfc_tag_lost_headline),
                            stringResource(R.string.cdw_nfc_tag_lost_info)
                        )

                        else -> info
                    }

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
}

@Composable
fun pukRetriesLeft(count: Int) =
    annotatedPluralsResource(
        R.plurals.cdw_nfc_intro_step2_info_on_puk_error,
        count,
        buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(count.toString()) } }
    )
