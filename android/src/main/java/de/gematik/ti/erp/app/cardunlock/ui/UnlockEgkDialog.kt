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

package de.gematik.ti.erp.app.cardunlock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.systemBarsPadding
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.NfcNotEnabledException
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardunlock.usecase.UnlockEgkState
import de.gematik.ti.erp.app.cardwall.ui.CardAnimationBox
import de.gematik.ti.erp.app.cardwall.ui.EnableNfcDialog
import de.gematik.ti.erp.app.cardwall.ui.ErrorDialog
import de.gematik.ti.erp.app.cardwall.ui.Troubleshooting
import de.gematik.ti.erp.app.cardwall.ui.rotatingScanCardAssistance
import de.gematik.ti.erp.app.cardwall.ui.toAnnotatedString
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.Dialog
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import kotlinx.coroutines.CancellationException
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
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.retryWhen
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
@Suppress("LongMethod")
@Composable
fun UnlockEgkDialog(
    changeSecret: Boolean = false,
    dialogState: UnlockEgkDialogState,
    viewModel: UnlockEgkViewModel,
    cardAccessNumber: String,
    personalUnblockingKey: String,
    newSecret: String,
    onClickTroubleshooting: (() -> Unit)? = null,
    troubleShootingEnabled: Boolean = false,
    onRetryCan: () -> Unit,
    onRetryPuk: () -> Unit,
    onFinishUnlock: () -> Unit
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
                            viewModel.unlockEgk(
                                changeSecret = changeSecret,
                                can = cardAccessNumber,
                                puk = personalUnblockingKey,
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
                        viewModel.unlockEgk(
                            changeSecret = changeSecret,
                            can = cardAccessNumber,
                            puk = personalUnblockingKey,
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
        EnableNfcDialog(activity) {
            showEnableNfcDialog = false
        }
    }

    val nextText = nextTextFromUnlockEgkState(state)

    val resumeText = resumeTextFromUnlockEgkState(changeSecret, state)

    resumeText?.let {
        ResumeDialog(
            state = state,
            resumeText = it,
            onFinishUnlock = onFinishUnlock,
            nextText = nextText,
            onToggleUnlock = {
                coroutineScope.launch {
                    toggleUnlock.emit(ToggleUnlock.ToggleByUser(it))
                }
            },
            onRetryCan = onRetryCan,
            onRetryPuk = onRetryPuk
        )
    }
}

@Composable
private fun nextTextFromUnlockEgkState(state: UnlockEgkState): String {
    val nextText = when (state) {
        UnlockEgkState.HealthCardCardAccessNumberWrong,
        UnlockEgkState.HealthCardPukRetriesLeft -> stringResource(R.string.cdw_auth_retry_pin_can)
        UnlockEgkState.HealthCardCommunicationFinished,
        UnlockEgkState.HealthCardPukBlocked -> stringResource(R.string.unlock_egk_finished_ok)
        else -> stringResource(R.string.cdw_auth_retry)
    }
    return nextText
}

@Composable
private fun resumeTextFromUnlockEgkState(
    changeSecret: Boolean,
    state: UnlockEgkState
): Pair<AnnotatedString, AnnotatedString>? {
    val resumeText = when (state) {
        UnlockEgkState.HealthCardCommunicationFinished -> Pair(
            if (changeSecret) {
                stringResource(R.string.unlock_egk_dialog_new_secret_saved).toAnnotatedString()
            } else {
                stringResource(R.string.unlock_egk_unlock_success_header).toAnnotatedString()
            },
            stringResource(R.string.unlock_egk_unlock_success_info).toAnnotatedString()
        )
        UnlockEgkState.HealthCardCardAccessNumberWrong -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_can_error).toAnnotatedString(),
            stringResource(R.string.cdw_nfc_intro_step2_info_on_can_error).toAnnotatedString()
        )
        UnlockEgkState.HealthCardPukRetriesLeft -> Pair(
            stringResource(R.string.cdw_nfc_intro_step2_header_on_puk_error).toAnnotatedString(),
            pukRetriesLeft(state.pukRetriesLeft)
        )
        UnlockEgkState.HealthCardPukBlocked -> Pair(
            if (changeSecret) {
                stringResource(R.string.unlock_egk_dialog_saving_new_secret_not_possible).toAnnotatedString()
            } else {
                stringResource(R.string.unlock_not_possible_header).toAnnotatedString()
            },
            stringResource(R.string.unlock_not_possible_info).toAnnotatedString()
        )
        else -> null
    }
    return resumeText
}

@Composable
private fun ResumeDialog(
    state: UnlockEgkState,
    resumeText: Pair<AnnotatedString, AnnotatedString>,
    onFinishUnlock: () -> Unit,
    nextText: String,
    onToggleUnlock: (Boolean) -> Unit,
    onRetryCan: () -> Unit,
    onRetryPuk: () -> Unit
) {
    if (state == UnlockEgkState.HealthCardCommunicationFinished) {
        AcceptDialog(
            header = resumeText.first,
            info = resumeText.second,
            acceptText = stringResource(R.string.unlock_egk_finished_ok),
            onClickAccept = { onFinishUnlock() }
        )
    } else {
        ErrorDialog(
            header = resumeText.first,
            info = resumeText.second,
            retryButtonText = nextText,
            onCancel = {
                onToggleUnlock(false)
            },
            onRetry = {
                when (state) {
                    UnlockEgkState.HealthCardCardAccessNumberWrong -> onRetryCan()
                    UnlockEgkState.HealthCardPukRetriesLeft -> onRetryPuk()
                    UnlockEgkState.HealthCardPukBlocked -> onFinishUnlock()
                    else -> onToggleUnlock(true)
                }
            }
        )
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
                    if (showTroubleshooting) {
                        Troubleshooting(
                            onClick = { onClickTroubleshooting?.run { onClickTroubleshooting() } }
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
