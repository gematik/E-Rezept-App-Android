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

package de.gematik.ti.erp.app.prescription.ui

import de.gematik.ti.erp.app.Requirement
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant

data class ScannedCode(
    val json: String,
    val scannedOn: Instant
)

data class ValidScannedCode(
    val raw: ScannedCode,
    val urls: List<String>
)

@Serializable
data class Tasks(
    val urls: MutableList<String>
)

/**
 * The [TwoDCodeValidator] validates a [ScannedCode] and returns, if the containing json is valid,
 * a [ValidScannedCode] or otherwise null.
 */
@Requirement(
    "O.Source_1#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Validate the data matrix code structure"
)
class TwoDCodeValidator {
    fun validate(code: ScannedCode): ValidScannedCode? {
        try {
            Json.decodeFromString<Tasks>(code.json).let { bundle ->
                val urls = bundle.urls
                    .takeIf { it.size in MIN_PRESCRIPTIONS..MAX_PRESCRIPTIONS }
                    ?.takeIf {
                        it.all { url ->
                            taskPattern.matchEntire(url) != null
                        }
                    }

                return urls?.let {
                    ValidScannedCode(code, urls)
                }
            }
        } catch (e: Exception) {
            Napier.d("Couldn't parse data matrix content", e)
        }
        return null
    }

    companion object {
        const val MAX_PRESCRIPTIONS = 3
        const val MIN_PRESCRIPTIONS = 1

        // see gemSpec_FD_eRp A_19019 & A_19021

        val taskIdPattern = "([A-Za-z0-9\\-.]{1,64})".toRegex()
        val accessCodePattern = "([0-9a-f]{64})".toRegex()

        val taskPattern = (
            "Task/([A-Za-z0-9\\-\\.]{1,64})/\\\$accept\\?ac=([0-9a-f]{64})"
            ).toRegex()
    }
}
