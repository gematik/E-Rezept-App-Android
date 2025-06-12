/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.medicationplan.ui
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.Checkbox
import androidx.compose.material.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.medicationplan.model.DateEvent
import de.gematik.ti.erp.app.medicationplan.presentation.PrescriptionSchedule
import de.gematik.ti.erp.app.medicationplan.presentation.rememberMedicationPlanScheduleScreenController
import de.gematik.ti.erp.app.medicationplan.ui.preview.ScheduleDateRangeScreenPreview
import de.gematik.ti.erp.app.medicationplan.ui.preview.ScheduleDateRangeScreenPreviewParameter
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintCloseButton
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.formattedString
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

const val EpochMillisPerDay = 86400000L
class ScheduleDateRangeScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val taskId =
            navBackStackEntry.arguments?.getString(
                PrescriptionDetailRoutes.PRESCRIPTION_DETAIL_NAV_TASK_ID
            ) ?: return

        val controller = rememberMedicationPlanScheduleScreenController(taskId)

        val prescriptionScheduleData by controller.prescriptionSchedule.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()

        val changeDateEvent = ComposableEvent<DateEvent>()
        val dialog = LocalDialog.current
        BackHandler {
            navController.popBackStack()
        }

        ChangeDateDialog(
            event = changeDateEvent,
            isPieceableAndStructured = prescriptionScheduleData.data?.isPieceableAndStructured() ?: false,
            dialog = dialog,
            onDateChanged = { dateEvent ->
                controller.changeScheduledDate(dateEvent)
            },
            onSelectIndividualDateRange = { controller.calculateIndividualDateRange() }
        )

        ScheduleDateRangeScreenScaffold(
            prescriptionScheduleState = prescriptionScheduleData,
            listState = listState,
            onClickEndlessDateRange = {
                controller.saveEndlessDateRange()
            },
            onSelectIndividualDateRange = {
                controller.calculateIndividualDateRange()
            },
            onChangeScheduledDate = { dateEvent ->
                changeDateEvent.trigger(dateEvent)
            },
            onBack = {
                navController.navigateUp()
            }
        )
    }
}

@Composable
fun ScheduleDateRangeScreenScaffold(
    prescriptionScheduleState: UiState<PrescriptionSchedule>,
    listState: LazyListState,
    onClickEndlessDateRange: () -> Unit,
    onSelectIndividualDateRange: () -> Unit,
    onChangeScheduledDate: (DateEvent) -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        listState = listState,
        topBarTitle = stringResource(R.string.medication_plan_title),
        navigationMode = NavigationBarMode.Back,
        onBack = onBack
    ) { contentPadding ->

        UiStateMachine(
            state = prescriptionScheduleState,
            onLoading = {
                Center {
                    CircularProgressIndicator()
                }
            },
            onEmpty = {
                ErrorScreenComponent()
            },
            onError = {
                ErrorScreenComponent()
            },
            onContent = { prescriptionSchedule ->
                ScheduleDateRangeScreenContent(
                    prescriptionSchedule = prescriptionSchedule,
                    contentPadding = contentPadding,
                    listState = listState,
                    onClickEndlessDateRange = onClickEndlessDateRange,
                    onSelectIndividualDateRange = onSelectIndividualDateRange,
                    onChangeScheduledDate = onChangeScheduledDate
                )
            }
        )
    }
}

