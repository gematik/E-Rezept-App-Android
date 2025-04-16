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

import android.Manifest
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material.icons.filled.DoDisturbOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.sharp.Alarm
import androidx.compose.material.IconButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TextButton
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationPlanScheduleScreenPreviewParameter
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationScheduleScreenPreview
import de.gematik.ti.erp.app.medicationplan.model.MedicationDosage
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotification
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.medicationplan.presentation.PrescriptionSchedule
import de.gematik.ti.erp.app.medicationplan.presentation.checkNotificationPermission
import de.gematik.ti.erp.app.medicationplan.presentation.isIgnoringBatteryOptimizations
import de.gematik.ti.erp.app.medicationplan.presentation.rememberMedicationPlanScheduleScreenController
import de.gematik.ti.erp.app.medicationplan.presentation.requestIgnoreBatteryOptimizations
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.InputField
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.OutlinedElevatedCard
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.formattedStringShort
import de.gematik.ti.erp.app.utils.isBeforeCurrentDate
import de.gematik.ti.erp.app.utils.isInFuture
import de.gematik.ti.erp.app.utils.isMaxDate
import de.gematik.ti.erp.app.utils.toHourMinuteString
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MedicationPlanScheduleScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val taskId =
            navBackStackEntry.arguments?.getString(
                PrescriptionDetailRoutes.PRESCRIPTION_DETAIL_NAV_TASK_ID
            ) ?: return
        val context = LocalContext.current
        val dialogScaffold = LocalDialog.current
        val controller = rememberMedicationPlanScheduleScreenController(taskId)
        val prescriptionScheduleData by controller.prescriptionSchedule.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()
        val timePickerEvent = ComposableEvent<MedicationNotification>()
        val showNotificationPermissionDialogEvent = ComposableEvent<Unit>()
        val showChangeDosageDialogEvent = ComposableEvent<MedicationNotification>()
        val isIgnoringBatteryOptimizations by isIgnoringBatteryOptimizations()
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val defaultMedicationDosage = prescriptionScheduleData.data?.medicationSchedule?.notifications?.lastOrNull()?.dosage
            ?: MedicationDosage(
                stringResource(R.string.medication_plan_default_form),
                context.getString(
                    R.string.medication_plan_default_dosage
                )
            )
        BackHandler {
            navController.popBackStack()
        }

        ChangeMedicationDosageDialog(
            event = showChangeDosageDialogEvent,
            onDosageChanged = { notification, dosage ->
                controller.modifyDosage(notification, dosage)
            }
        )

        timePickerEvent.listen { notification ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    controller.modifyNotificationTime(
                        notification = notification,
                        time = LocalTime(hour, minute)
                    )
                },
                notification.time.hour,
                notification.time.minute,
                true
            ).show()
        }

        @Requirement(
            "O.Plat_5#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Reminder notifications are deactivated by default.",
            codeLines = 50
        )
        val notificationPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (!it) {
                    showNotificationPermissionDialogEvent.trigger(Unit)
                    controller.deactivateSchedule()
                }
            }

        val showIgnoreBatteryOptimizationDialog = ComposableEvent<Unit>()

        IgnoreBatteryOptimizationDialog(
            showDisableBatteryOptimizationDialog = showIgnoreBatteryOptimizationDialog,
            dialogScaffold = dialogScaffold
        ) {
            context.requestIgnoreBatteryOptimizations()
            controller.activateSchedule()
        }

        MedicationPlanScheduleScaffold(
            listState = listState,
            prescriptionScheduleData = prescriptionScheduleData,
            currentDate = currentDate,
            isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
            onAddNewTimeSlot = {
                controller.addNewTimeSlot(
                    dosage = defaultMedicationDosage
                )
            },
            onRemoveNotificationTime = { notification ->
                controller.removeNotification(notification)
            },
            onClickChangeDateRange = {
                navController.navigate(
                    MedicationPlanRoutes.ScheduleDateRange.path(taskId = taskId)
                )
            },
            onActivateSchedule = {
                controller.activateSchedule()
                @Requirement(
                    "O.Plat_5#2",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "Check for notification permission and open launcher if not granted.",
                    codeLines = 50
                )
                context.checkNotificationPermission(
                    onDenied = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    onGranted = {
                    }
                )
                controller.activateSchedule()
            },
            onDeactivateSchedule = {
                controller.deactivateSchedule()
            },
            onNotificationTimeClick = { notification ->
                timePickerEvent.trigger(
                    notification
                )
            },
            onClickDosageInfo = {
                taskId.let {
                    navController.navigate(
                        MedicationPlanRoutes.MedicationPlanDosageInfo.path(taskId = taskId)
                    )
                }
            },
            onDosageClicked = { notification ->
                showChangeDosageDialogEvent.trigger(notification)
            },
            onShowBatteryOptimizationDialog = { showIgnoreBatteryOptimizationDialog.trigger(Unit) },
            onBack = { navController.popBackStack() }
        )
    }
}

