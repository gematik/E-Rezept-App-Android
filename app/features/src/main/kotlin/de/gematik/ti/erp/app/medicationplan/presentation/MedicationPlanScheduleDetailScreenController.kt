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

package de.gematik.ti.erp.app.medicationplan.presentation

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.core.LifecycleEventObserver
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstructionErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosageErpModel
import de.gematik.ti.erp.app.medicationplan.model.parseInstruction
import de.gematik.ti.erp.app.medicationplan.model.toMedicationSchedule
import de.gematik.ti.erp.app.medicationplan.usecase.DeactivateMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.DeleteMedicationScheduleNotificationUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.DeleteMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.GetMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.CheckAndScheduleMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleNotificationDosageUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleNotificationTimeUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetOrCreateActiveMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetOrCreateMedicationScheduleNotificationUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import org.kodein.di.compose.rememberInstance
import java.util.UUID
import kotlin.getValue

class MedicationPlanScheduleDetailScreenController(
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase,
    private val deleteMedicationScheduleUseCase: DeleteMedicationScheduleUseCase,
    private val getMedicationScheduleByTaskIdUseCase: GetMedicationScheduleByTaskIdUseCase,
    private val setOrCreateActiveMedicationScheduleUseCase: SetOrCreateActiveMedicationScheduleUseCase,
    private val deactivateMedicationScheduleUseCase: DeactivateMedicationScheduleUseCase,
    private val setOrCreateMedicationScheduleNotificationUseCase: SetOrCreateMedicationScheduleNotificationUseCase,
    private val deleteMedicationScheduleNotificationUseCase: DeleteMedicationScheduleNotificationUseCase,
    private val setMedicationScheduleNotificationDosageUseCase: SetMedicationScheduleNotificationDosageUseCase,
    private val setMedicationScheduleNotificationTimeUseCase: SetMedicationScheduleNotificationTimeUseCase,
    private val checkAndScheduleMedicationScheduleUseCase: CheckAndScheduleMedicationScheduleUseCase,
    private val taskId: String
) : Controller() {
    val changeMedicationScheduleNotificationErpModelTimeEvent = ComposableEvent<MedicationScheduleNotificationErpModel>()
    val addMedicationNotificationTimeEvent = ComposableEvent<Unit>()
    val changeMedicationScheduleNotificationErpModelDosageEvent = ComposableEvent<MedicationScheduleNotificationErpModel>()
    val changeAllowExactAlarmsEvent = ComposableEvent<Unit>()
    val deleteMedicationScheduleEvent = ComposableEvent<Unit>()

    private val _dosageInstruction: MutableStateFlow<MedicationPlanDosageInstructionErpModel> =
        MutableStateFlow(MedicationPlanDosageInstructionErpModel.Empty)
    private val _medicationScheduleErpModel: MutableStateFlow<UiState<MedicationScheduleErpModel>> =
        MutableStateFlow(UiState.Loading())
    val dosageInstruction: StateFlow<MedicationPlanDosageInstructionErpModel> =
        _dosageInstruction
    val medicationScheduleErpModel: StateFlow<UiState<MedicationScheduleErpModel>> =
        _medicationScheduleErpModel

    init {
        controllerScope.launch {
            _medicationScheduleErpModel.update { UiState.Loading() }
            runCatching {
                getPrescriptionByTaskIdUseCase(taskId).first()
            }.fold(
                onSuccess = { prescription ->
                    _dosageInstruction.update {
                        when (prescription) {
                            is TaskErpModel.Synced.Prescription -> parseInstruction(prescription.medicationRequest?.dosageInstruction)
                            else -> MedicationPlanDosageInstructionErpModel.Empty
                        }
                    }
                    getMedicationScheduleByTaskIdUseCase(taskId).collect { medicationSchedule ->
                        _medicationScheduleErpModel.update {
                            UiState.Data(
                                medicationSchedule ?: prescription.toMedicationSchedule()
                            )
                        }
                        medicationSchedule?.let {
                            checkAndScheduleMedicationScheduleUseCase.invoke(it)
                        }
                    }
                },
                onFailure = { error ->
                    _medicationScheduleErpModel.update { UiState.Error(error) }
                }
            )
        }
    }

    internal fun activateSchedule() {
        controllerScope.launch {
            _medicationScheduleErpModel.value.data?.let { schedule ->
                setOrCreateActiveMedicationScheduleUseCase.invoke(medicationScheduleErpModel = schedule)
            }
        }
    }

    internal fun deactivateSchedule() {
        controllerScope.launch {
            _medicationScheduleErpModel.value.data?.let { schedule ->
                deactivateMedicationScheduleUseCase.invoke(taskId = schedule.taskId)
            }
        }
    }

    internal fun addNewMedicationNotification(
        uuid: String = UUID.randomUUID().toString(),
        dosage: MedicationScheduleNotificationDosageErpModel,
        time: LocalTime
    ) {
        controllerScope.launch {
            _medicationScheduleErpModel.value.data?.let { schedule ->
                val newMedicationScheduleNotificationErpModel = MedicationScheduleNotificationErpModel(
                    dosage = dosage,
                    time = LocalTime(time.hour, time.minute),
                    id = uuid
                )
                setOrCreateMedicationScheduleNotificationUseCase.invoke(
                    taskId = schedule.taskId,
                    medicationScheduleNotificationErpModel = newMedicationScheduleNotificationErpModel
                )
            }
        }
    }

    internal fun removeMedicationNotification(notification: MedicationScheduleNotificationErpModel) {
        controllerScope.launch {
            deleteMedicationScheduleNotificationUseCase(medicationScheduleNotificationId = notification.id)
        }
    }

    internal fun changeMedicationNotificationTime(notification: MedicationScheduleNotificationErpModel, time: LocalTime) {
        controllerScope.launch {
            setMedicationScheduleNotificationTimeUseCase(
                medicationScheduleNotificationId = notification.id,
                time = time
            )
        }
    }

    internal fun changeMedicationNotificationDosage(
        notification: MedicationScheduleNotificationErpModel,
        dosage: MedicationScheduleNotificationDosageErpModel
    ) {
        controllerScope.launch {
            setMedicationScheduleNotificationDosageUseCase(
                medicationScheduleNotificationId = notification.id,
                dosage = dosage
            )
        }
    }

    internal fun deleteMedicationSchedule(taskId: String) {
        controllerScope.launch {
            deleteMedicationScheduleUseCase(
                taskId = taskId
            )
        }
    }

    internal fun onTriggerChangeMedicationNotificationDosageEvent(notification: MedicationScheduleNotificationErpModel) {
        changeMedicationScheduleNotificationErpModelDosageEvent.trigger(notification)
    }

    internal fun onTriggerChangeMedicationNotificationTimeEvent(notification: MedicationScheduleNotificationErpModel) {
        changeMedicationScheduleNotificationErpModelTimeEvent.trigger(notification)
    }

    internal fun onTriggerAddMedicationNotificationTimeEvent() {
        addMedicationNotificationTimeEvent.trigger(Unit)
    }

    internal fun onTriggerShowIgnoreBatteryOptimizationEvent() {
        changeAllowExactAlarmsEvent.trigger(Unit)
    }

    internal fun onTriggerShowDeleteDialogEvent() {
        deleteMedicationScheduleEvent.trigger(Unit)
    }
}

