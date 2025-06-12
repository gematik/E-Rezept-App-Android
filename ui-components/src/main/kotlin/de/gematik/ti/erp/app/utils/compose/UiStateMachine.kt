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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.animated.AnimationTime
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState

@Composable
fun <T : Any> UiStateMachine(
    state: UiState<T?>,
    onLoading: (@Composable () -> Unit)? = null,
    onEmpty: (@Composable () -> Unit)? = null,
    onError: (@Composable (Throwable) -> Unit)? = null,
    onContent: @Composable (T) -> Unit
) {
    Crossfade(
        targetState = state,
        animationSpec = tween(AnimationTime.DELAY_100),
        label = "ui-state-change"
    ) { currentState ->
        when {
            currentState.isLoading -> onLoading?.invoke()
            currentState.error != null -> onError?.invoke(currentState.error)
            currentState.data == null || state.isEmptyState -> onEmpty?.invoke()
            else -> onContent(currentState.data)
        }
    }
}
