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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmpty

@Composable
fun <T : Any> UiStateMachine(
    state: UiState<T?>,
    onLoading: (@Composable () -> Unit)? = null,
    onEmpty: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
    onContent: @Composable (T) -> Unit
) {
    val data = state.data
    val error = state.error
    when {
        state.isLoading -> onLoading?.invoke()
        error != null -> onError?.invoke(error)
        data == null || state.isEmpty -> onEmpty?.invoke()
        else -> onContent(data)
    }
}
