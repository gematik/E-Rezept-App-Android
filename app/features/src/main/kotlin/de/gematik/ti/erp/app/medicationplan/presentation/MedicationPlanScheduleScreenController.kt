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

package de.gematik.ti.erp.app.medicationplan.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.medicationplan.model.DateEvent
import de.gematik.ti.erp.app.medicationplan.model.MedicationDosage
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotification
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.getCalculatedEndDate
import de.gematik.ti.erp.app.medicationplan.model.pieceableForm
import de.gematik.ti.erp.app.medicationplan.model.toMedicationSchedule
import de.gematik.ti.erp.app.medicationplan.usecase.GetDosageInstructionByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.LoadMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.PlanMedicationScheduleUseCase
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.utils.atCurrentTime
import de.gematik.ti.erp.app.utils.isMaxDate
import de.gematik.ti.erp.app.utils.maxLocalDate
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.compose.rememberInstance
import java.util.UUID

@Immutable
data class PrescriptionSchedule(
    val prescription: PrescriptionData.Prescription,
    val medicationSchedule: MedicationSchedule,
    val dosageInstruction: MedicationPlanDosageInstruction
) {
    fun isScheduledEndless() = medicationSchedule.end.isMaxDate()
    fun isPieceableAndStructured() =
        dosageInstruction is MedicationPlanDosageInstruction.Structured &&
            pieceableForm.contains((prescription as PrescriptionData.Synced).medicationRequest.medication?.form)
}

@Stable
open class MedicationPlanScheduleScreenController(
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase,
    private val loadMedicationScheduleByTaskIdUseCase: LoadMedicationScheduleByTaskIdUseCase,
    private val getDosageInstructionByTaskIdUseCase: GetDosageInstructionByTaskIdUseCase,
    private val planMedicationScheduleUseCase: PlanMedicationScheduleUseCase,
    private val taskId: String,
    private val now: Instant = Clock.System.now()
) : Controller() {
    private val _prescriptionSchedule:
        MutableStateFlow<UiState<PrescriptionSchedule>> =
            MutableStateFlow(UiState.Loading())
    val prescriptionSchedule: StateFlow<UiState<PrescriptionSchedule>> =
        _prescriptionSchedule

    init {
        controllerScope.launch {
            runCatching {
                getPrescriptionByTaskIdUseCase(taskId).first()
            }.fold(
                onSuccess = { prescription ->
                    loadMedicationScheduleByTaskIdUseCase(taskId).collect { medicationSchedule ->
                        _prescriptionSchedule.value = UiState.Data(
                            PrescriptionSchedule(
                                prescription = prescription,
                                medicationSchedule = medicationSchedule ?: prescription.toMedicationSchedule(now),
                                dosageInstruction = getDosageInstructionByTaskIdUseCase(prescription.taskId).first()
                            )
                        )
                    }
                },
                onFailure = {
                    _prescriptionSchedule.value = UiState.Error(it)
                }
            )
        }
    }

    internal fun addNewTimeSlot(
        uuid: String = UUID.randomUUID().toString(),
        dosage: MedicationDosage,
        time: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    ) {
        withMedicationSchedule { schedule ->
            val formattedTime = LocalTime(time.hour, time.minute)
            planMedicationScheduleUseCase(
                schedule.copy(
                    notifications = schedule.notifications + MedicationNotification(
                        dosage = dosage,
                        time = formattedTime,
                        id = uuid
                    )
                )
            )
        }
    }

    internal fun removeNotification(notification: MedicationNotification) {
        withMedicationSchedule { schedule ->
            planMedicationScheduleUseCase(
                schedule.copy(
                    notifications = schedule.notifications.filter {
                        it.id != notification.id
                    }
                )
            )
        }
    }

    internal fun modifyNotificationTime(notification: MedicationNotification, time: LocalTime) {
        withMedicationSchedule { schedule ->
            planMedicationScheduleUseCase(
                schedule.copy(
                    notifications = schedule.notifications.map {
                        if (it.id == notification.id) {
                            it.copy(time = time)
                        } else {
                            it
                        }
                    }
                )
            )
        }
    }

    internal fun modifyDosage(notification: MedicationNotification, dosage: MedicationDosage) {
        withMedicationSchedule { schedule ->
            planMedicationScheduleUseCase(
                schedule.copy(
                    notifications = schedule.notifications.map {
                        if (it.id == notification.id) {
                            it.copy(dosage = dosage)
                        } else {
                            it
                        }
                    }
                )
            )
        }
    }

    internal fun activateSchedule() {
        withMedicationSchedule { schedule ->
            planMedicationScheduleUseCase(schedule.copy(isActive = true))
        }
    }

    internal fun deactivateSchedule() {
        withMedicationSchedule { schedule ->
            planMedicationScheduleUseCase(schedule.copy(isActive = false))
        }
    }

    internal fun changeScheduledDate(dateEvent: DateEvent) {
        withMedicationSchedule { schedule ->
            when (dateEvent) {
                is DateEvent.StartDate -> {
                    planMedicationScheduleUseCase(
                        schedule.copy(
                            start = dateEvent.date
                        )
                    )
                }

                is DateEvent.EndDate -> {
                    planMedicationScheduleUseCase(
                        schedule.copy(
                            end = dateEvent.date
                        )
                    )
                }
            }
        }
    }

    internal fun saveEndlessDateRange(
        startDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    ) {
        withMedicationSchedule { schedule ->
            planMedicationScheduleUseCase(
                schedule.copy(
                    start = startDate,
                    end = maxLocalDate()
                )
            )
        }
    }

    internal fun calculateIndividualDateRange(now: Instant = Clock.System.now()) {
        withPrescriptionSchedule { prescriptionSchedule ->
            planMedicationScheduleUseCase(
                prescriptionSchedule.medicationSchedule.copy(
                    start = prescriptionSchedule.medicationSchedule.start,
                    end = getCalculatedEndDate(
                        start = prescriptionSchedule.medicationSchedule.start.atCurrentTime(now),
                        amount = prescriptionSchedule.medicationSchedule.amount ?: Ratio(
                            numerator = Quantity(
                                value = "1",
                                unit = ""
                            ),
                            denominator = Quantity(
                                value = "1",
                                unit = ""
                            )
                        ),
                        dosageInstruction = prescriptionSchedule.dosageInstruction,
                        form = when (prescriptionSchedule.prescription) {
                            is PrescriptionData.Scanned -> ""
                            is PrescriptionData.Synced ->
                                prescriptionSchedule.prescription.medicationRequest.medication?.form ?: ""
                        }
                    )
                )
            )
        }
    }

    private fun withMedicationSchedule(block: suspend (MedicationSchedule) -> Unit) {
        controllerScope.launch {
            prescriptionSchedule.value.data?.medicationSchedule?.let { schedule ->
                block(schedule)
            }
        }
    }

    private fun withPrescriptionSchedule(block: suspend (PrescriptionSchedule) -> Unit) {
        controllerScope.launch {
            prescriptionSchedule.value.data?.let { prescriptionSchedule ->
                block(prescriptionSchedule)
            }
        }
    }
}

