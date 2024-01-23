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

package de.gematik.ti.erp.app

import org.jose4j.base64url.Base64Url

class UncaughtException(private val throwable: Throwable) : Throwable(cause = throwable) {
    override fun toString(): String {
        val name = throwable::class::simpleName.name
        val message = throwable.localizedMessage
        val trace = throwable.stackTraceToString()

        return when {
            message != null -> {
                val msgBase64 = Base64Url
                    .encodeUtf8ByteRepresentation(message)
                    .replace('-', '$') // class names don't contain any minus symbol

                "${name}_$msgBase64: \nlocalizedMessage: $message\n stackTrace: $trace"
            }
            else -> name
        }
    }
}

fun catchUncaughtException() {
    val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        defaultExceptionHandler?.uncaughtException(thread, UncaughtException(throwable))
    }
}
