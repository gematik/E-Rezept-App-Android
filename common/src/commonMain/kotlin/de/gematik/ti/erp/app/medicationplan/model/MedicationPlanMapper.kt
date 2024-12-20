/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.medicationplan.model

import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationDosageEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationNotificationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleEntityV1
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.toRatio
import de.gematik.ti.erp.app.utils.toLocalDate
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.days

private val MORNING_HOUR = LocalTime.parse("08:00")
private val NOON_HOUR = LocalTime.parse("12:00")
private val EVENING_HOUR = LocalTime.parse("18:00")
private val NIGHT_HOUR = LocalTime.parse("20:00")

fun MedicationDosageEntityV1.toMedicationDosage(): MedicationDosage = MedicationDosage(
    form = this.form,
    ratio = this.ratio
)

fun RealmList<MedicationNotificationEntityV1>.toNotifications(): List<MedicationNotification> =
    this.map { notification ->
        MedicationNotification(
            time = LocalDateTime.parse(notification.time).time,
            dosage = notification.dosage?.toMedicationDosage() ?: MedicationDosage("", ""),
            id = notification.id
        )
    }

fun MedicationScheduleEntityV1.update(newMedicationSchedule: MedicationSchedule) {
    this.apply {
        this.start = newMedicationSchedule.start.toString()
        this.end = newMedicationSchedule.end.toString()
        this.title = newMedicationSchedule.message.title
        this.body = newMedicationSchedule.message.body
        this.taskId = newMedicationSchedule.taskId
        this.notifications = newMedicationSchedule.notifications.map { notification ->
            MedicationNotificationEntityV1().apply {
                time = notification.time.toString()
                dosage = MedicationDosageEntityV1().apply {
                    this.form = notification.dosage.form
                    this.ratio = notification.dosage.ratio
                }
            }
        }.toRealmList()
    }
}

fun MedicationScheduleEntityV1.toMedicationSchedule() =
    MedicationSchedule(
        start = LocalDate.parse(this.start),
        end = LocalDate.parse(this.end),
        isActive = this.isActive,
        message = MedicationNotificationMessage(
            title = this.title,
            body = this.body
        ),
        taskId = this.taskId,
        profileId = this.profileId,
        amount = this.amount.toRatio() ?: Ratio(Quantity("", ""), Quantity("", "")),
        notifications = this.notifications.toNotifications()
    )

fun PrescriptionData.Prescription.toMedicationSchedule(
    now: Instant = Clock.System.now()
): MedicationSchedule {
    when (this) {
        is PrescriptionData.Scanned -> {
            return MedicationSchedule(
                start = now.toLocalDateTime(TimeZone.currentSystemDefault()).date,
                end = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(DatePeriod(days = 10)),
                isActive = false,
                message = MedicationNotificationMessage(
                    title = this.name,
                    body = ""
                ),
                taskId = this.taskId,
                profileId = this.profileId,
                amount = null,
                notifications = emptyList()
            )
        }

        is PrescriptionData.Synced -> {
            val dosageInstruction = parseInstruction(this.medicationRequest.dosageInstruction)
            val amount = getAmount(this.medicationRequest)

            return MedicationSchedule(
                start = now.toLocalDate(),
                end = getCalculatedEndDate(
                    start = now,
                    amount = amount,
                    dosageInstruction = dosageInstruction,
                    form = medicationRequest.medication?.form ?: ""
                ),
                isActive = false,
                message = MedicationNotificationMessage(
                    title = this.medicationRequest.medication?.name() ?: "",
                    body = ""
                ),
                amount = amount,
                taskId = this.taskId,
                profileId = this.profileId,
                notifications = mapDosageInstructionToNotifications(
                    dosageInstruction,
                    medicationRequest.medication?.form
                )
            )
        }
    }
}

fun getCalculatedEndDate(
    start: Instant = Clock.System.now(),
    amount: Ratio,
    dosageInstruction: MedicationPlanDosageInstruction,
    form: String
): LocalDate {
    return if (pieceableForm.contains(form)) {
        when (dosageInstruction) {
            // 1-0-1
            is MedicationPlanDosageInstruction.Structured -> {
                val currentTime = start.toLocalDateTime(TimeZone.currentSystemDefault()).time

                val amountToConsumePerDay = dosageInstruction.interpretation.entries.map {
                    it.value.toFloatOrNull() ?: 1f
                }.sum()

                val amountToConsumeToDay = dosageInstruction.interpretation.map { (dayTime, amount) ->
                    val hour = when (dayTime) {
                        MedicationPlanDosageInstruction.DayTime.MORNING -> MORNING_HOUR
                        MedicationPlanDosageInstruction.DayTime.NOON -> NOON_HOUR
                        MedicationPlanDosageInstruction.DayTime.EVENING -> EVENING_HOUR
                        MedicationPlanDosageInstruction.DayTime.NIGHT -> NIGHT_HOUR
                    }
                    if (currentTime <= hour) {
                        amount.toFloatOrNull() ?: 0f
                    } else {
                        0f
                    }
                }.sum()

                val amountInPackage = amount?.numerator?.value?.toInt() ?: 1

                val daysLeft = (amountInPackage / amountToConsumePerDay).takeIf { amountToConsumePerDay != 0f } ?: 0
                val adjustedDaysLeft = daysLeft.toLong() - if (amountToConsumeToDay == amountToConsumePerDay) 1 else 0
                start.plus(adjustedDaysLeft.days).toLocalDate()
            } else -> start.toLocalDate()
        }
    } else {
        start.toLocalDate()
    }
}

