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

package de.gematik.ti.erp.app.utils.compose

import android.app.Dialog
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.base.ClipBoardCopy
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.ErezeptText.TextAlignment
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.capitalizeFirstChar

@Suppress("FunctionNaming")
fun ExceptionDialog(
    error: Throwable,
    dialogScaffold: DialogScaffold
) {
    if (BuildConfigExtension.isInternalDebug) {
        dialogScaffold.show {
            val scrollState = rememberScrollState()
            ExceptionDialogContent(scrollState, it, error)
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun ExceptionDialogContent(
    scrollState: ScrollState,
    dialog: Dialog,
    error: Throwable
) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            Row {
                ErezeptText.Title(
                    modifier = Modifier.weight(.2f)
                        .padding(PaddingDefaults.Medium),
                    textAlignment = TextAlignment.Center,
                    text = "${error.message?.capitalizeFirstChar()}"
                )
                IconButton(
                    modifier = Modifier.weight(.8f),
                    onClick = { dialog.dismiss() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
        },
        bottomBar = {
            Center(
                modifier = Modifier.padding(bottom = PaddingDefaults.Medium)
            ) {
                PrimaryButton(
                    onClick = {
                        ClipBoardCopy.copyToClipboard(
                            context = context,
                            text = error.stackTraceToString()
                        )
                        dialog.dismiss()
                    }
                ) {
                    Text(text = "Copy to clipboard")
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(PaddingDefaults.Medium)
                .verticalScroll(scrollState)
        ) {
            ErezeptText.Body(
                text = error.stackTraceToString(),
                color = Color.Red
            )
            SpacerMedium()
        }
    }
}

@LightDarkPreview
@Composable
internal fun ExceptionDialogPreview() {
    PreviewAppTheme {
        ExceptionDialogContent(
            scrollState = rememberScrollState(),
            dialog = Dialog(LocalContext.current),
            error = IllegalStateException("This is a test exception")
        )
    }
}