@Composable
fun MedicationPlanScheduleScaffold(
    listState: LazyListState,
    prescriptionScheduleData: UiState<PrescriptionSchedule>,
    currentDate: LocalDate,
    isIgnoringBatteryOptimizations: Boolean,
    onAddNewTimeSlot: () -> Unit,
    onRemoveNotificationTime: (MedicationNotification) -> Unit,
    onNotificationTimeClick: (MedicationNotification) -> Unit,
    onClickChangeDateRange: () -> Unit,
    onClickDosageInfo: () -> Unit,
    onDosageClicked: (MedicationNotification) -> Unit,
    onActivateSchedule: () -> Unit,
    onDeactivateSchedule: () -> Unit,
    onShowBatteryOptimizationDialog: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        listState = listState,
        topBarTitle = stringResource(R.string.medication_plan_title),
        navigationMode = NavigationBarMode.Back,
        onBack = onBack

    ) { contentPadding ->

        UiStateMachine(
            state = prescriptionScheduleData,
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

                MedicationScheduleScreenContent(
                    listState = listState,
                    contentPadding = contentPadding,
                    currentDate = currentDate,
                    prescriptionSchedule = prescriptionSchedule,
                    isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
                    onAddNewItem = onAddNewTimeSlot,
                    onRemoveNotificationTime = onRemoveNotificationTime,
                    onClickChangeDateRange = onClickChangeDateRange,
                    onActivateSchedule = onActivateSchedule,
                    onDeactivateSchedule = onDeactivateSchedule,
                    onNotificationTimeClick = onNotificationTimeClick,
                    onClickDosageInfo = onClickDosageInfo,
                    onDosageClicked = onDosageClicked,
                    onShowBatteryOptimizationDialog = onShowBatteryOptimizationDialog
                )
            }
        )
    }
}

@Composable
fun ChangeMedicationDosageDialog(
    event: ComposableEvent<MedicationNotification>,
    onDosageChanged: (MedicationNotification, MedicationDosage) -> Unit
) {
    val dialog = LocalDialog.current
    var dosage by remember {
        mutableStateOf(event.payload?.dosage ?: MedicationDosage("", ""))
    }
    event.listen {
        dosage = it.dosage
        dialog.show { d ->
            ErezeptAlertDialog(
                title = stringResource(R.string.adjust_dosage_dialog_title),
                body = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingDefaults.Medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InputField(
                            modifier = Modifier,
                            value = dosage.ratio,
                            onValueChange = { value ->
                                dosage = dosage.copy(ratio = value)
                            },
                            label = {
                                Text(stringResource(R.string.adjust_dosage_amount_label))
                            },
                            keyBoardType = KeyboardType.Number,
                            onSubmit = {}
                        )
                        SpacerMedium()
                        InputField(
                            modifier = Modifier,
                            value = dosage.form,
                            onValueChange = { dosage = dosage.copy(form = it) },
                            label = { Text(stringResource(R.string.adjust_dosage_form_label)) },
                            keyBoardType = KeyboardType.Text,
                            onSubmit = {}
                        )
                    }
                },
                onConfirmRequest = {
                    onDosageChanged(it, dosage)
                    d.dismiss()
                },
                onDismissRequest = {
                    d.dismiss()
                }
            )
        }
    }
}

