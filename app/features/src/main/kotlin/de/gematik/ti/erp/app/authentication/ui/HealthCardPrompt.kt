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

package de.gematik.ti.erp.app.authentication.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.gematik.ti.erp.app.cardwall.mini.ui.HealthCardPromptAuthenticator
import de.gematik.ti.erp.app.cardwall.ui.components.ReadingCardAnimation
import de.gematik.ti.erp.app.cardwall.ui.components.SearchingCardAnimation
import de.gematik.ti.erp.app.cardwall.ui.components.TagLostCard
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val InfoTextRoundTime = 5000L

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
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                Modifier
                    .semantics(false) { }
                    .fillMaxSize()
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
