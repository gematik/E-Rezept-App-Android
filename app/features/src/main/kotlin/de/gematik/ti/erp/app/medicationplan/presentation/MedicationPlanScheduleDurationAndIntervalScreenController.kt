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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval
import de.gematik.ti.erp.app.medicationplan.model.toMedicationSchedule
import de.gematik.ti.erp.app.medicationplan.usecase.GetMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.ScheduleMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleDurationUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleIntervalUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.kodein.di.compose.rememberInstance

class MedicationPlanScheduleDurationAndIntervalScreenController(
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase,
    private val getMedicationScheduleByTaskIdUseCase: GetMedicationScheduleByTaskIdUseCase,
    private val setMedicationScheduleDurationUseCase: SetMedicationScheduleDurationUseCase,
    private val setMedicationScheduleIntervalUseCase: SetMedicationScheduleIntervalUseCase,
    private val scheduleMedicationScheduleUseCase: ScheduleMedicationScheduleUseCase,
    private val taskId: String,
    private val now: Instant = Clock.System.now()
) : Controller() {
    val pickPersonalizedDurationDateRangeEvent = ComposableEvent<MedicationScheduleDuration>()
    val pickDurationStartDateEvent = ComposableEvent<LocalDate>()
    val pickDurationEndDateEvent = ComposableEvent<LocalDate>()

    private val _medicationSchedule: MutableStateFlow<UiState<MedicationSchedule>> = MutableStateFlow(UiState.Loading())
    val medicationSchedule: StateFlow<UiState<MedicationSchedule>> = _medicationSchedule

    init {
        controllerScope.launch {
            _medicationSchedule.update { UiState.Loading() }
            runCatching {
                getPrescriptionByTaskIdUseCase(taskId).first()
            }.fold(
                onSuccess = { prescription ->
                    getMedicationScheduleByTaskIdUseCase(taskId).collect { medicationSchedule ->
                        _medicationSchedule.update {
                            UiState.Data(medicationSchedule ?: prescription.toMedicationSchedule(now))
                        }
                        medicationSchedule?.let { scheduleMedicationScheduleUseCase.invoke(it) }
                    }
                },
                onFailure = { error ->
                    _medicationSchedule.update { UiState.Error(error) }
                }
            )
        }
    }

    internal fun setMedicationScheduleDurationToEndless() {
        controllerScope.launch {
            _medicationSchedule.value.data?.let { schedule ->
                setMedicationScheduleDurationUseCase.invoke(
                    taskId = schedule.taskId,
                    medicationScheduleDuration = MedicationScheduleDuration.Endless()
                )
            }
        }
    }

    internal fun setMedicationScheduleDurationToEndOfPack() {
        controllerScope.launch {
            _medicationSchedule.value.data?.let { schedule ->
                val startDate = schedule.duration.startDate
                val endDate = schedule.calculateEndOfPack()
                setMedicationScheduleDurationUseCase.invoke(
                    taskId = schedule.taskId,
                    medicationScheduleDuration = MedicationScheduleDuration.EndOfPack(
                        startDate = startDate,
                        endDate = endDate
                    )
                )
            }
        }
    }

    internal fun setMedicationScheduleDurationToPersonalized(
        startDate: LocalDate?,
        endDate: LocalDate?
    ) {
        if (startDate != null && endDate != null) {
            controllerScope.launch {
                _medicationSchedule.value.data?.let { schedule ->
                    setMedicationScheduleDurationUseCase.invoke(
                        taskId = schedule.taskId,
                        medicationScheduleDuration = MedicationScheduleDuration.Personalized(
                            startDate = startDate,
                            endDate = endDate
                        )
                    )
                }
            }
        }
    }

    internal fun changeMedicationScheduleDurationStartDate(
        startDate: LocalDate?
    ) {
        if (startDate != null) {
            controllerScope.launch {
                _medicationSchedule.value.data?.let { schedule ->
                    val updatedDuration = when (val currentDuration = schedule.duration) {
                        is MedicationScheduleDuration.Endless -> currentDuration.copy(startDate = startDate)
                        is MedicationScheduleDuration.EndOfPack -> currentDuration.copy(
                            startDate = startDate,
                            endDate = schedule.calculateEndOfPack(startDate = startDate)
                        )
                        is MedicationScheduleDuration.Personalized -> currentDuration.copy(startDate = startDate)
                    }
                    setMedicationScheduleDurationUseCase.invoke(
                        taskId = schedule.taskId,
                        medicationScheduleDuration = updatedDuration
                    )
                }
            }
        }
    }

    internal fun changeMedicationScheduleDurationEndDate(
        endDate: LocalDate?
    ) {
        if (endDate != null) {
            controllerScope.launch {
                _medicationSchedule.value.data?.let { schedule ->
                    val updatedDuration = when (val currentDuration = schedule.duration) {
                        is MedicationScheduleDuration.Endless -> currentDuration.copy(endDate = endDate)
                        is MedicationScheduleDuration.EndOfPack -> currentDuration.copy(endDate = endDate)
                        is MedicationScheduleDuration.Personalized -> currentDuration.copy(endDate = endDate)
                    }
                    setMedicationScheduleDurationUseCase.invoke(
                        taskId = schedule.taskId,
                        medicationScheduleDuration = updatedDuration
                    )
                }
            }
        }
    }

    internal fun setMedicationScheduleIntervalToDaily() {
        controllerScope.launch {
            _medicationSchedule.value.data?.let { schedule ->
                setMedicationScheduleIntervalUseCase.invoke(
                    taskId = schedule.taskId,
                    medicationScheduleInterval = MedicationScheduleInterval.Daily
                )
            }
        }
    }

    internal fun setMedicationScheduleIntervalToEveryToDays() {
        controllerScope.launch {
            _medicationSchedule.value.data?.let { schedule ->
                setMedicationScheduleIntervalUseCase.invoke(
                    taskId = schedule.taskId,
                    medicationScheduleInterval = MedicationScheduleInterval.EveryTwoDays
                )
            }
        }
    }

    internal fun selectDayOfWeekAndSetMedicationScheduleIntervalToPersonalized(
        dayOfWeek: DayOfWeek
    ) {
        controllerScope.launch {
            _medicationSchedule.value.data?.let { schedule ->
                val interval = schedule.interval
                val updatedInterval = if (interval is MedicationScheduleInterval.Personalized) {
                    if (interval.selectedDays.contains(dayOfWeek)) {
                        val updatedDays = interval.selectedDays.minus(dayOfWeek)
                        if (updatedDays.isEmpty()) {
                            MedicationScheduleInterval.Daily
                        } else {
                            MedicationScheduleInterval.Personalized(selectedDays = updatedDays)
                        }
                    } else {
                        val updatedDays = interval.selectedDays.plus(dayOfWeek)
                        if (updatedDays.containsAll(DayOfWeek.entries)) {
                            MedicationScheduleInterval.Daily
                        } else {
                            MedicationScheduleInterval.Personalized(selectedDays = updatedDays)
                        }
                    }
                } else {
                    MedicationScheduleInterval.Personalized(
                        selectedDays = setOf(dayOfWeek)
                    )
                }
                setMedicationScheduleIntervalUseCase.invoke(
                    taskId = schedule.taskId,
                    medicationScheduleInterval = updatedInterval
                )
            }
        }
    }

    internal fun onTriggerPersonalizedDurationDateRangeEvent(duration: MedicationScheduleDuration) {
        pickPersonalizedDurationDateRangeEvent.trigger(duration)
    }

    internal fun onTriggerPickDurationStartDateEvent(startDate: LocalDate) {
        pickDurationStartDateEvent.trigger(startDate)
    }

    internal fun onTriggerPickDurationEndDateEvent(endDate: LocalDate) {
        pickDurationEndDateEvent.trigger(endDate)
    }
}