@Composable
fun rememberMedicationPlanScheduleScreenController(
    taskId: String
): MedicationPlanScheduleScreenController {
    val getPrescriptionByTaskIdUseCase by rememberInstance<GetPrescriptionByTaskIdUseCase>()
    val loadMedicationScheduleByTaskIdUseCase by rememberInstance<LoadMedicationScheduleByTaskIdUseCase>()
    val getDosageInstructionByTaskIdUseCase by rememberInstance<GetDosageInstructionByTaskIdUseCase>()
    val planMedicationScheduleUseCase by rememberInstance<PlanMedicationScheduleUseCase>()
    return remember {
        MedicationPlanScheduleScreenController(
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            getDosageInstructionByTaskIdUseCase = getDosageInstructionByTaskIdUseCase,
            loadMedicationScheduleByTaskIdUseCase = loadMedicationScheduleByTaskIdUseCase,
            planMedicationScheduleUseCase = planMedicationScheduleUseCase,
            taskId = taskId
        )
    }
}

fun Context.checkNotificationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
    ) {
        onDenied()
    } else {
        onGranted()
    }
}

@Composable
fun isIgnoringBatteryOptimizations(): State<Boolean> {
    val currentState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val context = LocalContext.current
    return remember(currentState) { derivedStateOf { context.isIgnoringBatteryOptimizations() } }
}

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(packageName)
}

@SuppressLint("BatteryLife")
fun Context.requestIgnoreBatteryOptimizations() {
    val intent =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    startActivity(intent)
}
