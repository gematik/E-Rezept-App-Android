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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.core.LocalIntentHandler
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import io.github.aakira.napier.Napier
import org.kodein.di.LazyDelegate
import org.kodein.di.compose.rememberInstance
import java.net.URI

@Stable
class FastTrackHandler(
    idpUseCase: LazyDelegate<IdpUseCase>
) {
    private val idpUseCase by idpUseCase

    /**
     * Handles an incoming intent. Returns `true` if the intent could be handled.
     */
    @Requirement(
        "O.Plat_10#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "handle incoming intent"
    )
    @Suppress("TooGenericExceptionCaught")
    suspend fun handle(value: String): Boolean =
        try {
            Napier.d("Authenticate external ...")
            idpUseCase.authenticateWithExternalAppAuthorization(URI(value))
            Napier.d("... authenticated")
            true
        } catch (e: Exception) {
            Napier.e(e) { "Couldn't authenticate" }
            false
        }
}

@Composable
fun ExternalAuthenticationDialog() {
    var showAuthenticationError by remember { mutableStateOf(false) }
    val intentHandler = LocalIntentHandler.current
    val idpUseCase = rememberInstance<IdpUseCase>()
    val fastTrackHandler = remember { FastTrackHandler(idpUseCase) }

    LaunchedEffect(Unit) {
        intentHandler.extAuthIntent.collect {
            if (!fastTrackHandler.handle(it)) {
                showAuthenticationError = true
            }
        }
    }

    if (showAuthenticationError) {
        AcceptDialog(
            header = stringResource(R.string.main_fasttrack_error_title),
            info = stringResource(R.string.main_fasttrack_error_info),
            acceptText = stringResource(R.string.ok)
        ) {
            showAuthenticationError = false
        }
    }
}
