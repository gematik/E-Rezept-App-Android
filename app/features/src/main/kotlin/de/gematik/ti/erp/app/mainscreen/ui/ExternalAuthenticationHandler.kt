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

package de.gematik.ti.erp.app.mainscreen.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.base.ClipBoardCopy
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.idp.usecase.AuthenticateWithExternalHealthInsuranceAppUseCase
import de.gematik.ti.erp.app.mainscreen.presentation.AuthenticationHandlerState
import de.gematik.ti.erp.app.mainscreen.presentation.ExternalAuthenticationHandler
import de.gematik.ti.erp.app.utils.compose.LoadingDialog
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
import de.gematik.ti.erp.app.utils.extensions.SnackbarScaffold
import org.kodein.di.compose.rememberInstance

@Composable
fun ExternalAuthenticationUiHandler() {
    val intentHandler = LocalIntentHandler.current

    val snackbar = LocalSnackbar.current
    val context = LocalContext.current

    val idpUseCase = rememberInstance<AuthenticateWithExternalHealthInsuranceAppUseCase>()
    val authenticationHandler = remember { ExternalAuthenticationHandler(idpUseCase) }
    val state by authenticationHandler.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        intentHandler.extAuthIntent.collect {
            showUniversalUrl(
                snackbar = snackbar,
                context = context,
                url = it.uriData
            )
            authenticationHandler.handle(it)
        }
    }

    if (state == AuthenticationHandlerState.Loading) {
        LoadingDialog { authenticationHandler.resetState() }
    }

    if (state == AuthenticationHandlerState.Failure) {
        UniversalLinkWrongDialog { authenticationHandler.resetState() }
    }

    if (BuildConfigExtension.isInternalDebug) {
        if (state == AuthenticationHandlerState.SsoTokenNotSaved) {
            snackbar.show("SSO Token missing")
        }

        if (state == AuthenticationHandlerState.AuthTokenNotSaved) {
            snackbar.show("Authentication Token missing")
        }
    }

    if (state == AuthenticationHandlerState.Loading && BuildConfigExtension.isReleaseMode) {
        snackbar.show(stringResource(R.string.connecting_universal_link))
    }
}

private fun showUniversalUrl(
    snackbar: SnackbarScaffold,
    context: Context,
    url: String
) {
    val copyTextId = R.string.debug_copy_text
    if (BuildConfigExtension.isInternalDebug) {
        snackbar.show(
            text = "DEBUG: universal-url: $url",
            actionTextId = copyTextId,
            onClickAction = {
                ClipBoardCopy.copyToClipboard(
                    context = context,
                    text = url
                )
            }
        )
    }
}
