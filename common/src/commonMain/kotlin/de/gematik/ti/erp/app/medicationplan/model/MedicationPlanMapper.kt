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

import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MedicationScheduleEntityV1
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MedicationScheduleNotificationDosageEntityV1
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MedicationScheduleNotificationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleDurationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleDurationTypeV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleIntervalEntityV1
import de.gematik.ti.erp.app.db.entities.v1.medicationplan.MedicationScheduleIntervalTypeV1
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.toRatio
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.ext.toRealmSet
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

val MORNING_HOUR = LocalTime.parse("08:00")
val NOON_HOUR = LocalTime.parse("12:00")
val EVENING_HOUR = LocalTime.parse("18:00")
val NIGHT_HOUR = LocalTime.parse("20:00")

// Data -> Entity
fun MedicationSchedule.toMedicationScheduleEntityV1(): MedicationScheduleEntityV1 =
    MedicationScheduleEntityV1().apply {
        taskId = this@toMedicationScheduleEntityV1.taskId
        amount = this@toMedicationScheduleEntityV1.amount?.toRatioEntity()
        isActive = this@toMedicationScheduleEntityV1.isActive
        profileId = this@toMedicationScheduleEntityV1.profileId
        duration = this@toMedicationScheduleEntityV1.duration.toMedicationScheduleDurationEntityV1()
        interval = this@toMedicationScheduleEntityV1.interval.toMedicationScheduleIntervalEntityV1()
        title = this@toMedicationScheduleEntityV1.message.title
        body = this@toMedicationScheduleEntityV1.message.body
        notifications = this@toMedicationScheduleEntityV1.notifications.map { it.toMedicationScheduleNotificationEntityV1() }.toRealmList()
    }
fun MedicationScheduleDuration.toMedicationScheduleDurationEntityV1(): MedicationScheduleDurationEntityV1 =
    MedicationScheduleDurationEntityV1().apply {
        startDate = this@toMedicationScheduleDurationEntityV1.startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toRealmInstant()
        endDate = this@toMedicationScheduleDurationEntityV1.endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toRealmInstant()
        type = when (this@toMedicationScheduleDurationEntityV1) {
            is MedicationScheduleDuration.Endless -> MedicationScheduleDurationTypeV1.ENDLESS
            is MedicationScheduleDuration.EndOfPack -> MedicationScheduleDurationTypeV1.END_OF_PACK
            is MedicationScheduleDuration.Personalized -> MedicationScheduleDurationTypeV1.PERSONALIZED
        }
    }

fun MedicationScheduleInterval.toMedicationScheduleIntervalEntityV1(): MedicationScheduleIntervalEntityV1 =
    MedicationScheduleIntervalEntityV1().apply {
        when (this@toMedicationScheduleIntervalEntityV1) {
            is MedicationScheduleInterval.Daily -> {
                this.type = MedicationScheduleIntervalTypeV1.DAILY
                this.selectedDaysStrings = realmSetOf()
            }
            is MedicationScheduleInterval.EveryTwoDays -> {
                this.type = MedicationScheduleIntervalTypeV1.EVERY_TWO_DAYS
                this.selectedDaysStrings = realmSetOf()
            }
            is MedicationScheduleInterval.Personalized -> {
                this.type = MedicationScheduleIntervalTypeV1.PERSONALIZED
                this.selectedDaysStrings = this@toMedicationScheduleIntervalEntityV1.selectedDays.map { it.value }.toRealmSet()
            }
        }
    }

fun MedicationScheduleNotification.toMedicationScheduleNotificationEntityV1(): MedicationScheduleNotificationEntityV1 =
    MedicationScheduleNotificationEntityV1().apply {
        id = this@toMedicationScheduleNotificationEntityV1.id
        time = this@toMedicationScheduleNotificationEntityV1.time.toString()
        dosage = this@toMedicationScheduleNotificationEntityV1.dosage.toMedicationScheduleNotificationDosageEntityV1()
    }

fun MedicationScheduleNotificationDosage.toMedicationScheduleNotificationDosageEntityV1(): MedicationScheduleNotificationDosageEntityV1 =
    MedicationScheduleNotificationDosageEntityV1().apply {
        this.form = this@toMedicationScheduleNotificationDosageEntityV1.form
        this.ratio = this@toMedicationScheduleNotificationDosageEntityV1.ratio
    }

