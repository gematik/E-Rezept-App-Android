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

package de.gematik.ti.erp.app

import com.ensody.reactivestate.isFatal
import org.jose4j.base64url.Base64Url

class UncaughtException(
    private val throwable: Throwable,
    private val packageName: String,
    private val isDemoMode: Boolean
) : Throwable(cause = throwable) {
    override fun toString(): String {
        val name = throwable::class::simpleName.name
        val message = throwable.localizedMessage
        val trace = throwable.stackTraceToString()
        val className = throwable.stackTrace[0].className
        val methodName = throwable.stackTrace[0].methodName
        val fileName = throwable.stackTrace[0].fileName
        val lineNumber = throwable.stackTrace[0].lineNumber

        return when {
            message != null -> {
                val msgBase64 = Base64Url
                    .encodeUtf8ByteRepresentation(message)
                    .replace('-', '$') // class names don't contain any minus symbol

                "${name}_$msgBase64: \n" +
                    "localizedMessage: $message\n " +
                    "activity-info: package-$packageName is-demo-mode-$isDemoMode\n " +
                    "className: $className\n" +
                    "methodName: $methodName\n" +
                    "fileName: $fileName\n" +
                    "lineNumber: $lineNumber\n" +
                    "stackTrace: $trace\n" +
                    "isFatal: ${throwable.isFatal()}"
            }

            else ->
                "$name (no message)\n " +
                    "activity-info: package-$packageName is-demo-mode-$isDemoMode\n " +
                    "className: $className\n" +
                    "methodName: $methodName\n" +
                    "fileName: $fileName\n" +
                    "lineNumber: $lineNumber\n" +
                    "stackTrace: ${trace}\n" +
                    "isFatal: ${throwable.isFatal()}"
        }
    }
}