@Composable
fun rememberMedicationPlanScheduleDetailScreenController(
    taskId: String
): MedicationPlanScheduleDetailScreenController {
    val getPrescriptionByTaskIdUseCase by rememberInstance<GetPrescriptionByTaskIdUseCase>()
    val deleteMedicationScheduleUseCase by rememberInstance<DeleteMedicationScheduleUseCase>()
    val getMedicationScheduleByTaskIdUseCase by rememberInstance<GetMedicationScheduleByTaskIdUseCase>()
    val setOrCreateActiveMedicationScheduleUseCase by rememberInstance<SetOrCreateActiveMedicationScheduleUseCase>()
    val deactivateMedicationScheduleUseCase by rememberInstance<DeactivateMedicationScheduleUseCase>()
    val setOrCreateMedicationScheduleNotificationUseCase by rememberInstance<SetOrCreateMedicationScheduleNotificationUseCase>()
    val deleteMedicationScheduleNotificationUseCase by rememberInstance<DeleteMedicationScheduleNotificationUseCase>()
    val setMedicationScheduleNotificationDosageUseCase by rememberInstance<SetMedicationScheduleNotificationDosageUseCase>()
    val setMedicationScheduleNotificationTimeUseCase by rememberInstance<SetMedicationScheduleNotificationTimeUseCase>()
    val checkAndScheduleMedicationScheduleUseCase by rememberInstance<CheckAndScheduleMedicationScheduleUseCase>()
    return remember {
        MedicationPlanScheduleDetailScreenController(
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            deleteMedicationScheduleUseCase = deleteMedicationScheduleUseCase,
            getMedicationScheduleByTaskIdUseCase = getMedicationScheduleByTaskIdUseCase,
            setOrCreateActiveMedicationScheduleUseCase = setOrCreateActiveMedicationScheduleUseCase,
            deactivateMedicationScheduleUseCase = deactivateMedicationScheduleUseCase,
            setOrCreateMedicationScheduleNotificationUseCase = setOrCreateMedicationScheduleNotificationUseCase,
            deleteMedicationScheduleNotificationUseCase = deleteMedicationScheduleNotificationUseCase,
            setMedicationScheduleNotificationDosageUseCase = setMedicationScheduleNotificationDosageUseCase,
            setMedicationScheduleNotificationTimeUseCase = setMedicationScheduleNotificationTimeUseCase,
            checkAndScheduleMedicationScheduleUseCase = checkAndScheduleMedicationScheduleUseCase,
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
fun canScheduleExactAlarms(): Boolean {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.canScheduleExactAlarms()
            } else {
                true
            }
        )
    }
    LifecycleEventObserver { event ->
        if (event == Lifecycle.Event.ON_RESUME && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission = context.canScheduleExactAlarms()
        }
    }

    return hasPermission
}

@RequiresApi(Build.VERSION_CODES.S)
fun Context.canScheduleExactAlarms(): Boolean {
    val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return alarmManager.canScheduleExactAlarms()
}

@RequiresApi(Build.VERSION_CODES.S)
fun Context.openExactAlarmsPermission() {
    val intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    startActivity(intent)
}
