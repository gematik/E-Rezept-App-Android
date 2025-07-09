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

@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.timeouts.datasource.local

import android.content.SharedPreferences
import androidx.core.content.edit
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.DEFAULT_INACTIVITY_DURATION
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.DEFAULT_PAUSE_DURATION
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.INACTIVITY_TIMER_ENUM
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.INACTIVITY_TIMER_VALUE
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.PAUSE_TIMER_ENUM
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.PAUSE_TIMER_VALUE
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultInactivityMetric
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultInactivityValue
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultPauseMetric
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultPauseValue
import io.github.aakira.napier.Napier
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

object TimeoutConstant {
    const val INACTIVITY_TIMER_VALUE = "INACTIVITY_TIMER"
    const val INACTIVITY_TIMER_ENUM = "INACTIVITY_TIMER_ENUM"
    const val PAUSE_TIMER_VALUE = "PAUSE_TIMER"
    const val PAUSE_TIMER_ENUM = "PAUSE_TIMER_ENUM"
    const val defaultInactivityValue = 10
    const val defaultPauseValue = 60
    val defaultInactivityMetric = DurationUnit.MINUTES
    val defaultPauseMetric = DurationUnit.SECONDS
    val DEFAULT_INACTIVITY_DURATION = 10.minutes

    @Requirement(
        "O.Auth_8#4",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "The timer is set to 60 seconds.",
        codeLines = 2
    )
    val DEFAULT_PAUSE_DURATION = 60.seconds
}

class TimeoutsLocalDataSource(
    private val sharedPreferences: SharedPreferences
) {
    fun setDefaultTimeouts() {
        try {
            sharedPreferences.edit {
                putInt(INACTIVITY_TIMER_VALUE, defaultInactivityValue)
                apply()
            }
            sharedPreferences.edit {
                putInt(PAUSE_TIMER_VALUE, defaultPauseValue)
                apply()
            }
            sharedPreferences.edit {
                putString(INACTIVITY_TIMER_ENUM, defaultInactivityMetric.name)
                apply()
            }
            sharedPreferences.edit {
                putString(PAUSE_TIMER_ENUM, defaultPauseMetric.name)
                apply()
            }
        } catch (e: Throwable) {
            Napier.e { "error on setDefaultTimeouts" }
        }
    }

    fun setInactivityTimer(duration: Duration) {
        try {
            val (value, enum) = "$duration".durationEnumPair()
            sharedPreferences.edit {
                putInt(INACTIVITY_TIMER_VALUE, value.toInt())
                apply()
            }
            sharedPreferences.edit {
                putString(INACTIVITY_TIMER_ENUM, enum.name)
                apply()
            }
        } catch (e: Throwable) {
            Napier.e { "error on setInactivityTimer" }
        }
    }

    fun setPauseTimer(duration: Duration) {
        try {
            val (value, enum) = "$duration".durationEnumPair()
            sharedPreferences.edit().apply {
                putInt(PAUSE_TIMER_VALUE, value.toInt())
                apply()
            }
            sharedPreferences.edit().apply {
                putString(PAUSE_TIMER_ENUM, enum.name)
                apply()
            }
        } catch (e: Throwable) {
            Napier.e { "error on setPauseTimer" }
        }
    }

    fun checkForExistingTimeouts(): Boolean {
        return try {
            val inactivityEnum = sharedPreferences.getString(INACTIVITY_TIMER_ENUM, null)
            val inactivityValue = sharedPreferences.getInt(INACTIVITY_TIMER_VALUE, 0)
            val hasInactivityTimer = (inactivityValue to inactivityEnum).toDuration() != null

            val pauseEnum = sharedPreferences.getString(PAUSE_TIMER_ENUM, null)
            val pauseValue = sharedPreferences.getInt(PAUSE_TIMER_VALUE, 0)
            val hasPauseTimer = (pauseValue to pauseEnum).toDuration() != null
            (hasInactivityTimer && hasPauseTimer)
        } catch (e: Throwable) {
            false
        }
    }

    fun getInactivityTimeout(): Duration {
        return try {
            val enum = sharedPreferences.getString(INACTIVITY_TIMER_ENUM, null)
            val value = sharedPreferences.getInt(INACTIVITY_TIMER_VALUE, 0)
            (value to enum).toDuration() ?: DEFAULT_INACTIVITY_DURATION
        } catch (e: Throwable) {
            Napier.e { "exception on getInactivityTimeout $e" }
            DEFAULT_INACTIVITY_DURATION
        }
    }

    fun getPauseTimeout(): Duration {
        return try {
            val enum = sharedPreferences.getString(PAUSE_TIMER_ENUM, null)
            val value = sharedPreferences.getInt(PAUSE_TIMER_VALUE, 0)
            (value to enum).toDuration() ?: DEFAULT_PAUSE_DURATION
        } catch (e: Throwable) {
            Napier.e { "exception on getPauseTimeout $e" }
            DEFAULT_PAUSE_DURATION
        }
    }

    companion object {
        private fun String.durationEnumPair(): Pair<String, DurationEnum> {
            val regex = Regex("(\\d+)([hms])")
            val matchResults = regex.findAll(this)
            var value = ""
            var unit = DurationEnum.SECONDS
            for (result in matchResults) {
                value = result.groupValues[1] // Extract the numeric value
                unit = DurationEnum.extractedUnit(result.groupValues[2]) // Extract the unit (h, m, or s)
            }
            return value to unit
        }

        @OptIn(ExperimentalTime::class)
        fun Pair<Int?, String?>.toDuration(): Duration? {
            this.first?.let { nonNullValue ->
                this.second?.let { nonNullEnum ->
                    val durationUnit = DurationUnit.valueOf(nonNullEnum)
                    return Duration.convert(
                        nonNullValue.toDouble(),
                        durationUnit,
                        durationUnit
                    ).toDuration(durationUnit)
                }
            }
            return null
        }

        /**
         * Allowed enums on the debug screen
         */
        enum class DurationEnum {
            UNSPECIFIED,
            SECONDS,
            MINUTES,
            HOURS;

            companion object {
                fun extractedUnit(input: String) =
                    when (input) {
                        "h" -> HOURS
                        "m" -> MINUTES
                        "s" -> SECONDS
                        else -> UNSPECIFIED
                    }
            }
        }
    }
}
