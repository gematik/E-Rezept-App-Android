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

package de.gematik.ti.erp.app

import com.ensody.reactivestate.isFatal
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import io.github.aakira.napier.Napier
import org.jose4j.base64url.Base64Url
import org.json.JSONObject
import java.time.Instant

class UncaughtException(
    private val throwable: Throwable,
    private val packageName: String,
    private val isDemoMode: Boolean,
    private val metadata: Map<String, Any?>
) : Throwable(cause = throwable) {

    override fun toString(): String {
        val name = throwable::class.simpleName ?: "UnknownException"
        val message = throwable.localizedMessage ?: "No message"
        val encodedMessage = Base64Url.encodeUtf8ByteRepresentation(message).replace('-', '$')

        val firstFrame = throwable.stackTrace.firstOrNull()
        val className = firstFrame?.className ?: "unknown"
        val methodName = firstFrame?.methodName ?: "unknown"
        val fileName = firstFrame?.fileName ?: "unknown"
        val lineNumber = firstFrame?.lineNumber ?: -1

        val traceLines = throwable.stackTraceToString().lines()
        val traceTrimmed = if (traceLines.size > 20) {
            traceLines.take(20).joinToString("\n") + "\n...(${traceLines.size - 20} more lines)"
        } else {
            traceLines.joinToString("\n")
        }

        val rootCause = findRootCause(throwable)
        val rootCauseMsg = rootCause?.localizedMessage ?: "None"
        val rootCauseClass = rootCause?.javaClass?.name ?: "None"

        if (BuildConfigExtension.isInternalDebug) {
            Napier.e(tag = "UncaughtException", message = toJson())
        }

        return buildString {
            appendLine("[$name]_$encodedMessage")
            appendLine("Timestamp         : ${Instant.now()}")
            appendLine("Localized Message : $message")
            appendLine("Fatal             : ${throwable.isFatal()}")
            appendLine("Package           : $packageName")
            appendLine("Demo Mode         : $isDemoMode")
            appendLine("Class             : $className")
            appendLine("Method            : $methodName")
            appendLine("File              : $fileName")
            appendLine("Line              : $lineNumber")
            appendLine("Root Cause        : $rootCauseClass → $rootCauseMsg")
            appendLine("Cause Chain       : ${buildCauseChain(throwable)}")

            if (throwable.suppressed.isNotEmpty()) {
                appendLine("Suppressed        :")
                throwable.suppressed.forEach {
                    appendLine("  ${it::class.java.simpleName}: ${it.localizedMessage}")
                }
            }

            appendLine("Metadata:")
            appendLine(formatMetadata(metadata))

            appendLine("Stacktrace:")
            appendLine(traceTrimmed)
        }
    }

    fun toJson(): String {
        val json = JSONObject()
        json.put("timestamp", Instant.now().toString())
        json.put("exception", throwable::class.simpleName ?: "Unknown")
        json.put("message", throwable.localizedMessage ?: "No message")
        json.put("fatal", throwable.isFatal())
        json.put("package", packageName)
        json.put("demoMode", isDemoMode)

        val firstFrame = throwable.stackTrace.firstOrNull()
        json.put("class", firstFrame?.className ?: "unknown")
        json.put("method", firstFrame?.methodName ?: "unknown")
        json.put("file", firstFrame?.fileName ?: "unknown")
        json.put("line", firstFrame?.lineNumber ?: -1)

        json.put("rootCause", findRootCause(throwable)?.toString() ?: "None")

        if (metadata.isNotEmpty()) {
            val metaJson = JSONObject()
            metadata.forEach { (key, value) ->
                metaJson.put(key, value?.toString() ?: "null")
            }
            json.put("metadata", metaJson)
        }

        return json.toString(2) // pretty print
    }

    private fun buildCauseChain(throwable: Throwable): String {
        val causeChain = StringBuilder()
        var cause = throwable.cause

        while (cause != null) {
            if (causeChain.isNotEmpty()) {
                causeChain.append(" → ")
            }
            causeChain.append(cause::class.java.simpleName)
            causeChain.append(": ")
            causeChain.append(cause.localizedMessage ?: "no message")
            cause = cause.cause
        }

        return causeChain.toString().ifEmpty { "None" }
    }

    private fun findRootCause(throwable: Throwable): Throwable? {
        var rootCause: Throwable? = throwable
        while (rootCause?.cause != null) {
            rootCause = rootCause.cause
        }
        return rootCause
    }

    private fun formatMetadata(meta: Map<*, *>, indent: String = "  "): String {
        val formattedMetadata = StringBuilder()

        for ((key, value) in meta) {
            if (formattedMetadata.isNotEmpty()) {
                formattedMetadata.append('\n')
            }

            appendMetadataEntry(formattedMetadata, key, value, indent)
        }

        return formattedMetadata.toString()
    }

    private fun appendMetadataEntry(builder: StringBuilder, key: Any?, value: Any?, indent: String) {
        builder.append(indent)
        builder.append(key)
        builder.append(": ")

        when (value) {
            is Map<*, *> -> {
                builder.append('\n')
                builder.append(formatMetadata(value, "$indent  "))
            }
            is Collection<*> -> appendCollection(builder, value)
            else -> builder.append(value)
        }
    }

    private fun appendCollection(builder: StringBuilder, values: Collection<*>) {
        var isFirstValue = true
        for (item in values) {
            if (!isFirstValue) {
                builder.append(", ")
            }
            builder.append(item)
            isFirstValue = false
        }
    }
}
