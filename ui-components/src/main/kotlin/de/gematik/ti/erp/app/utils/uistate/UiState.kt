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

package de.gematik.ti.erp.app.utils.uistate

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

@Stable
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

        val UiState<*>.isEmptyState: Boolean
            get() = !isLoading && isDataEmpty

        val UiState<*>.isNotDataState: Boolean
            get() = !isDataState

        val UiState<*>.isErrorState: Boolean
            get() = !isLoading && error != null && data == null

        val UiState<*>.isDataState: Boolean
            get() = !isLoading && data != null && error == null

        val UiState<*>.isLoadingState: Boolean
            get() = isLoading && data == null && error == null

        suspend fun <T> StateFlow<UiState<T>>.extract(): T? = first { it.isDataState }.data

        suspend fun <T> Flow<UiState<T>>.extract(): T? =
            first { it.data != null && !it.isLoading && it.error == null }.data
    }
}
