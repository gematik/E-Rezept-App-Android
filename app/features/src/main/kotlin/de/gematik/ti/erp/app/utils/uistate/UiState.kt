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

package de.gematik.ti.erp.app.utils.uistate

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class UiState<out T>(
    val isLoading: Boolean = false,
    @Stable val data: T? = null,
    @Stable val error: Throwable? = null
) {

    private val isDataEmpty: Boolean
        get() = data.let { data ->
            when (data) {
                is Collection<*> -> data.isEmpty()
                else -> data == null
            }
        }

    companion object {

        @Suppress("FunctionName")
        fun <T> Loading(data: T? = null): UiState<T> = UiState(isLoading = true, data = data)

        @Suppress("FunctionName")
        fun <T> Data(data: T?): UiState<T> = UiState(isLoading = false, data = data)

        @Suppress("FunctionName")
        fun <T> Empty(): UiState<T> = UiState(isLoading = false, data = null)

        @Suppress("FunctionName")
        fun <T> Error(error: Throwable): UiState<T> = UiState(isLoading = false, error = error)

        val UiState<*>.isEmpty: Boolean
            get() = !isLoading && isDataEmpty
    }
}