@Composable
fun rememberMedicationPlanScheduleDurationAndIntervalScreenController(
    taskId: String
): MedicationPlanScheduleDurationAndIntervalScreenController {
    val getPrescriptionByTaskIdUseCase by rememberInstance<GetPrescriptionByTaskIdUseCase>()
    val getMedicationScheduleByTaskIdUseCase by rememberInstance<GetMedicationScheduleByTaskIdUseCase>()
    val setMedicationScheduleDurationUseCase by rememberInstance<SetMedicationScheduleDurationUseCase>()
    val setMedicationScheduleIntervalUseCase by rememberInstance<SetMedicationScheduleIntervalUseCase>()
    val scheduleMedicationScheduleUseCase by rememberInstance<ScheduleMedicationScheduleUseCase>()
    return remember {
        MedicationPlanScheduleDurationAndIntervalScreenController(
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            getMedicationScheduleByTaskIdUseCase = getMedicationScheduleByTaskIdUseCase,
            setMedicationScheduleDurationUseCase = setMedicationScheduleDurationUseCase,
            setMedicationScheduleIntervalUseCase = setMedicationScheduleIntervalUseCase,
            scheduleMedicationScheduleUseCase = scheduleMedicationScheduleUseCase,
            taskId = taskId
        )
    }
}
