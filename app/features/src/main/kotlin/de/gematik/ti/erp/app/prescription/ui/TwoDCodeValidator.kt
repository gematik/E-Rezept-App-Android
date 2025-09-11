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

package de.gematik.ti.erp.app.prescription.ui

import androidx.core.net.toUri
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.prescription.model.ParsedScannedCode
import de.gematik.ti.erp.app.prescription.model.ParsedScannedQrCode
import de.gematik.ti.erp.app.prescription.model.ParserScannedDataMatrix
import de.gematik.ti.erp.app.prescription.ui.TwoDCodeValidator.Companion.taskPattern
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.net.URLDecoder

data class RawScannedCode(
    val json: String,
    val scannedOn: Instant
)

data class ValidScannedCode(
    val raw: RawScannedCode,
    val codes: List<ParsedScannedCode>
)

/** DataMatrix payload used today on the paper token. */
@Serializable
private data class ScannedTaskUrls(
    val urls: MutableList<String>
)

/**
 * Validates 2D codes coming from:
 *  - DataMatrix JSON ({"urls": ["Task/<id>/$accept?ac=<code>", ...]})
 *  - QR deeplink (https://erezept.gematik.de/prescription/#["taskId|accessCode|name", ...])
 *
 * For both, we normalize to canonical URLs: "Task/{id}/$accept?ac={code}" and validate via [taskPattern].
 * Return type stays unchanged: [ValidScannedCode] with the canonical URLs.
 */
@Requirement(
    "O.Source_1#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Validate the DataMatrix code structure; accept equivalent QR deeplink content when normalized."
)
class TwoDCodeValidator {

    /**
     * @return [ValidScannedCode] if payload is valid and within allowed bounds, else null.
     */
    fun validate(code: RawScannedCode): ValidScannedCode? {
        // Try QR deeplink else DataMatrix JSON  (existing behavior)
        val rawParsedCodes = parseQrDeepLinks(code.json) ?: parseDataMatrixUrls(code.json)

        val validatedParsedCodes = rawParsedCodes
            ?.takeIf { it.size in MIN_PRESCRIPTIONS..MAX_PRESCRIPTIONS }
            ?.takeIf { codes ->
                codes.all { code ->
                    val sanitaryCheckResult = when (code) {
                        is ParsedScannedQrCode -> {
                            val url = "Task/${code.taskId}/${'$'}accept?ac=${code.accessCode}"
                            taskPattern.matchEntire(url) != null
                        }

                        is ParserScannedDataMatrix -> taskPattern.matchEntire(code.url) != null
                    }

                    Napier.d(tag = "2DScanner", message = "Validating [$code] -> $sanitaryCheckResult")
                    sanitaryCheckResult
                }
            }

        return validatedParsedCodes?.let {
            ValidScannedCode(
                raw = code,
                codes = validatedParsedCodes
            )
        }
    }

    // ----------------------
    // Parsing helpers
    // ----------------------

    /** DataMatrix case: {"urls":[ "Task/<id>/$accept?ac=<code>", ... ]} */
    private fun parseDataMatrixUrls(payload: String): List<ParsedScannedCode>? =
        try {
            Json.decodeFromString(ScannedTaskUrls.serializer(), payload).urls.map { ParserScannedDataMatrix(it) }
        } catch (e: Exception) {
            Napier.d(tag = "2DScanner", message = "Not a DataMatrix Tasks JSON", throwable = e)
            null
        }

    /** Returns triples (taskId, accessCode, name) if input is a QR deeplink; else null. */
    private fun parseQrDeepLinks(urlOrPayload: String): List<ParsedScannedCode>? =
        try {
            val fragment = urlOrPayload.toUri().fragment ?: return null
            val decoded = URLDecoder.decode(fragment, Charsets.UTF_8.name())
            val entries = Json.decodeFromString(ListSerializer(String.serializer()), decoded)

            entries.mapNotNull { triple ->
                val parts = triple.split("|", limit = 3)
                val id = parts.getOrNull(0)?.trim().orEmpty()
                val ac = parts.getOrNull(1)?.trim().orEmpty()
                val nm = parts.getOrNull(2)?.trim().orEmpty()
                when {
                    id.isEmpty() || ac.isEmpty() || nm.isEmpty() -> null
                    !accessCodePattern.matches(ac) -> null // quick sanity check, optional
                    else -> ParsedScannedQrCode(
                        taskId = id,
                        accessCode = ac,
                        name = nm
                    )
                }
            }.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Napier.d(tag = "2DScanner", message = "Not a QR deeplink with JSON array fragment", throwable = e)
            null
        }

    companion object {
        const val MAX_PRESCRIPTIONS = 3
        const val MIN_PRESCRIPTIONS = 1

        // see gemSpec_FD_eRp A_19019 & A_19021
        val taskIdPattern = "([A-Za-z0-9\\-.]{1,64})".toRegex()
        val accessCodePattern = "([0-9a-f]{64})".toRegex()

        /** Canonical URL form we validate against. */
        val taskPattern =
            ("Task/([A-Za-z0-9\\-\\.]{1,64})/\\\$accept\\?ac=([0-9a-f]{64})").toRegex()
    }
}
