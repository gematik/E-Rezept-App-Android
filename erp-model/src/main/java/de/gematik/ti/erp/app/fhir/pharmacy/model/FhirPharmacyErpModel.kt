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

package de.gematik.ti.erp.app.fhir.pharmacy.model

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class NotAvailablePeriodMetadata(
    val erpModel: NotAvailablePeriodErpModel,
    val hasOverlap: Boolean,
    val isActive: Boolean,
    val isInPast: Boolean
) {
    companion object {
        fun List<NotAvailablePeriodMetadata>.isCurrentlyClosed(): Boolean {
            val isActive = filter { it.isActive }
            return isActive.isNotEmpty()
        }
    }
}

/**
 * Data class that holds metadata about a [SpecialOpeningTimeErpModel].
 * It combines the original model with calculated states like whether it's currently active or in the past.
 * @property erpModel The original [SpecialOpeningTimeErpModel] object.
 * @property isActive True if the special opening time is currently active.
 * @property isInPast True if the special opening time period has already ended.
 */
@Serializable
data class SpecialOpeningTimeMetadata(
    val erpModel: SpecialOpeningTimeErpModel,
    val isActive: Boolean,
    val isInPast: Boolean
)

/**
 * Represents a period with optional time information.
 */
@Serializable
data class FhirPharmacyErpModelPeriod(
    val start: FhirTemporal?,
    val end: FhirTemporal?
)

/**
 * Data class representing a period when the pharmacy is not available.
 * @param description Reason for not being available.
 * @param period Date and optional time range.
 * @param overlapsAvailableTime True if the closing time overlaps with availableTime.
 */
@Serializable
data class NotAvailablePeriodErpModel(
    val description: String?,
    val period: FhirPharmacyErpModelPeriod
)

/**
 * Data class representing a special opening time for the pharmacy.
 * @param description A description for the special opening time.
 * @param period The period during which the special opening time is valid.
 */
@Serializable
data class SpecialOpeningTimeErpModel(
    val description: String?,
    val period: FhirPharmacyErpModelPeriod?
)

@Serializable
/**
 * Model representing a pharmacy with its availability and not available periods.
 * @property id Pharmacy ID.
 * @property name Pharmacy name.
 * @property telematikId Telematik ID.
 * @property position Position info.
 * @property address Address info.
 * @property contact Contact info.
 * @property specialities List of specialties.
 * @property hoursOfOperation Hours of operation.
 * @property availableTime Available time.
 * @property notAvailablePeriods List of not available periods.
 * @property specialOpeningTimes List of special opening times.
 * @property isClosedToday True if today is a closed day.
 */
