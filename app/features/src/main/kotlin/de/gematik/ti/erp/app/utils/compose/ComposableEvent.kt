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

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Stable
class ComposableEvent<T> {

    private val triggerFlow = MutableStateFlow<T?>(null)

    var payload: T? by mutableStateOf(null)

    fun trigger(payload: T) {
        this.payload = payload
        triggerFlow.value = payload
    }

    @Composable
    @SuppressLint("ComposableNaming")
    fun listen(
        block: suspend CoroutineScope.(payload: T) -> Unit
    ) {
        LaunchedEffect(triggerFlow) {
            triggerFlow.collectLatest { payload ->
                payload?.let {
                    block(payload)
                    triggerFlow.value = null
                }
            }
        }
    }

    fun listen(
        coroutineScope: CoroutineScope,
        block: suspend CoroutineScope.(payload: T) -> Unit
    ) {
        coroutineScope.launch {
            triggerFlow.collectLatest { payload ->
                payload?.let {
                    block(it)
                    triggerFlow.value = null
                }
            }
        }
    }

    companion object {
        fun ComposableEvent<Unit>.trigger() {
            trigger(Unit)
        }
    }
}
