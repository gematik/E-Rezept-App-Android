/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.api

import timber.log.Timber

sealed class Result<out T : Any> {

    class Success<out T : Any>(data: T?) : Result<T>() {
        private val _data = data
        val data
            get() = requireNotNull(_data)
    }

    class Error(val exception: Exception) : Result<Nothing>() {
        init {
            Timber.d(exception, "/\\/\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\\n")
        }
    }

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}

inline fun <A : Any, B : Any> Result<A>.map(block: (A) -> Result<B>): Result<B> =
    when (this) {
        is Result.Success -> block(this.data)
        is Result.Error -> this
    }

fun <T : Any> Result<T>.unwrap(): T =
    when (this) {
        is Result.Success -> this.data
        is Result.Error -> throw this.exception
    }
