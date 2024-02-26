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

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope

class ComposableEvent<T> {

    private var trigger by mutableStateOf(false)

    var payload: T? by mutableStateOf(null)

    fun trigger(payload: T) {
        this.payload = payload
        trigger = true
    }

    @Composable
    @SuppressLint("ComposableNaming")
    fun listen(
        block: suspend CoroutineScope.(payload: T) -> Unit
    ) {
        LaunchedEffect(trigger) {
            if (trigger) {
                trigger = false

                // Has to be set via trigger as `T`
                @Suppress("UNCHECKED_CAST")
                block(payload as T)
            }
        }
    }

    companion object {
        fun ComposableEvent<Unit>.trigger() {
            trigger(Unit)
        }
    }
}