fun getAmount(medicationRequest: SyncedTaskData.MedicationRequest): Ratio {
    val nrOfPackages = medicationRequest.quantity
    return medicationRequest.medication?.let { medication ->
        multiplyMedicationAmount(medication.amount, nrOfPackages)
    } ?: Ratio(
        numerator = Quantity(
            value = "1",
            unit = ""
        ),
        denominator = Quantity(
            value = "1",
            unit = ""
        )
    )
}

fun multiplyMedicationAmount(value: Ratio?, multiplier: Int): Ratio? {
    return value?.numerator?.value?.replace(",", ".")?.toFloatOrNull()?.let { number ->
        val result = number * multiplier
        val formattedResult = if (result % 1 == 0f) result.toInt().toString() else result.toString()
        value.copy(
            numerator = Quantity(
                value = formattedResult,
                unit = value.numerator.unit
            )
        )
    }
}

fun mapDosageInstructionToNotifications(
    dosageInstruction: MedicationPlanDosageInstruction,
    form: String?
): List<MedicationNotification> {
    return when (dosageInstruction) {
        is MedicationPlanDosageInstruction.Structured -> {
            dosageInstruction.interpretation.map { (dayTime, dosage) ->
                MedicationNotification(
                    time = when (dayTime) {
                        MedicationPlanDosageInstruction.DayTime.MORNING -> MORNING_HOUR
                        MedicationPlanDosageInstruction.DayTime.NOON -> NOON_HOUR
                        MedicationPlanDosageInstruction.DayTime.EVENING -> EVENING_HOUR
                        MedicationPlanDosageInstruction.DayTime.NIGHT -> NIGHT_HOUR
                    },
                    dosage = MedicationDosage(
                        form = form ?: "",
                        ratio = dosage
                    ),
                    id = UUID.randomUUID().toString()
                )
            }
        }
        else -> emptyList()
    }
}

fun parseInstruction(dosageInstruction: String?): MedicationPlanDosageInstruction {
    return dosageInstruction?.let { instruction ->

        val trimmedInstruction = trimInstruction(instruction)
        val interpretation = interpretDosage(trimmedInstruction)

        if (interpretation.isEmpty()) {
            when {
                trimmedInstruction.lowercase() == "dj" -> MedicationPlanDosageInstruction.External
                trimmedInstruction.isNotEmpty() -> MedicationPlanDosageInstruction.FreeText(instruction)
                else -> MedicationPlanDosageInstruction.Empty
            }
        } else {
            val filtered = interpretation.filter { it.value != "0" }
            if (filtered.isEmpty()) {
                MedicationPlanDosageInstruction.Empty
            } else {
                MedicationPlanDosageInstruction.Structured(
                    text = instruction,
                    interpretation = filtered
                )
            }
        }
    } ?: MedicationPlanDosageInstruction.Empty
}

fun trimInstruction(instruction: String): String = instruction.trimStart(' ', '<', '>').trimEnd(' ', '<', '>')

private fun interpretDosage(cleanedDosage: String): Map<MedicationPlanDosageInstruction.DayTime, String> {
    val parts = cleanedDosage.split("-").map { it.trim() }
    return parts.mapIndexedNotNull { index, part ->
        when {
            parts.size <= MedicationPlanDosageInstruction.DayTime.entries.size &&
                part.matches(Regex("[0-9,.½/ ]+")) -> {
                MedicationPlanDosageInstruction.DayTime.entries.getOrNull(index)?.let { time ->
                    time to part
                }
            }
            else -> {
                null
            }
        }
    }.toMap()
}

// see kbvCodeMapping for more information
val pieceableForm = listOf(
    "AMP",
    "BEU",
    "BON",
    "BTA",
    "DKA",
    "DRA",
    "DRM",
    "FDA",
    "FER",
    "FMR",
    "FTA",
    "GLO",
    "HKM",
    "HKP",
    "HPI",
    "HVW",
    "KAP",
    "KDA",
    "KGU",
    "KLI",
    "KLT",
    "KMP",
    "KMR",
    "KOD",
    "KTA",
    "LTA",
    "LUP",
    "LUT",
    "MRP",
    "MTA",
    "PAS",
    "PEL",
    "PEN",
    "PER",
    "RED",
    "REK",
    "RET",
    "RKA",
    "RUT",
    "SMT",
    "SUT",
    "TAB",
    "TAE",
    "TKA",
    "TLE",
    "TMR",
    "TRT",
    "TSD",
    "TSE",
    "TVW",
    "UTA",
    "VKA",
    "VTA",
    "WKA",
    "WKM",
    "XGM",
    "ZKA"
)
