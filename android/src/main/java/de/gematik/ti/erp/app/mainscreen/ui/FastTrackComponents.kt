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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.MainViewModel
import de.gematik.ti.erp.app.utils.compose.AcceptDialog

@Composable
fun ExternalAuthenticationDialog(
    mainViewModel: MainViewModel
) {
    var showAuthenticationError by remember { mutableStateOf(false) }

    val activity = LocalActivity.current
    LaunchedEffect(Unit) {
        // This ensures that we only trigger the authorization if we are returning from the main card wall
        mainViewModel.hasActiveProfileToken.collect {
            if (!it) {
                (activity as MainActivity).unvalidatedInstantUri.collect { uri ->
                    mainViewModel.onExternAppAuthorizationResult(uri)
                        .onFailure { showAuthenticationError = true }
                }
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
