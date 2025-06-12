/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.authentication.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.gematik.ti.erp.app.cardwall.mini.ui.ExternalPromptAuthenticator
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.mainscreen.ui.ExternalAuthenticationUiHandler
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.erezeptTextFieldColors
import kotlinx.coroutines.launch

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
                    .imePadding()
                    .systemBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                PromptScaffold(
                    title = stringResource(R.string.cardwall_gid_header),
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
                        ErezeptOutlineText(
                            value = state.authenticatorName,
                            onValueChange = {},
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.mini_cdw_fasttrack_search)) },
                            shape = RoundedCornerShape(8.dp),
                            readOnly = true,
                            colors = erezeptTextFieldColors()
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
        ExternalAuthenticationUiHandler()
    }
}
