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

import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent

sealed class NonNullResult<T> {
    data class Success<T>(val value: T) : NonNullResult<T>()
    class NullValue<T> : NonNullResult<T>()
}

fun <T> requireNonNull(input: T?): NonNullResult<T> {
    return if (input != null) {
        NonNullResult.Success(input)
    } else {
        NonNullResult.NullValue()
    }
}

@Suppress("ComposableNaming")
@Composable
fun <T> NonNullResult<T>.fold(
    onSuccess: @Composable (T) -> Unit,
    onFailure: @Composable () -> Unit = { ErrorScreenComponent() }
) {
    if (this is NonNullResult.Success) {
        onSuccess(value)
    } else {
        onFailure()
    }
}
