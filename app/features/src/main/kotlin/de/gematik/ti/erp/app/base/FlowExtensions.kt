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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull

val falseStateFlow
    @Composable
    get() = MutableStateFlow(false).collectAsState()

/**
 * Collects the results of a flow and calls the appropriate lambda based on the result type
 */
suspend fun <R, T> Flow<Result<T>>.collectResult(
    onSuccess: (value: T) -> R,
    onFailure: (exception: Throwable) -> R
) {
    collectLatest { result ->
        result.fold(
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}

/**
 * Updates the results of a flow to a UiState
 */
suspend fun <T> Flow<List<T>>.toUiState(
    state: (UiState<List<T>>) -> Unit
) {
    runCatching {
        this
    }.fold(
        onSuccess = {
            val items = it.firstOrNull()
            if (items?.size == 0) {
                state(UiState.Empty())
            } else {
                state(UiState.Data(items))
            }
        },
        onFailure = { error ->
            Napier.e { "Error on UsecaseResult load ${error.stackTraceToString()}" }
            state(UiState.Error(error))
        }
    )
}