@Composable
private fun ScheduleDateRangeScreenContent(
    prescriptionSchedule: PrescriptionSchedule,
    contentPadding: PaddingValues,
    listState: LazyListState,
    onClickEndlessDateRange: () -> Unit,
    onSelectIndividualDateRange: () -> Unit,
    onChangeScheduledDate: (DateEvent) -> Unit

) {
    LazyColumn(
        modifier = Modifier.padding(vertical = PaddingDefaults.Medium),
        contentPadding = contentPadding,
        state = listState
    ) {
        item {
            DateRangeSection(
                text = stringResource(R.string.schedule_date_range_unlimited),
                selected = prescriptionSchedule.isScheduledEndless(),
                onSelect = onClickEndlessDateRange
            )
        }
        item {
            DateRangeSection(
                text = stringResource(R.string.schedule_date_range_individually),
                selected = !prescriptionSchedule.isScheduledEndless(),
                onSelect = onSelectIndividualDateRange
            )
        }

        if (!prescriptionSchedule.isScheduledEndless()) {
            item {
                ScheduleDateSection(
                    text = stringResource(R.string.schedule_date_range_start),
                    date = prescriptionSchedule.medicationSchedule.start,
                    onclickDate = {
                        onChangeScheduledDate(
                            DateEvent.StartDate(
                                date = prescriptionSchedule.medicationSchedule.start
                            )
                        )
                    }
                )
            }
            item {
                ScheduleDateSection(
                    text = stringResource(R.string.schedule_date_range_end),
                    date = prescriptionSchedule.medicationSchedule.end,
                    onclickDate = {
                        onChangeScheduledDate(
                            DateEvent.EndDate(
                                date = prescriptionSchedule.medicationSchedule.end
                            )
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeDateDialog(
    event: ComposableEvent<DateEvent>,
    isPieceableAndStructured: Boolean,
    onDateChanged: (DateEvent) -> Unit,
    onSelectIndividualDateRange: () -> Unit,
    dialog: DialogScaffold
) {
    event.listen {
        dialog.show { dialog ->
            val initialDateMillis = when (event.payload) {
                is DateEvent.StartDate -> (
                    event.payload as DateEvent.StartDate
                    ).date.toEpochDays() * EpochMillisPerDay
                is DateEvent.EndDate -> (
                    event.payload as DateEvent.EndDate
                    )
                    .date.toEpochDays() * EpochMillisPerDay
                null -> Clock.System.now().toEpochMilliseconds()
            }
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialDateMillis
            )

            var showHint by remember { mutableStateOf(true) }
            LazyColumn {
                item {
                    Column {
                        SpacerSmall()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = PaddingDefaults.Small, end = PaddingDefaults.Medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    dialog.dismiss()
                                }
                            ) {
                                Text(text = stringResource(R.string.cancel))
                            }
                            Spacer(modifier = Modifier.weight(1.0f))
                            PrimaryButton(onClick = {
                                when (event.payload) {
                                    is DateEvent.StartDate -> {
                                        onDateChanged(
                                            DateEvent.StartDate(
                                                date = LocalDate.fromEpochDays(
                                                    datePickerState.selectedDateMillis?.div(
                                                        EpochMillisPerDay
                                                    )?.toInt() ?: 0
                                                )
                                            )
                                        )
                                    }

                                    else -> {
                                        onDateChanged(
                                            DateEvent.EndDate(
                                                date = LocalDate.fromEpochDays(
                                                    datePickerState.selectedDateMillis?.div(
                                                        EpochMillisPerDay
                                                    )?.toInt() ?: 0
                                                )
                                            )
                                        )
                                    }
                                }
                                dialog.dismiss()
                            }) {
                                Text(
                                    text = stringResource(R.string.date_picker_save_date),
                                    color = AppTheme.colors.neutral000
                                )
                            }
                        }
                    }
                }
                item {
                    if (isPieceableAndStructured && event.payload is DateEvent.EndDate) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                modifier = Modifier.padding(start = PaddingDefaults.Tiny),
                                checked = initialDateMillis == datePickerState.selectedDateMillis,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        onSelectIndividualDateRange()
                                        dialog.dismiss()
                                    }
                                }
                            )
                            SpacerMedium()
                            Text(stringResource(R.string.date_picker_to_calculated_date))
                        }
                        if (showHint) {
                            HintCard(
                                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                                properties = HintCardDefaults.properties(
                                    backgroundColor = AppTheme.colors.primary100,
                                    contentColor = AppTheme.colors.primary900,
                                    border = BorderStroke(1.dp, AppTheme.colors.primary700)
                                ),
                                image = {
                                    Icon(
                                        Icons.Outlined.Info,
                                        null,
                                        modifier = Modifier
                                            .padding(it)
                                            .requiredSize(24.dp),
                                        tint = AppTheme.colors.primary700
                                    )
                                },
                                body = {
                                    Text(text = stringResource(R.string.date_picker_calculateed_date_info))
                                },
                                title = null,
                                close = {
                                    HintCloseButton(tint = AppTheme.colors.primary700, innerPadding = it) {
                                        showHint = false
                                    }
                                }
                            )
                        }
                    }
                }
                item {
                    val text = when (event.payload) {
                        is DateEvent.StartDate -> stringResource(R.string.schedule_date_range_save_start)
                        else -> stringResource(R.string.schedule_date_range_save_end)
                    }
                    Box(modifier = Modifier.padding(PaddingDefaults.Medium), contentAlignment = Alignment.Center) {
                        DatePicker(
                            state = datePickerState,
                            colors = DatePickerDefaults.colors(
                                selectedDayContainerColor = AppTheme.colors.primary700,
                                selectedDayContentColor = AppTheme.colors.neutral000,
                                selectedYearContainerColor = AppTheme.colors.primary700,
                                selectedYearContentColor = AppTheme.colors.neutral000,
                                todayContentColor = AppTheme.colors.neutral600,
                                todayDateBorderColor = AppTheme.colors.primary700
                            ),
                            title = { Text(text = text) },
                            showModeToggle = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateRangeSection(selected: Boolean, text: String, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect()
            }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PaddingDefaults.XLarge,
                vertical = PaddingDefaults.Medium
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = AppTheme.typography.body1
            )
            Spacer(modifier = Modifier.weight(1f))
            if (selected) {
                Icon(Icons.Sharp.Check, null, tint = AppTheme.colors.primary700)
            }
        }
    }
}

@Composable
private fun ScheduleDateSection(text: String, date: LocalDate, onclickDate: () -> Unit) {
    Row(
        modifier = Modifier.padding(
            horizontal = PaddingDefaults.XLarge,
            vertical = PaddingDefaults.Medium
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = AppTheme.typography.body1
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = date.formattedString(),
            modifier = Modifier
                .background(
                    shape = RoundedCornerShape(SizeDefaults.one),
                    color = AppTheme.colors.primary200
                )
                .clip(RoundedCornerShape(SizeDefaults.one))
                .clickable {
                    onclickDate()
                }
                .padding(
                    horizontal = PaddingDefaults.Tiny,
                    vertical = SizeDefaults.threeQuarter
                ),
            style = AppTheme.typography.body1
        )
    }
}

@LightDarkPreview
@Composable
fun ScheduleDateRangeScreenPreview(
    @PreviewParameter(ScheduleDateRangeScreenPreviewParameter::class) previewData: ScheduleDateRangeScreenPreview
) {
    PreviewAppTheme {
        val listState = rememberLazyListState()

        ScheduleDateRangeScreenScaffold(
            listState = listState,
            prescriptionScheduleState = previewData.state,
            onChangeScheduledDate = {},
            onClickEndlessDateRange = {},
            onSelectIndividualDateRange = {},
            onBack = {}
        )
    }
}
