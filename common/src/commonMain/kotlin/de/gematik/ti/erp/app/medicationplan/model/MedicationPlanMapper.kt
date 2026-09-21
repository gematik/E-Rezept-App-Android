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

import de.gematik.ti.erp.app.database.realm.v1.medicationplan.EVENING_HOUR
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MORNING_HOUR
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.NIGHT_HOUR
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.NOON_HOUR
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.task.model.QuantityErpModel
import de.gematik.ti.erp.app.task.model.RatioErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

// Prescription -> MedicationSchedule
// initialise every new MedicationSchedule as inactive, endless and daily MedicationSchedule
fun PrescriptionData.Prescription.toMedicationSchedule(
    now: Instant = Clock.System.now()
): MedicationScheduleErpModel {
    when (this) {
        is PrescriptionData.Scanned -> {
            return MedicationScheduleErpModel(
                isActive = false,
                profileId = this.profileId,
                taskId = this.taskId,
                amount = null,
                duration = MedicationScheduleDurationErpModel.Endless(
                    startDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                ),
                interval = MedicationScheduleIntervalErpModel.Daily,
                message = MedicationNotificationMessageErpModel(
                    title = this.name,
                    body = ""
                ),
                notifications = emptyList()
            )
        }

        is PrescriptionData.Synced -> {
            val dosageInstruction = parseInstruction(this.medicationRequest.dosageInstruction)
            val amount = getAmount(this.medicationRequest)

            return MedicationScheduleErpModel(
                isActive = false,
                profileId = this.profileId,
                taskId = this.taskId,
                amount = amount,
                duration = MedicationScheduleDurationErpModel.Endless(
                    startDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                ),
                interval = MedicationScheduleIntervalErpModel.Daily,
                message = MedicationNotificationMessageErpModel(
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

fun getAmount(medicationRequest: SyncedTaskData.MedicationRequest): RatioErpModel {
    val nrOfPackages = medicationRequest.quantity
    return medicationRequest.medication?.let { medication ->
        multiplyMedicationAmount(
            RatioErpModel(
                numerator = medication.amount?.numerator?.let { QuantityErpModel(it.value, it.unit) },
                denominator = medication.amount?.denominator?.let { QuantityErpModel(it.value, it.unit) }
            ),
            nrOfPackages
        )
    } ?: RatioErpModel(
        numerator = QuantityErpModel(
            value = "1",
            unit = ""
        ),
        denominator = QuantityErpModel(
            value = "1",
            unit = ""
        )
    )
}

fun multiplyMedicationAmount(value: RatioErpModel?, multiplier: Int): RatioErpModel? {
    return value?.numerator?.value?.replace(",", ".")?.toFloatOrNull()?.let { number ->
        val result = number * multiplier
        val formattedResult = if (result % 1 == 0f) result.toInt().toString() else result.toString()
        value.copy(
            numerator = QuantityErpModel(
                value = formattedResult,
                unit = value.numerator?.unit ?: ""
            )
        )
    }
}

fun mapDosageInstructionToNotifications(
    dosageInstruction: MedicationPlanDosageInstructionErpModel,
    form: String?
): List<MedicationScheduleNotificationErpModel> {
    return when (dosageInstruction) {
        is MedicationPlanDosageInstructionErpModel.Structured -> {
            dosageInstruction.interpretation.map { (dayTime, dosage) ->
                MedicationScheduleNotificationErpModel(
                    time = when (dayTime) {
                        MedicationPlanDosageInstructionErpModel.DayTime.MORNING -> MORNING_HOUR
                        MedicationPlanDosageInstructionErpModel.DayTime.NOON -> NOON_HOUR
                        MedicationPlanDosageInstructionErpModel.DayTime.EVENING -> EVENING_HOUR
                        MedicationPlanDosageInstructionErpModel.DayTime.NIGHT -> NIGHT_HOUR
                    },
                    dosage = MedicationScheduleNotificationDosageErpModel(
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

fun parseInstruction(dosageInstruction: String?): MedicationPlanDosageInstructionErpModel {
    return dosageInstruction?.let { instruction ->

        val trimmedInstruction = trimInstruction(instruction)
        val interpretation = interpretDosage(trimmedInstruction)

        if (interpretation.isEmpty()) {
            when {
                trimmedInstruction.lowercase() == "dj" -> MedicationPlanDosageInstructionErpModel.External
                trimmedInstruction.isNotEmpty() -> MedicationPlanDosageInstructionErpModel.FreeText(instruction)
                else -> MedicationPlanDosageInstructionErpModel.Empty
            }
        } else {
            val filtered = interpretation.filter { it.value != "0" }
            if (filtered.isEmpty()) {
                MedicationPlanDosageInstructionErpModel.Empty
            } else {
                MedicationPlanDosageInstructionErpModel.Structured(
                    text = instruction,
                    interpretation = filtered
                )
            }
        }
    } ?: MedicationPlanDosageInstructionErpModel.Empty
}

fun trimInstruction(instruction: String): String = instruction.trimStart(' ', '<', '>').trimEnd(' ', '<', '>')

private fun interpretDosage(cleanedDosage: String): Map<MedicationPlanDosageInstructionErpModel.DayTime, String> {
    val parts = cleanedDosage.split("-").map { it.trim() }
    return parts.mapIndexedNotNull { index, part ->
        when {
            parts.size <= MedicationPlanDosageInstructionErpModel.DayTime.entries.size &&
                part.matches(Regex("[0-9,.½/ ]+")) -> {
                MedicationPlanDosageInstructionErpModel.DayTime.entries.getOrNull(index)?.let { time ->
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

fun TaskErpModel.toMedicationSchedule(now: Instant = Clock.System.now()): MedicationScheduleErpModel {
    return when (this) {
        is TaskErpModel.Scanned -> MedicationScheduleErpModel(
            isActive = false,
            profileId = this.profileId,
            taskId = this.taskId,
            amount = null,
            duration = MedicationScheduleDurationErpModel.Endless(
                startDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            ),
            interval = MedicationScheduleIntervalErpModel.Daily,
            message = MedicationNotificationMessageErpModel(
                title = this.name ?: "",
                body = ""
            ),
            notifications = emptyList()
        )

        is TaskErpModel.Synced.Prescription -> {
            val dosageInstruction = parseInstruction(this.medicationRequest?.dosageInstruction)
            val amount = this.medicationRequest?.let { req ->
                req.medication?.let { med ->
                    val ratio = med.amount?.let { r ->
                        RatioErpModel(
                            numerator = r.numerator?.let { QuantityErpModel(it.value, it.unit) },
                            denominator = r.denominator?.let { QuantityErpModel(it.value, it.unit) }
                        )
                    }
                    multiplyMedicationAmount(ratio, req.quantity)
                } ?: RatioErpModel(numerator = QuantityErpModel("1", ""), denominator = QuantityErpModel("1", ""))
            }
            MedicationScheduleErpModel(
                isActive = false,
                profileId = this.profileId,
                taskId = this.taskId,
                amount = amount,
                duration = MedicationScheduleDurationErpModel.Endless(
                    startDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                ),
                interval = MedicationScheduleIntervalErpModel.Daily,
                message = MedicationNotificationMessageErpModel(
                    title = this.medicationRequest?.medication?.name() ?: "",
                    body = ""
                ),
                notifications = mapDosageInstructionToNotifications(
                    dosageInstruction,
                    this.medicationRequest?.medication?.form
                )
            )
        }

        is TaskErpModel.Synced.Diga -> MedicationScheduleErpModel(
            isActive = false,
            profileId = this.profileId,
            taskId = this.taskId,
            amount = null,
            duration = MedicationScheduleDurationErpModel.Endless(
                startDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
            ),
            interval = MedicationScheduleIntervalErpModel.Daily,
            message = MedicationNotificationMessageErpModel(
                title = this.name ?: "",
                body = ""
            ),
            notifications = emptyList()
        )
    }
}
