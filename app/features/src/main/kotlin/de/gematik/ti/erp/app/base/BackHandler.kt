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

package de.gematik.ti.erp.app.base

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.github.aakira.napier.Napier

@Composable
fun backPressedHandler(
    canHandleBack: Boolean,
    onBack: () -> Unit
): OnBackPressedCallback {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
    val callback = remember {
        object : OnBackPressedCallback(canHandleBack) {
            override fun handleOnBackPressed() {
                if (canHandleBack) {
                    Napier.d { "on back pressed" }
                    // Only pop the back stack if backHandler is true
                    onBack()
                }
            }
        }
    }

    DisposableEffect(callback) {
        backPressedDispatcher?.onBackPressedDispatcher?.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }
    return callback
}
