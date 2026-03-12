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

package de.gematik.ti.erp.app.pkv.consent.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.pkv.consent.component.PkvConsentErrorDialog
import de.gematik.ti.erp.app.pkv.consent.presentation.ConsentController
import de.gematik.ti.erp.app.pkv.consent.presentation.rememberConsentController
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

@Suppress("LongParameterList")
@Composable
fun ConsentScreen(
    profile: ProfilesUseCaseData.Profile,
    consentController: ConsentController = rememberConsentController(),
    dialogScaffold: DialogScaffold = LocalDialog.current,
    onShowCardWall: () -> Unit = {},
    onConsentGranted: () -> Unit = {},
    onConsentRevoked: () -> Unit = {},
    onConsentNotGranted: () -> Unit = {},
    onDeleteLocalInvoices: () -> Unit = {}
) {
    val consentViewState by consentController.consentViewState.collectAsStateWithLifecycle()

    PkvConsentErrorDialog(
        consentState = consentViewState,
        dialog = dialogScaffold,
        onRetry = { consentController.onRetry(profile) },
        onShowCardWall = onShowCardWall
    )

    // Notify parent about consent state changes and trigger auxiliary actions
    val latestOnGranted by rememberUpdatedState(onConsentGranted)
    val latestOnRevoked by rememberUpdatedState(onConsentRevoked)
    val latestOnNotGranted by rememberUpdatedState(onConsentNotGranted)
    val latestOnDeleteLocalInvoices by rememberUpdatedState(onDeleteLocalInvoices)

    LaunchedEffect(consentViewState.state) {
        when (consentViewState.state) {
            is ConsentState.ValidState.Granted -> {
                latestOnGranted()
            }

            is ConsentState.ValidState.Revoked -> {
                latestOnRevoked()
                // allow caller to purge local invoices when consent is revoked
                latestOnDeleteLocalInvoices()
            }

            is ConsentState.ValidState.NotGranted -> latestOnNotGranted()
            else -> Unit
        }
    }
}