@Suppress("LongMethod", "MaxLineLength")
@Composable
private fun MedicationScheduleScreenContent(
    listState: LazyListState,
    contentPadding: PaddingValues,
    prescriptionSchedule: PrescriptionSchedule,
    currentDate: LocalDate,
    isIgnoringBatteryOptimizations: Boolean,
    onAddNewItem: () -> Unit,
    onRemoveNotificationTime: (MedicationNotification) -> Unit,
    onNotificationTimeClick: (MedicationNotification) -> Unit,
    onClickChangeDateRange: () -> Unit,
    onClickDosageInfo: () -> Unit,
    onDosageClicked: (MedicationNotification) -> Unit,
    onActivateSchedule: () -> Unit,
    onDeactivateSchedule: () -> Unit,
    onShowBatteryOptimizationDialog: () -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = listState,
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
    ) {
        val name = when (prescriptionSchedule.prescription) {
            is PrescriptionData.Scanned -> prescriptionSchedule.prescription.name
            is PrescriptionData.Synced -> prescriptionSchedule.prescription.name
        }

        item {
            SpacerMedium()
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Sharp.Alarm,
                    contentDescription = null,
                    tint = AppTheme.colors.primary700,
                    modifier = Modifier
                        .size(SizeDefaults.sevenfold)
                )
                SpacerMedium()
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = if (name.isNullOrBlank()) {
                        stringResource(R.string.medication_plan_missing_medication_name)
                    } else {
                        name
                    },
                    style = AppTheme.typography.h6,
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            SpacerMedium()
            ScheduleSettingsAndDosageCard(
                schedule = prescriptionSchedule,
                isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
                onIgnoreBatteryOptimizations = onShowBatteryOptimizationDialog,
                onActivateSchedule = onActivateSchedule,
                onDeactivateSchedule = onDeactivateSchedule,
                onClickDosageInfo = onClickDosageInfo
            )
        }

        if (prescriptionSchedule.medicationSchedule.isActive) {
            item {
                SpacerXXLarge()
                Text(stringResource(R.string.plan_notification_times_header), style = AppTheme.typography.h6)
            }

            item {
                val shape = RoundedCornerShape(SizeDefaults.doubleHalf)
                SpacerMedium()
                OutlinedElevatedCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .clickable {
                                onClickChangeDateRange()
                            }
                            .padding(PaddingDefaults.Medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val text = getScheduleDurationString(prescriptionSchedule, currentDate)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(end = PaddingDefaults.Small)
                        ) {
                            Text(stringResource(R.string.medication_schedule_repeat), style = AppTheme.typography.body1)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = text,
                                style = AppTheme.typography.body1l,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowRight, contentDescription = null, tint = AppTheme.colors.neutral600)
                    }
                }
            }
            item {
                SpacerMedium()
            }

            item {
                OutlinedElevatedCard {
                    Column(
                        modifier = Modifier
                            .padding(
                                vertical = PaddingDefaults.Medium
                            )
                            .padding(end = PaddingDefaults.Medium),
                        verticalArrangement = Arrangement.spacedBy(
                            SizeDefaults.one
                        )
                    ) {
                        prescriptionSchedule.medicationSchedule.notifications.forEach { notification ->

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(SizeDefaults.half),
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    modifier = Modifier.padding(horizontal = PaddingDefaults.Tiny),
                                    onClick = {
                                        onRemoveNotificationTime(notification)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DoDisturbOn,
                                        contentDescription = null,
                                        tint = AppTheme.colors.red600
                                    )
                                }
                                Text(
                                    text = notification.time.toHourMinuteString(),
                                    modifier = Modifier
                                        .background(
                                            shape = RoundedCornerShape(SizeDefaults.one),
                                            color = AppTheme.colors.neutral200
                                        )
                                        .clip(RoundedCornerShape(SizeDefaults.one))
                                        .clickable {
                                            onNotificationTimeClick(notification)
                                        }
                                        .padding(
                                            horizontal = PaddingDefaults.Tiny,
                                            vertical = SizeDefaults.threeQuarter
                                        ),
                                    style = AppTheme.typography.body1
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = {
                                        onDosageClicked(notification)
                                    }
                                ) {
                                    Text("${notification.dosage.ratio} ${notification.dosage.form}")
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddNewItem() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SpacerMedium()
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = null,
                                tint = AppTheme.colors.green600
                            )
                            SpacerMedium()
                            Text(
                                text = stringResource(R.string.medication_schedule_add_notification_time),
                                style = AppTheme.typography.body1,
                                color = AppTheme.colors.primary700
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getScheduleDurationString(
    prescriptionSchedule: PrescriptionSchedule,
    currentDate: LocalDate
): String {
    val text = when {
        prescriptionSchedule.medicationSchedule.end.isMaxDate() -> stringResource(R.string.medication_plan_endless)
        prescriptionSchedule.medicationSchedule.end.isBeforeCurrentDate(currentDate) ->
            stringResource(R.string.medication_plan_ended)
        prescriptionSchedule.medicationSchedule.start.isInFuture(currentDate) -> stringResource(
            R.string.medicationPlanDuration,
            prescriptionSchedule.medicationSchedule.start.formattedStringShort(),
            prescriptionSchedule.medicationSchedule.end.formattedStringShort()
        )
        else -> stringResource(
            R.string.medication_plan_ends,
            prescriptionSchedule.medicationSchedule.end.formattedStringShort()
        )
    }
    return text
}

@Composable
fun IgnoreBatteryOptimizationDialog(
    showDisableBatteryOptimizationDialog: ComposableEvent<Unit>,
    dialogScaffold: DialogScaffold,
    onIgnoreBatteryOptimizations: () -> Unit
) {
    showDisableBatteryOptimizationDialog.listen {
        dialogScaffold.show { dialog ->
            ErezeptAlertDialog(
                title = stringResource(R.string.ignore_battery_optimization_dialog_title),
                bodyText = stringResource(R.string.ignore_battery_optimization_dialog_info),
                dismissText = stringResource(R.string.ignore_battery_optimization_dialog_dissmiss),
                confirmText = stringResource(R.string.ignore_battery_optimization_dialog_confirm),
                onConfirmRequest = { onIgnoreBatteryOptimizations() },
                onDismissRequest = { dialog.dismiss() }
            )
        }
    }
}

@Composable
fun ScheduleSettingsAndDosageCard(
    schedule: PrescriptionSchedule,
    isIgnoringBatteryOptimizations: Boolean,
    onIgnoreBatteryOptimizations: () -> Unit,
    onActivateSchedule: () -> Unit,
    onDeactivateSchedule: () -> Unit,
    onClickDosageInfo: () -> Unit
) {
    OutlinedElevatedCard {
        ScheduleActivitySection(schedule, onActivateSchedule, onDeactivateSchedule)
        if (!isIgnoringBatteryOptimizations) {
            IgnoreBatteryOptimizationSection(onIgnoreBatteryOptimizations)
        }
        DosageInfo(schedule, onClickDosageInfo)
    }
}

@Composable
fun IgnoreBatteryOptimizationSection(onIgnoreBatteryOptimizations: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable {
                onIgnoreBatteryOptimizations()
            }
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.body1,
                text = stringResource(R.string.battery_optimazation_info)
            )
            Icon(
                Icons.Rounded.RadioButtonUnchecked,
                null,
                tint = AppTheme.colors.neutral400
            )
        }
        Text(
            stringResource(R.string.battery_optimization_info_label),
            style = AppTheme.typography.body2l,
            color = AppTheme.colors.primary700
        )
    }
}

@Composable
fun DosageInfo(schedule: PrescriptionSchedule, onClickDosageInfo: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable {
                onClickDosageInfo()
            }
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.body1,
                text = when (schedule.dosageInstruction) {
                    is MedicationPlanDosageInstruction.FreeText -> schedule.dosageInstruction.text
                    is MedicationPlanDosageInstruction.Structured -> schedule.dosageInstruction.text
                    is MedicationPlanDosageInstruction.Empty -> stringResource(R.string.dosage_instruction_empty)
                    is MedicationPlanDosageInstruction.External -> stringResource(R.string.dosage_instruction_external)
                }
            )
            Icon(
                modifier = Modifier,
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = AppTheme.colors.primary700
            )
        }
        Text(stringResource(R.string.plan_schedule_dosage_instruction_label), style = AppTheme.typography.body2l)
    }
}

