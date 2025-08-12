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

package de.gematik.ti.erp.app.medicationplan.ui
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.fhir.temporal.formattedString
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval
import de.gematik.ti.erp.app.medicationplan.presentation.rememberMedicationPlanScheduleDurationAndIntervalScreenController
import de.gematik.ti.erp.app.medicationplan.ui.components.PickPersonalizedDurationDateDialog
import de.gematik.ti.erp.app.medicationplan.ui.components.PickPersonalizedDurationDateRangeDialog
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationPlanScheduleDurationAndIntervalScreenPreview
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationPlanScheduleDurationAndIntervalScreenPreviewParameter
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class MedicationPlanScheduleDurationAndIntervalScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val taskId =
            navBackStackEntry.arguments?.getString(
                PrescriptionDetailRoutes.PRESCRIPTION_DETAIL_NAV_TASK_ID
            ) ?: return

        val controller = rememberMedicationPlanScheduleDurationAndIntervalScreenController(taskId)

        val medicationScheduleUiState by controller.medicationSchedule.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()

        val dialog = LocalDialog.current

        val onBack by rememberUpdatedState {
            navController.popBackStack()
        }

        with(controller) {
            PickPersonalizedDurationDateRangeDialog(
                event = pickPersonalizedDurationDateRangeEvent,
                dialogScaffold = dialog,
                onPickDateRange = { startDate, endDate ->
                    controller.setMedicationScheduleDurationToPersonalized(startDate = startDate, endDate = endDate)
                }
            )
            PickPersonalizedDurationDateDialog(
                event = pickDurationStartDateEvent,
                dialogScaffold = dialog,
                onPickDate = { startDate ->
                    controller.changeMedicationScheduleDurationStartDate(startDate = startDate)
                }
            )
            PickPersonalizedDurationDateDialog(
                event = pickDurationEndDateEvent,
                dialogScaffold = dialog,
                onPickDate = { endDate ->
                    controller.changeMedicationScheduleDurationEndDate(endDate = endDate)
                }
            )
        }

        BackHandler {
            onBack()
        }
        MedicationPlanScheduleDurationAndIntervalScreenScaffold(
            medicationScheduleUiState = medicationScheduleUiState,
            listState = listState,
            onClickDurationOptionEndless = {
                controller.setMedicationScheduleDurationToEndless()
            },
            onClickDurationOptionEndOfPack = {
                controller.setMedicationScheduleDurationToEndOfPack()
            },
            onClickDurationOptionPersonalized = {
                medicationScheduleUiState.data?.duration?.let { controller.onTriggerPersonalizedDurationDateRangeEvent(it) }
            },
            onClickChangeStartDate = {
                medicationScheduleUiState.data?.duration?.let { controller.onTriggerPickDurationStartDateEvent(it.startDate) }
            },
            onClickChangeEndDate = {
                medicationScheduleUiState.data?.duration?.let { controller.onTriggerPickDurationEndDateEvent(it.endDate) }
            },
            onClickIntervalOptionDaily = {
                controller.setMedicationScheduleIntervalToDaily()
            },
            onClickIntervalOptionEveryTwoDays = {
                controller.setMedicationScheduleIntervalToEveryToDays()
            },
            onClickIntervalOptionPersonalized = { day ->
                controller.selectDayOfWeekAndSetMedicationScheduleIntervalToPersonalized(dayOfWeek = day)
            },
            onBack = {
                onBack()
            }
        )
    }
}

