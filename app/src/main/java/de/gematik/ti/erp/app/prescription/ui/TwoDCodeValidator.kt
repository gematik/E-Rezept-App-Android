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

package de.gematik.ti.erp.app.prescription.ui

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

data class ScannedCode(
    val json: String,
    val scannedOn: OffsetDateTime
)

data class ValidScannedCode(
    val raw: ScannedCode,
    val urls: List<String>
)

@JsonClass(generateAdapter = true)
data class Tasks(
    val urls: MutableList<String>
)

const val FNC_INT = 29

/**
 * The [TwoDCodeValidator] validates a [ScannedCode] and returns, if the containing json is valid,
 * a [ValidScannedCode] or otherwise null.
 */
class TwoDCodeValidator @Inject constructor(
    moshi: Moshi
) {
    private val adapter = moshi.adapter(Tasks::class.java)
    private val fncChar = FNC_INT.toChar()
    fun validate(code: ScannedCode): ValidScannedCode? {
        try {
            val codeToValidate = if (code.json.startsWith(fncChar)) {
                val newJson = code.json.removePrefix(fncChar.toString())
                code.copy(json = newJson)
            } else {
                code
            }

            adapter.fromJson(codeToValidate.json)?.let { bundle ->
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
            Timber.d(e, "Couldn't parse data matrix content")
        }
        return null
    }

    companion object {
        const val MAX_PRESCRIPTIONS = 3
        const val MIN_PRESCRIPTIONS = 1

        // see gemSpec_FD_eRp A_19019 & A_19021
        val taskPattern = (
            "Task/([A-Za-z0-9\\-\\.]{1,64})/\\\$accept\\?ac=([0-9a-f]{64})"
            ).toRegex()
    }
}
