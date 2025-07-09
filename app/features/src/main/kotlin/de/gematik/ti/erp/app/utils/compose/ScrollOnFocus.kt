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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LayoutDelay = 330L

@OptIn(ExperimentalLayoutApi::class)
fun Modifier.scrollOnFocus(to: Int, listState: LazyListState, offset: Int = 0) = composed {
    val coroutineScope = rememberCoroutineScope()
    val mutex = MutatorMutex()

    var hasFocus by remember { mutableStateOf(false) }
    val keyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(hasFocus, keyboardVisible) {
        if (hasFocus && keyboardVisible) {
            mutex.mutate {
                delay(LayoutDelay)
                listState.animateScrollToItem(to, offset)
            }
        }
    }

    onFocusChanged {
        if (it.hasFocus) {
            hasFocus = true
            coroutineScope.launch {
                mutex.mutate(MutatePriority.UserInput) {
                    delay(LayoutDelay)
                    listState.animateScrollToItem(to, offset)
                }
            }
        } else {
            hasFocus = false
        }
    }
}