@Composable
fun MedicationPlanScheduleDurationAndIntervalScreenScaffold(
    medicationScheduleUiState: UiState<MedicationSchedule>,
    listState: LazyListState,
    onClickDurationOptionEndless: () -> Unit,
    onClickDurationOptionEndOfPack: () -> Unit,
    onClickDurationOptionPersonalized: () -> Unit,
    onClickChangeStartDate: () -> Unit,
    onClickChangeEndDate: () -> Unit,
    onClickIntervalOptionDaily: () -> Unit,
    onClickIntervalOptionEveryTwoDays: () -> Unit,
    onClickIntervalOptionPersonalized: (day: DayOfWeek) -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        listState = listState,
        topBarTitle = stringResource(R.string.medication_plan_title),
        navigationMode = NavigationBarMode.Back,
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        onBack = onBack
    ) { contentPadding ->
        UiStateMachine(
            state = medicationScheduleUiState,
            onLoading = {
                FullScreenLoadingIndicator()
            },
            onEmpty = {
                ErrorScreenComponent()
            },
            onError = {
                ErrorScreenComponent()
            },
            onContent = { medicationSchedule ->
                MedicationPlanScheduleDurationAndIntervalScreenContent(
                    medicationSchedule = medicationSchedule,
                    contentPadding = contentPadding,
                    listState = listState,
                    onClickDurationOptionEndless = onClickDurationOptionEndless,
                    onClickDurationOptionEndOfPack = onClickDurationOptionEndOfPack,
                    onClickDurationOptionPersonalized = onClickDurationOptionPersonalized,
                    onClickChangeStartDate = onClickChangeStartDate,
                    onClickChangeEndDate = onClickChangeEndDate,
                    onClickIntervalOptionDaily = onClickIntervalOptionDaily,
                    onClickIntervalOptionEveryTwoDays = onClickIntervalOptionEveryTwoDays,
                    onClickIntervalOptionPersonalized = onClickIntervalOptionPersonalized
                )
            }
        )
    }
}

@Composable
private fun MedicationPlanScheduleDurationAndIntervalScreenContent(
    medicationSchedule: MedicationSchedule,
    contentPadding: PaddingValues,
    listState: LazyListState,
    onClickDurationOptionEndless: () -> Unit,
    onClickDurationOptionEndOfPack: () -> Unit,
    onClickDurationOptionPersonalized: () -> Unit,
    onClickChangeStartDate: () -> Unit,
    onClickChangeEndDate: () -> Unit,
    onClickIntervalOptionDaily: () -> Unit,
    onClickIntervalOptionEveryTwoDays: () -> Unit,
    onClickIntervalOptionPersonalized: (day: DayOfWeek) -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.XXLarge),
        state = listState
    ) {
        scheduleIntervalSection(
            medicationScheduleInterval = medicationSchedule.interval,
            onClickIntervalOptionDaily = onClickIntervalOptionDaily,
            onClickIntervalOptionEveryTwoDays = onClickIntervalOptionEveryTwoDays,
            onClickIntervalOptionPersonalized = onClickIntervalOptionPersonalized
        )
        medicationScheduleDurationSection(
            medicationSchedule = medicationSchedule,
            onClickDurationOptionEndless = onClickDurationOptionEndless,
            onClickDurationOptionEndOfPack = onClickDurationOptionEndOfPack,
            onClickDurationOptionPersonalized = onClickDurationOptionPersonalized,
            onClickChangeStartDate = onClickChangeStartDate,
            onClickChangeEndDate = onClickChangeEndDate
        )
    }
}

