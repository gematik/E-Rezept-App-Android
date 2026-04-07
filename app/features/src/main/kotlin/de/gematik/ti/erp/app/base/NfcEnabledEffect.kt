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

package de.gematik.ti.erp.app.base

import android.nfc.NfcAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.core.LocalActivity

/**
 * Observes [Lifecycle.Event.ON_RESUME] and calls [onNfcEnabled] whenever the activity resumes
 * with NFC already enabled. This covers the common case where the user goes to system NFC
 * settings, enables NFC, and presses Back to return to the app.
 *
 * [rememberUpdatedState] ensures the latest [onNfcEnabled] lambda is always used, avoiding
 * the stale-closure issue that affects plain [DisposableEffect] lambdas.
 */
@Composable
fun NfcEnabledEffect(onNfcEnabled: () -> Unit) {
    val activity = LocalActivity.current as MainActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnNfcEnabled by rememberUpdatedState(onNfcEnabled)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
                if (nfcAdapter?.isEnabled == true) {
                    currentOnNfcEnabled()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
