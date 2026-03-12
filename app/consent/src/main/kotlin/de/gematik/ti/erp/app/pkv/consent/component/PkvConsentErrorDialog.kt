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

package de.gematik.ti.erp.app.pkv.consent.component

import android.app.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.authentication.mapper.toDialogMapper
import de.gematik.ti.erp.app.pkv.consent.model.ConsentDialogAction
import de.gematik.ti.erp.app.pkv.consent.model.ConsentUiAction
import de.gematik.ti.erp.app.pkv.consent.model.ConsentViewState
import de.gematik.ti.erp.app.pkv.consent.preview.ConsentErrorPreviewData
import de.gematik.ti.erp.app.pkv.consent.preview.HandleErrorStatePreviewParameterProvider
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.utils.compose.ConsentFailureDialog
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Suppress("LongParameterList", "CyclomaticComplexMethod")
@Composable
fun PkvConsentErrorDialog(
    consentState: ConsentViewState,
    dialog: DialogScaffold,
    onRetry: () -> Unit,
    onShowCardWall: () -> Unit
) {
    val latestOnRetry = rememberUpdatedState(onRetry)
    val latestOnShowCardWall = rememberUpdatedState(onShowCardWall)

    val action = remember(consentState) {
        reduceConsentState(
            state = consentState.errorState
        )
    }

    LaunchedEffect(action) {
        when (action) {
            is ConsentUiAction.ShowDialog -> {
                action.params.let { params ->
                    dialog.show { d ->
                        ConsentFailureDialog(error = params) {
                            when (action.action) {
                                ConsentDialogAction.RETRY -> latestOnRetry.value.invoke()
                                ConsentDialogAction.SHOW_CARD_WALL -> latestOnShowCardWall.value.invoke()
                                null -> Unit
                            }
                            d.dismiss()
                        }
                    }
                }
            }

            ConsentUiAction.NoOp -> Unit
        }
    }
}

fun reduceConsentState(
    state: HttpErrorState
): ConsentUiAction = when (state) {
    HttpErrorState.RequestTimeout,
    HttpErrorState.ServerError,
    HttpErrorState.TooManyRequest -> {
        state.toDialogMapper()?.let { params ->
            ConsentUiAction.ShowDialog(
                params = params,
                action = ConsentDialogAction.RETRY
            )
        } ?: ConsentUiAction.NoOp
    }

    HttpErrorState.BadRequest,
    HttpErrorState.Forbidden ->
        (state.toDialogMapper()?.let { ConsentUiAction.ShowDialog(it) } ?: ConsentUiAction.NoOp)

    HttpErrorState.Unauthorized ->
        (
            state.toDialogMapper()?.let { params ->
                ConsentUiAction.ShowDialog(params, action = ConsentDialogAction.SHOW_CARD_WALL)
            } ?: ConsentUiAction.NoOp
            )

    else -> ConsentUiAction.NoOp
}

@LightDarkPreview
@Composable
fun PkvConsentErrorDialogPreview(
    @PreviewParameter(HandleErrorStatePreviewParameterProvider::class) data: ConsentErrorPreviewData
) {
    PreviewTheme {
        val context = LocalContext.current
        val dialogContent = remember { mutableStateOf<(@Composable (Dialog) -> Unit)?>(null) }
        val previewDialogScaffold = object : DialogScaffold {
            override fun show(content: @Composable (Dialog) -> Unit) {
                // Store content to be rendered from composable scope below
                dialogContent.value = content
            }
        }
        PkvConsentErrorDialog(
            consentState = data.viewState,
            dialog = previewDialogScaffold,
            onRetry = {},
            onShowCardWall = {}
        )
        dialogContent.value?.invoke(Dialog(context))
    }
}
