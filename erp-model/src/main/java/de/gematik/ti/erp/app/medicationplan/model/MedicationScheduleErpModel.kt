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

package de.gematik.ti.erp.app.medicationplan.model

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.RatioErpModel
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.time.Duration.Companion.days

@Serializable
data class MedicationScheduleErpModel(
    val isActive: Boolean,
    val profileId: ProfileIdentifier,
    // task
    val taskId: String,
    val amount: RatioErpModel?,
    // timing
    val duration: MedicationScheduleDurationErpModel,
    val interval: MedicationScheduleIntervalErpModel,
    // message
    val message: MedicationNotificationMessageErpModel,
    val notifications: List<MedicationScheduleNotificationErpModel>
) {
    companion object {
        fun MedicationScheduleErpModel.toJson(): String = SafeJson.value.encodeToString(this)
    }
    fun shouldBeScheduled(localDateNow: LocalDate): Boolean =
        this.isActive &&
            this.duration.endDate >= localDateNow &&
            this.notifications.isNotEmpty()

    fun calculateEndOfPack(
        currentDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        startDate: LocalDate = this.duration.startDate
    ): LocalDate {
        val amountInPackage = if (this.amount?.numerator?.value?.isNotNullOrEmpty() == true) {
            this.amount.numerator.value.toInt()
        } else {
            1
        }
        val amountToConsumePerDay = if (this.notifications.isNotEmpty()) {
            this.notifications.map {
                it.dosage.ratio.toFloatOrNull() ?: 1f
            }.sum()
        } else {
            1f // fallback if notifications are empty (no time selected)
        }

        val amountToConsumeToday = if (
            this.interval is MedicationScheduleIntervalErpModel.Personalized &&
            !this.interval.selectedDays.contains(currentDateTime.dayOfWeek)
        ) {
            0f
        } else {
            this.notifications.map { notification ->
                if (notification.time >= currentDateTime.time) {
                    notification.dosage.ratio.toFloatOrNull() ?: 1f
                } else {
                    0f
                }
            }.sum()
        }

        val amountInPackageRemainingAfterFirstDay = amountInPackage - amountToConsumeToday
        val daysLeft = ceil(amountInPackageRemainingAfterFirstDay / amountToConsumePerDay).takeIf { amountToConsumePerDay != 0f } ?: 0f

        return when (this.interval) {
            is MedicationScheduleIntervalErpModel.Daily -> {
                startDate.plus(DatePeriod(days = daysLeft.toInt()))
            }

            is MedicationScheduleIntervalErpModel.EveryTwoDays -> {
                startDate.plus(DatePeriod(days = daysLeft.toInt() * 2))
            }

            is MedicationScheduleIntervalErpModel.Personalized -> {
                var dayIndexStartingTomorrow = currentDateTime.dayOfWeek.plus(1).ordinal
                val weekDays = DayOfWeek.entries
                var remainingAmount = amountInPackageRemainingAfterFirstDay
                var remainingDays = 0
                while (remainingAmount > 0f) {
                    val currentDay = weekDays[dayIndexStartingTomorrow % 7]
                    if (this.interval.selectedDays.contains(currentDay)) {
                        remainingAmount -= amountToConsumePerDay
                    }
                    remainingDays++
                    dayIndexStartingTomorrow++
                }
                startDate.plus(DatePeriod(days = remainingDays))
            }
        }
    }

    fun calculateNextNotificationTime(): Long {
        val timeZone = TimeZone.currentSystemDefault()
        val currentDate = Clock.System.todayIn(timeZone)
        val currentTime = Clock.System.now().toLocalDateTime(timeZone).time
        val sortedNotification = this.notifications.map { it.time }.sorted()
        val firstNotificationOfADayOffsetInMilliSeconds =
            sortedNotification.first().toMillisecondOfDay().toLong()
        val timeTillEndOfDayOffsetInMilliSeconds = 1.days.inWholeMilliseconds -
            currentTime.toMillisecondOfDay().toLong()

        val calculatedOffSetInMilliseconds = if (this.duration.startDate > currentDate) {
            val startDateOffsetInMilliSeconds = this.duration.startDate.atStartOfDayIn(timeZone).toEpochMilliseconds() -
                currentDate.atStartOfDayIn(timeZone).toEpochMilliseconds()
            timeTillEndOfDayOffsetInMilliSeconds + startDateOffsetInMilliSeconds + firstNotificationOfADayOffsetInMilliSeconds
        } else {
            val nextNotificationOfADayOffsetInMilliSeconds = sortedNotification.firstOrNull { it > currentTime }?.toMillisecondOfDay()?.toLong()
            val shouldBeNotifiedToday = shouldBeNotifiedToday(currentDate)
            if (shouldBeNotifiedToday && nextNotificationOfADayOffsetInMilliSeconds != null) {
                val currentTimeMillis = currentTime.toMillisecondOfDay().toLong()
                nextNotificationOfADayOffsetInMilliSeconds - currentTimeMillis
            } else {
                val nextNotificationDayOffsetInMilliSeconds = calculateNextNotificationDayOffsetInMilliSeconds(currentDate)
                timeTillEndOfDayOffsetInMilliSeconds + nextNotificationDayOffsetInMilliSeconds + firstNotificationOfADayOffsetInMilliSeconds
            }
        }
        return Clock.System.now().toEpochMilliseconds() + calculatedOffSetInMilliseconds
    }

    private fun shouldBeNotifiedToday(currentDate: LocalDate): Boolean =
        when (this.interval) {
            is MedicationScheduleIntervalErpModel.Daily -> true
            is MedicationScheduleIntervalErpModel.EveryTwoDays -> {
                this.duration.startDate.daysUntil(currentDate) % 2 == 0
            }

            is MedicationScheduleIntervalErpModel.Personalized -> {
                this.interval.selectedDays.contains(currentDate.dayOfWeek)
            }
        }

    private fun calculateNextNotificationDayOffsetInMilliSeconds(currentDate: LocalDate): Long {
        return when (this.interval) {
            is MedicationScheduleIntervalErpModel.Daily -> 0L
            is MedicationScheduleIntervalErpModel.EveryTwoDays -> {
                if (this.duration.startDate.daysUntil(currentDate) % 2 == 0) {
                    1.days.inWholeMilliseconds
                } else {
                    0L
                }
            }

            is MedicationScheduleIntervalErpModel.Personalized -> {
                var dayIndexStartingTomorrow = currentDate.dayOfWeek.plus(1).ordinal
                val weekDays = DayOfWeek.entries
                var daysBetween = 0
                while (daysBetween < 7) {
                    val currentDayToCheck = weekDays[dayIndexStartingTomorrow % 7]
                    if (this.interval.selectedDays.contains(currentDayToCheck)) {
                        return daysBetween.days.inWholeMilliseconds
                    }
                    daysBetween++
                    dayIndexStartingTomorrow++
                }
                daysBetween.days.inWholeMilliseconds
            }
        }
    }

    fun daysSinceTheLastNotification(currentDate: LocalDate): Int {
        return when (this.interval) {
            is MedicationScheduleIntervalErpModel.Daily -> 0
            is MedicationScheduleIntervalErpModel.EveryTwoDays -> {
                this.duration.startDate.daysUntil(currentDate) % 2
            }

            is MedicationScheduleIntervalErpModel.Personalized -> {
                var dayIndexStartingToday = currentDate.dayOfWeek.ordinal
                val weekDays = DayOfWeek.entries
                var daysBetween = 0
                while (daysBetween < 7) {
                    val currentDayToCheck = weekDays[dayIndexStartingToday % 7]
                    if (this.interval.selectedDays.contains(currentDayToCheck)) {
                        break
                    }
                    daysBetween++
                    dayIndexStartingToday--
                }
                daysBetween
            }
        }
    }
}