// Entity -> Data
fun MedicationScheduleEntityV1.toMedicationSchedule() =
    MedicationSchedule(
        isActive = this.isActive,
        profileId = this.profileId,
        taskId = this.taskId,
        amount = this.amount.toRatio() ?: Ratio(Quantity("", ""), Quantity("", "")),
        interval = this.interval?.toMedicationScheduleInterval() ?: MedicationScheduleInterval.Daily,
        duration = this.duration?.toMedicationScheduleDuration() ?: MedicationScheduleDuration.Endless(),
        message = MedicationNotificationMessage(
            title = this.title,
            body = this.body
        ),
        notifications = this.notifications.map { it.toMedicationScheduleNotification() }.sortedBy { it.time }
    )

fun MedicationScheduleDurationEntityV1.toMedicationScheduleDuration(): MedicationScheduleDuration {
    val parsedStartDate = this.startDate.toInstant().toLocalDate()
    val parsedEndDate = this.endDate.toInstant().toLocalDate()
    return when (this.type) {
        MedicationScheduleDurationTypeV1.ENDLESS -> MedicationScheduleDuration.Endless(
            startDate = parsedStartDate,
            endDate = parsedEndDate
        )
        MedicationScheduleDurationTypeV1.END_OF_PACK -> MedicationScheduleDuration.EndOfPack(
            startDate = parsedStartDate,
            endDate = parsedEndDate
        )
        MedicationScheduleDurationTypeV1.PERSONALIZED -> MedicationScheduleDuration.Personalized(
            startDate = parsedStartDate,
            endDate = parsedEndDate
        )
    }
}

fun MedicationScheduleIntervalEntityV1.toMedicationScheduleInterval(): MedicationScheduleInterval {
    return when (this.type) {
        MedicationScheduleIntervalTypeV1.DAILY -> MedicationScheduleInterval.Daily
        MedicationScheduleIntervalTypeV1.EVERY_TWO_DAYS -> MedicationScheduleInterval.EveryTwoDays
        MedicationScheduleIntervalTypeV1.PERSONALIZED -> MedicationScheduleInterval.Personalized(
            selectedDays = this.selectedDaysStrings.map { DayOfWeek(it) }.toSet()
        )
    }
}

fun MedicationScheduleNotificationEntityV1.toMedicationScheduleNotification(): MedicationScheduleNotification =
    MedicationScheduleNotification(
        time = LocalTime.parse(this.time),
        dosage = this.dosage?.toMedicationScheduleNotificationDosage() ?: MedicationScheduleNotificationDosage("", ""),
        id = this.id
    )

fun MedicationScheduleNotificationDosageEntityV1.toMedicationScheduleNotificationDosage(): MedicationScheduleNotificationDosage =
    MedicationScheduleNotificationDosage(
        form = this.form,
        ratio = this.ratio
    )

// Prescription -> MedicationSchedule
// initialise every new MedicationSchedule as inactive, endless and daily MedicationSchedule
fun PrescriptionData.Prescription.toMedicationSchedule(
    now: Instant = Clock.System.now()
): MedicationSchedule {
    when (this) {
        is PrescriptionData.Scanned -> {
            return MedicationSchedule(
                isActive = false,
                profileId = this.profileId,
                taskId = this.taskId,
                amount = null,
                duration = MedicationScheduleDuration.Endless(
                    startDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                ),
                interval = MedicationScheduleInterval.Daily,
                message = MedicationNotificationMessage(
                    title = this.name,
                    body = ""
                ),
                notifications = emptyList()
            )
        }

        is PrescriptionData.Synced -> {
            val dosageInstruction = parseInstruction(this.medicationRequest.dosageInstruction)
            val amount = getAmount(this.medicationRequest)

            return MedicationSchedule(
                isActive = false,
                profileId = this.profileId,
                taskId = this.taskId,
                amount = amount,
                duration = MedicationScheduleDuration.Endless(
                    startDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                ),
                interval = MedicationScheduleInterval.Daily,
                message = MedicationNotificationMessage(
                    title = this.medicationRequest.medication?.name() ?: "",
                    body = ""
                ),
                notifications = mapDosageInstructionToNotifications(
                    dosageInstruction,
                    medicationRequest.medication?.form
                )
            )
        }
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
): List<MedicationScheduleNotification> {
    return when (dosageInstruction) {
        is MedicationPlanDosageInstruction.Structured -> {
            dosageInstruction.interpretation.map { (dayTime, dosage) ->
                MedicationScheduleNotification(
                    time = when (dayTime) {
                        MedicationPlanDosageInstruction.DayTime.MORNING -> MORNING_HOUR
                        MedicationPlanDosageInstruction.DayTime.NOON -> NOON_HOUR
                        MedicationPlanDosageInstruction.DayTime.EVENING -> EVENING_HOUR
                        MedicationPlanDosageInstruction.DayTime.NIGHT -> NIGHT_HOUR
                    },
                    dosage = MedicationScheduleNotificationDosage(
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