data class FhirPharmacyErpModel(
    val id: String?,
    val name: String,
    val telematikId: String,
    val position: FhirPositionErpModel?,
    val address: FhirPharmacyAddressErpModel?,
    val contact: FhirContactInformationErpModel,
    val specialities: List<FhirVzdSpecialtyType> = emptyList(),
    @Deprecated("value will always be null in fhirvzd")
    val hoursOfOperation: OpeningHoursErpModel? = null, // hoursOfOperation (only available from apo-vzd)
    val availableTime: OpeningHoursErpModel, // availableTime
    val notAvailablePeriods: List<NotAvailablePeriodErpModel> = emptyList(),
    val specialOpeningTimes: List<SpecialOpeningTimeErpModel> = emptyList(),
    val isClosedToday: Boolean = false
) {
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun notAvailablePeriodsWithMetadata(
        now: Instant = Clock.System.now(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): List<NotAvailablePeriodMetadata> = notAvailablePeriods.map { notAvailablePeriod ->
        val hasOverlap = notAvailablePeriod.overlapsWithAvailableTime(availableTime, timeZone)
        val isActive = notAvailablePeriod.isActiveNow(now, timeZone)
        val isInPast = notAvailablePeriod.isInPast(now, timeZone)
        NotAvailablePeriodMetadata(
            erpModel = notAvailablePeriod,
            hasOverlap = hasOverlap,
            isActive = isActive,
            isInPast = isInPast
        )
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    fun specialOpeningTimesWithMetadata(
        now: Instant = Clock.System.now(),
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): List<SpecialOpeningTimeMetadata> = specialOpeningTimes.map { specialOpeningTime ->
        val isActive = specialOpeningTime.isActiveNow(now, timeZone)
        val isInPast = specialOpeningTime.isInPast(now, timeZone)
        SpecialOpeningTimeMetadata(
            erpModel = specialOpeningTime,
            isActive = isActive,
            isInPast = isInPast
        )
    }

    companion object {
        fun FhirPharmacyErpModel.toJson(): String {
            return SafeJson.value.encodeToString(this)
        }
    }
}

/**
 * Extension function to check if a period is in the past.
 * SHARED logic used by both NotAvailablePeriodErpModel and SpecialOpeningTimeErpModel.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
fun FhirPharmacyErpModelPeriod.isInPast(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean {
    return try {
        val periodEnd = this.end?.toComparableDate(timeZone)
        periodEnd?.let { end ->
            val currentDate = now.toLocalDateTime(timeZone).date
            end < currentDate
        } ?: false
    } catch (e: Exception) {
        Napier.e(e) {
            "Failed to check if period is in the past. " +
                "Period: $this. " +
                "Stack trace: ${e.stackTraceToString()}"
        }
        false
    }
}

/**
 * Checks if a period is currently active (now between start and end).
 * SHARED logic used by both NotAvailablePeriodErpModel and SpecialOpeningTimeErpModel.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
fun FhirPharmacyErpModelPeriod.isActiveNow(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean {
    return try {
        val start = this.start?.toInstant(timeZone)
        val end = this.end?.toInstant(timeZone)
        val isActive = if (start != null && end != null) {
            now >= start && now <= end
        } else {
            false
        }
        isActive
    } catch (e: Exception) {
        Napier.e(e) {
            "Exception in isActiveNow: ${e.message}\n${e.stackTraceToString()}"
        }
        false
    }
}

/**
 * Extension function to check if a NotAvailablePeriodErpModel overlaps with available time.
 * Checks if the not-available period's day-of-week window contains any pharmacy opening days,
 * then performs time comparison within those matching days.
 * @param availableTime The pharmacy's available time schedule
 * @return true if the not available period overlaps with any available time
 */
fun NotAvailablePeriodErpModel.overlapsWithAvailableTime(
    availableTime: OpeningHoursErpModel,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean {
    return try {
        // Extract day of week and time from the not available period
        val notAvailableStartDate = this.period.start?.toDayOfTheWeek()
        val notAvailableEndDate = this.period.end?.toDayOfTheWeek()
        val notAvailableStartTime = this.period.start?.toLocalTime(timeZone)
        val notAvailableEndTime = this.period.end?.toLocalTime(timeZone)

        // Check each day in available time schedule
        availableTime.openingTime.forEach { (dayOfTheWeek, openingTimes) ->
            // Check if this pharmacy opening day falls within the not-available period's day window
            if (isDayInNotAvailableWindow(dayOfTheWeek, notAvailableStartDate, notAvailableEndDate)) {
                openingTimes.forEach { openingTime ->
                    // If no time information in not-available period, any day match is an overlap
                    if (notAvailableStartTime == null && notAvailableEndTime == null) {
                        return true
                    }

                    // Perform time comparison within the matching day
                    if (hasTimeOverlap(notAvailableStartTime, notAvailableEndTime, openingTime)) {
                        return true
                    }
                }
            }
        }

        false
    } catch (e: Exception) {
        Napier.e(e) {
            "Exception during temporal conversion or time comparison. " +
                "FhirPharmacyErpModelPeriod: ${this.period}, Description: ${this.description}. " +
                "Stack trace: ${e.stackTraceToString()}"
        }
        false // Return false if any temporal conversion fails
    }
}

/**
 * Extension function to check if a NotAvailablePeriodErpModel is in the past.
 * Delegates to shared FhirPharmacyErpModelPeriod.isInPast()
 */
@OptIn(kotlin.time.ExperimentalTime::class)
fun NotAvailablePeriodErpModel.isInPast(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean = this.period.isInPast(now, timeZone)

/**
 * Checks if NotAvailablePeriodErpModel is currently active.
 * Delegates to shared FhirPharmacyErpModelPeriod.isActiveNow()
 */
@OptIn(kotlin.time.ExperimentalTime::class)
fun NotAvailablePeriodErpModel.isActiveNow(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean = this.period.isActiveNow(now, timeZone)

/**
 * Extension function to check if a SpecialOpeningTimeErpModel is in the past.
 * Delegates to shared FhirPharmacyErpModelPeriod.isInPast()
 */
@OptIn(kotlin.time.ExperimentalTime::class)
fun SpecialOpeningTimeErpModel.isInPast(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean = this.period?.isInPast(now, timeZone) ?: false

/**
 * Checks if SpecialOpeningTimeErpModel is currently active.
 * Delegates to shared FhirPharmacyErpModelPeriod.isActiveNow()
 */
@OptIn(kotlin.time.ExperimentalTime::class)
fun SpecialOpeningTimeErpModel.isActiveNow(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean = this.period?.isActiveNow(now, timeZone) ?: false

private fun FhirTemporal.toComparableDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate? {
    return when (this) {
        is FhirTemporal.Instant -> this.value.toLocalDateTime(timeZone).date
        is FhirTemporal.LocalDate -> this.value
        is FhirTemporal.LocalDateTime -> this.value.date
        is FhirTemporal.YearMonth -> null
        is FhirTemporal.Year -> null
        is FhirTemporal.LocalTime -> null // Time only, can't determine date
    }
}

/**
 * Extract LocalTime from FhirTemporal. Only returns LocalTime for time-specific temporal types.
 */
private fun FhirTemporal.toLocalTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): kotlinx.datetime.LocalTime? {
    return when (this) {
        is FhirTemporal.Instant -> this.value.toLocalDateTime(timeZone).time
        is FhirTemporal.LocalDateTime -> this.value.time
        is FhirTemporal.LocalTime -> this.value
        else -> null
    }
}

private fun FhirTemporal.toDayOfTheWeek(): DayOfWeek? {
    return when (this) {
        is FhirTemporal.Instant -> this.value.toLocalDate().dayOfWeek
        is FhirTemporal.LocalDateTime -> this.value.date.dayOfWeek
        is FhirTemporal.LocalDate -> this.value.dayOfWeek
        else -> null
    }
}

/**
 * Simple time overlap check between not available period and opening time.
 * Only performs LocalTime comparison without date considerations.
 */
private fun hasTimeOverlap(
    notAvailableStartTime: kotlinx.datetime.LocalTime?,
    notAvailableEndTime: kotlinx.datetime.LocalTime?,
    openingTime: OpeningTimeErpModel
): Boolean {
    // If opening time covers all day (no specific times), any time-specific closure overlaps
    if (openingTime.isAllDayOpen()) {
        return notAvailableStartTime != null || notAvailableEndTime != null
    }

    // If no opening times are defined, no overlap
    val openingStartTime = openingTime.openingTime
    val openingEndTime = openingTime.closingTime

    if (openingStartTime == null && openingEndTime == null) {
        return false
    }

    // Handle cases where not available period has null start/end times
    val naStart = notAvailableStartTime ?: kotlinx.datetime.LocalTime(0, 0) // Start of day if null
    val naEnd = notAvailableEndTime ?: kotlinx.datetime.LocalTime(23, 59, 59) // End of day if null

    // Handle cases where opening time has null start/end times
    val opStart = openingStartTime ?: kotlinx.datetime.LocalTime(0, 0) // Start of day if null
    val opEnd = openingEndTime ?: kotlinx.datetime.LocalTime(23, 59, 59) // End of day if null

    // Check for overlap: intervals overlap if start1 <= end2 && start2 <= end1
    return naStart <= opEnd && opStart <= naEnd
}

/**
 * Check if a pharmacy's opening day falls within the not-available period's day-of-week window
 */
private fun isDayInNotAvailableWindow(
    pharmacyDay: DayOfWeek,
    notAvailableStartDay: DayOfWeek?,
    notAvailableEndDay: DayOfWeek?
): Boolean {
    // If no day information in not-available period, consider it as no day restriction
    if (notAvailableStartDay == null && notAvailableEndDay == null) {
        return true
    }

    // If only start day is available, check if pharmacy day matches or comes after
    if (notAvailableStartDay != null && notAvailableEndDay == null) {
        return pharmacyDay.ordinal >= notAvailableStartDay.ordinal
    }

    // If only end day is available, check if pharmacy day matches or comes before
    if (notAvailableStartDay == null && notAvailableEndDay != null) {
        return pharmacyDay.ordinal <= notAvailableEndDay.ordinal
    }

    // Both start and end days are available
    val startOrdinal = notAvailableStartDay!!.ordinal
    val endOrdinal = notAvailableEndDay!!.ordinal
    val pharmacyOrdinal = pharmacyDay.ordinal

    return if (startOrdinal <= endOrdinal) {
        // Same week: Monday(0) to Wednesday(2)
        pharmacyOrdinal in startOrdinal..endOrdinal
    } else {
        // Week wrap-around: Friday(4) to Monday(0) of next week
        pharmacyOrdinal >= startOrdinal || pharmacyOrdinal <= endOrdinal
    }
}
