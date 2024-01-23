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
@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.timeouts.datasource.local

import android.content.SharedPreferences
import androidx.core.content.edit
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.INACTIVITY_TIMER_ENUM
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.INACTIVITY_TIMER_VALUE
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.PAUSE_TIMER_ENUM
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.PAUSE_TIMER_VALUE
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultInactivityMetric
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultInactivityValue
import de.gematik.ti.erp.app.timeouts.datasource.local.TimeoutConstant.defaultPauseValue
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

object TimeoutConstant {
    const val INACTIVITY_TIMER_VALUE = "INACTIVITY_TIMER"
    const val INACTIVITY_TIMER_ENUM = "INACTIVITY_TIMER_ENUM"
    const val PAUSE_TIMER_VALUE = "PAUSE_TIMER"
    const val PAUSE_TIMER_ENUM = "PAUSE_TIMER_ENUM"
    const val defaultInactivityValue = 10
    const val defaultPauseValue = 30
    val defaultInactivityMetric = DurationUnit.MINUTES
    val defaultPauseMetric = DurationUnit.SECONDS
}

class TimeoutsLocalDataSource(
    private val sharedPreferences: SharedPreferences
) {
    fun setDefaultTimeouts() {
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
            putString(INACTIVITY_TIMER_ENUM, defaultInactivityMetric.name)
            apply()
        }
    }

    fun setInactivityTimer(duration: Duration) {
        val (value, enum) = "$duration".durationEnumPair()
        sharedPreferences.edit {
            putInt(INACTIVITY_TIMER_VALUE, value.toInt())
            apply()
        }
        sharedPreferences.edit {
            putString(INACTIVITY_TIMER_ENUM, enum.name)
            apply()
        }
    }

    fun setPauseTimer(duration: Duration) {
        val (value, enum) = "$duration".durationEnumPair()
        sharedPreferences.edit().apply {
            putInt(PAUSE_TIMER_VALUE, value.toInt())
            apply()
        }
        sharedPreferences.edit().apply {
            putString(PAUSE_TIMER_ENUM, enum.name)
            apply()
        }
    }

    fun getInactivityTimeout(): Duration? {
        val enum = sharedPreferences.getString(INACTIVITY_TIMER_ENUM, null)
        val value = sharedPreferences.getInt(INACTIVITY_TIMER_VALUE, 0)
        return (value to enum).toDuration()
    }

    fun getPauseTimeout(): Duration? {
        val enum = sharedPreferences.getString(PAUSE_TIMER_ENUM, null)
        val value = sharedPreferences.getInt(PAUSE_TIMER_VALUE, 0)
        return (value to enum).toDuration()
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