@Composable
private fun ScheduleActivitySection(
    schedule: PrescriptionSchedule,
    onActivateSchedule: () -> Unit,
    onDeactivateSchedule: () -> Unit
) {
    LabeledSwitch(
        checked = schedule.medicationSchedule.isActive,
        onCheckedChange = { checked ->
            if (checked) {
                onActivateSchedule()
            } else {
                onDeactivateSchedule()
            }
        }
    ) {
        Row(
            modifier = Modifier.weight(1.0f)
        ) {
            Column(
                modifier = Modifier
                    .weight(1.0f)
            ) {
                Text(
                    text = stringResource(R.string.activate_schedule_switch_text),
                    style = AppTheme.typography.body1
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
fun MedicationPlanScheduleScreenPreview(
    @PreviewParameter(MedicationPlanScheduleScreenPreviewParameter::class) previewData: MedicationScheduleScreenPreview
) {
    PreviewAppTheme {
        val listState = rememberLazyListState()

        MedicationPlanScheduleScaffold(
            listState = listState,
            prescriptionScheduleData = previewData.state,
            currentDate = previewData.currentDate,
            isIgnoringBatteryOptimizations = previewData.isIgnoringBatteryOptimizations,
            onDosageClicked = {},
            onClickDosageInfo = {},
            onActivateSchedule = {},
            onDeactivateSchedule = {},
            onAddNewTimeSlot = {},
            onClickChangeDateRange = {},
            onRemoveNotificationTime = {},
            onNotificationTimeClick = {},
            onShowBatteryOptimizationDialog = {},
            onBack = {}
        )
    }
}