private fun LazyListScope.scheduleIntervalSection(
    medicationScheduleInterval: MedicationScheduleInterval,
    onClickIntervalOptionDaily: () -> Unit,
    onClickIntervalOptionEveryTwoDays: () -> Unit,
    onClickIntervalOptionPersonalized: (day: DayOfWeek) -> Unit
) {
    item {
        Column(
            modifier = Modifier.padding(top = PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            Text(stringResource(R.string.medication_plan_reminder_interval_header), style = AppTheme.typography.h6, modifier = Modifier.semanticsHeading())
            Card(
                backgroundColor = AppTheme.colors.neutral000,
                border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral100),
                shape = RoundedCornerShape(SizeDefaults.doubleHalf),
                elevation = SizeDefaults.quarter
            ) {
                Column(modifier = Modifier.padding(vertical = PaddingDefaults.Small)) {
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.medication_plan_reminder_interval_daily),
                        selected = medicationScheduleInterval is MedicationScheduleInterval.Daily,
                        onClick = { onClickIntervalOptionDaily() }
                    )
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.medication_plan_reminder_interval_every_second_day),
                        selected = medicationScheduleInterval is MedicationScheduleInterval.EveryTwoDays,
                        onClick = { onClickIntervalOptionEveryTwoDays() }
                    )
                }
            }
            Card(
                backgroundColor = AppTheme.colors.neutral000,
                border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral100),
                shape = RoundedCornerShape(SizeDefaults.doubleHalf),
                elevation = SizeDefaults.quarter
            ) {
                Column(modifier = Modifier.padding(vertical = PaddingDefaults.Small)) {
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.medication_plan_reminder_interval_monday),
                        selected = (
                            medicationScheduleInterval is MedicationScheduleInterval.Personalized &&
                                medicationScheduleInterval.selectedDays.contains(DayOfWeek.MONDAY)
                            ) || medicationScheduleInterval is MedicationScheduleInterval.Daily,
                        onClick = { onClickIntervalOptionPersonalized(DayOfWeek.MONDAY) }
                    )
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.medication_plan_reminder_interval_tuesday),
                        selected = (
                            medicationScheduleInterval is MedicationScheduleInterval.Personalized &&
                                medicationScheduleInterval.selectedDays.contains(DayOfWeek.TUESDAY)
                            ) || medicationScheduleInterval is MedicationScheduleInterval.Daily,
                        onClick = { onClickIntervalOptionPersonalized(DayOfWeek.TUESDAY) }
                    )
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.medication_plan_reminder_interval_wednesday),
                        selected = (
                            medicationScheduleInterval is MedicationScheduleInterval.Personalized &&
                                medicationScheduleInterval.selectedDays.contains(DayOfWeek.WEDNESDAY)
                            ) || medicationScheduleInterval is MedicationScheduleInterval.Daily,
                        onClick = { onClickIntervalOptionPersonalized(DayOfWeek.WEDNESDAY) }
                    )
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.medication_plan_reminder_interval_thursday),
                        selected = (
                            medicationScheduleInterval is MedicationScheduleInterval.Personalized &&
                                medicationScheduleInterval.selectedDays.contains(DayOfWeek.THURSDAY)
                            ) || medicationScheduleInterval is MedicationScheduleInterval.Daily,
                        onClick = { onClickIntervalOptionPersonalized(DayOfWeek.THURSDAY) }
                    )
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.medication_plan_reminder_interval_friday),
                        selected = (
                            medicationScheduleInterval is MedicationScheduleInterval.Personalized &&
                                medicationScheduleInterval.selectedDays.contains(DayOfWeek.FRIDAY)
                            ) || medicationScheduleInterval is MedicationScheduleInterval.Daily,
                        onClick = { onClickIntervalOptionPersonalized(DayOfWeek.FRIDAY) }
                    )
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.medication_plan_reminder_interval_saturday),
                        selected = (
                            medicationScheduleInterval is MedicationScheduleInterval.Personalized &&
                                medicationScheduleInterval.selectedDays.contains(DayOfWeek.SATURDAY)
                            ) || medicationScheduleInterval is MedicationScheduleInterval.Daily,
                        onClick = { onClickIntervalOptionPersonalized(DayOfWeek.SATURDAY) }
                    )
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.medication_plan_reminder_interval_sunday),
                        selected = (
                            medicationScheduleInterval is MedicationScheduleInterval.Personalized &&
                                medicationScheduleInterval.selectedDays.contains(DayOfWeek.SUNDAY)
                            ) || medicationScheduleInterval is MedicationScheduleInterval.Daily,
                        onClick = { onClickIntervalOptionPersonalized(DayOfWeek.SUNDAY) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun LazyListScope.medicationScheduleDurationSection(
    medicationSchedule: MedicationSchedule,
    onClickDurationOptionEndless: () -> Unit,
    onClickDurationOptionEndOfPack: () -> Unit,
    onClickDurationOptionPersonalized: () -> Unit,
    onClickChangeStartDate: () -> Unit,
    onClickChangeEndDate: () -> Unit
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
            Text(stringResource(R.string.medication_plan_reminder_duration_header), style = AppTheme.typography.h6, modifier = Modifier.semanticsHeading())
            Card(
                backgroundColor = AppTheme.colors.neutral000,
                border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral100),
                shape = RoundedCornerShape(SizeDefaults.doubleHalf),
                elevation = SizeDefaults.quarter
            ) {
                Column(modifier = Modifier.padding(vertical = PaddingDefaults.Small)) {
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.schedule_date_range_unlimited),
                        selected = medicationSchedule.duration is MedicationScheduleDuration.Endless,
                        onClick = { onClickDurationOptionEndless() }
                    )
                    if (medicationSchedule.duration is MedicationScheduleDuration.Endless) {
                        Column(modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)) {
                            DateSelectionListItem(
                                text = stringResource(R.string.schedule_date_range_start),
                                date = medicationSchedule.duration.startDate,
                                onClick = {
                                    onClickChangeStartDate()
                                }
                            )
                        }
                    }
                    ListItem(
                        modifier = Modifier.toggleable(
                            value = medicationSchedule.duration is MedicationScheduleDuration.EndOfPack,
                            onValueChange = {
                                onClickDurationOptionEndOfPack()
                            },
                            role = Role.Checkbox,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current
                        ),
                        text = {
                            Text(
                                text = stringResource(R.string.medication_plan_till_end_of_pack),
                                style = AppTheme.typography.body1
                            )
                        },
                        secondaryText = {
                            Text(
                                medicationSchedule.calculateEndOfPack().formattedString(),
                                style = AppTheme.typography.body2,
                                color = AppTheme.colors.neutral600
                            )
                        },
                        trailing = {
                            if (medicationSchedule.duration is MedicationScheduleDuration.EndOfPack) {
                                Icon(Icons.Sharp.Check, null, tint = AppTheme.colors.primary700)
                            }
                        }
                    )
                    if (medicationSchedule.duration is MedicationScheduleDuration.EndOfPack) {
                        Column(modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)) {
                            DateSelectionListItem(
                                text = stringResource(R.string.schedule_date_range_start),
                                date = medicationSchedule.duration.startDate,
                                onClick = {
                                    onClickChangeStartDate()
                                }
                            )
                        }
                    }
                    ScheduleSelectionListItem(
                        text = stringResource(R.string.schedule_date_range_individually),
                        selected = medicationSchedule.duration is MedicationScheduleDuration.Personalized,
                        onClick = { onClickDurationOptionPersonalized() }
                    )
                    if (medicationSchedule.duration is MedicationScheduleDuration.Personalized) {
                        Column(
                            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                        ) {
                            DateSelectionListItem(
                                text = stringResource(R.string.schedule_date_range_start),
                                date = medicationSchedule.duration.startDate,
                                onClick = {
                                    onClickChangeStartDate()
                                }
                            )
                            DateSelectionListItem(
                                text = stringResource(R.string.schedule_date_range_end),
                                date = medicationSchedule.duration.endDate,
                                onClick = {
                                    onClickChangeEndDate()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScheduleSelectionListItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.toggleable(
            value = selected,
            onValueChange = {
                onClick()
            },
            role = Role.Checkbox,
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current
        ),
        text = {
            Text(
                text = text,
                style = AppTheme.typography.body1
            )
        },
        trailing = {
            if (selected) {
                Icon(Icons.Sharp.Check, null, tint = AppTheme.colors.primary700)
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DateSelectionListItem(
    text: String,
    date: LocalDate,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .background(color = AppTheme.colors.primary100, shape = RoundedCornerShape(SizeDefaults.one))
            .border(color = AppTheme.colors.primary500, shape = RoundedCornerShape(SizeDefaults.one), width = SizeDefaults.eighth)
            .clickable(
                role = Role.Button
            ) {
                onClick()
            },
        text = {
            Text(
                text = text,
                style = AppTheme.typography.body1,
                color = AppTheme.colors.primary900
            )
        },
        trailing = {
            Text(
                text = date.formattedString(),
                modifier = Modifier
                    .padding(
                        vertical = PaddingDefaults.Tiny
                    ),
                style = AppTheme.typography.body1,
                color = AppTheme.colors.primary900
            )
        }
    )
}

@LightDarkPreview
@Composable
fun MedicationPlanScheduleDurationAndIntervalScreenPreview(
    @PreviewParameter(MedicationPlanScheduleDurationAndIntervalScreenPreviewParameter::class)
    previewData: MedicationPlanScheduleDurationAndIntervalScreenPreview
) {
    PreviewAppTheme {
        val listState = rememberLazyListState()

        MedicationPlanScheduleDurationAndIntervalScreenScaffold(
            listState = listState,
            medicationScheduleUiState = previewData.state,
            onClickDurationOptionEndless = {},
            onClickDurationOptionEndOfPack = {},
            onClickDurationOptionPersonalized = {},
            onClickChangeStartDate = {},
            onClickChangeEndDate = {},
            onClickIntervalOptionDaily = {},
            onClickIntervalOptionEveryTwoDays = {},
            onClickIntervalOptionPersonalized = {},
            onBack = {}
        )
    }
}
