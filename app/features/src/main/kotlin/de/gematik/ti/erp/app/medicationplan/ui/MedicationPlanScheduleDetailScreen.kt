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

//noinspection UsingMaterialAndMaterial3Libraries
import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.sharp.Alarm
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotification
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosage
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.medicationplan.presentation.canScheduleExactAlarms
import de.gematik.ti.erp.app.medicationplan.presentation.checkNotificationPermission
import de.gematik.ti.erp.app.medicationplan.presentation.openExactAlarmsPermission
import de.gematik.ti.erp.app.medicationplan.presentation.rememberMedicationPlanScheduleDetailScreenController
import de.gematik.ti.erp.app.medicationplan.ui.components.AddMedicationNotificationTimeDialog
import de.gematik.ti.erp.app.medicationplan.ui.components.ChangeMedicationDosageDialog
import de.gematik.ti.erp.app.medicationplan.ui.components.ChangeMedicationNotificationTimeDialog
import de.gematik.ti.erp.app.medicationplan.ui.components.DeleteMedicationScheduleDialog
import de.gematik.ti.erp.app.medicationplan.ui.components.ScheduleExactAlarmsDialog
import de.gematik.ti.erp.app.medicationplan.ui.components.activateScheduleAndDosageInstructionCard
import de.gematik.ti.erp.app.medicationplan.ui.components.scheduleTimeSelectionSection
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationPlanScheduleDetailScreenPreview
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationPlanScheduleDetailScreenPreviewParameter
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MedicationPlanScheduleDetailScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    override fun Content() {
        val taskId =
            navBackStackEntry.arguments?.getString(
                PrescriptionDetailRoutes.PRESCRIPTION_DETAIL_NAV_TASK_ID
            ) ?: return
        val context = LocalContext.current
        val dialogScaffold = LocalDialog.current
        val controller = rememberMedicationPlanScheduleDetailScreenController(taskId)
        val medicationScheduleUiState by controller.medicationSchedule.collectAsStateWithLifecycle()
        val dosageInstruction by controller.dosageInstruction.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()

        val canScheduleExactAlarms = canScheduleExactAlarms()
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val onBack by rememberUpdatedState {
            navController.popBackStack()
        }
        val defaultMedicationDosage = MedicationScheduleNotificationDosage(
            stringResource(R.string.medication_plan_default_form),
            context.getString(
                R.string.medication_plan_default_dosage
            )
        )

        @Requirement(
            "O.Plat_5#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Reminder notifications are deactivated by default.",
            codeLines = 50
        )
        val notificationPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    controller.activateSchedule()
                } else {
                    controller.deactivateSchedule()
                }
            }

        with(controller) {
            ChangeMedicationNotificationTimeDialog(
                event = changeMedicationScheduleNotificationTimeEvent,
                dialogScaffold = dialogScaffold,
                onConfirmChosenTime = { notification, time -> controller.changeMedicationNotificationTime(notification, time) }
            )
            AddMedicationNotificationTimeDialog(
                event = addMedicationNotificationTimeEvent,
                dialogScaffold = dialogScaffold,
                onConfirmChosenTime = { time ->
                    controller.addNewMedicationNotification(
                        dosage = medicationScheduleUiState.data?.notifications?.firstOrNull()?.dosage
                            ?: defaultMedicationDosage,
                        time = time
                    )
                }
            )
            ChangeMedicationDosageDialog(
                event = changeMedicationScheduleNotificationDosageEvent,
                dialog = dialogScaffold,
                onDosageChanged = { notification, dosage ->
                    controller.changeMedicationNotificationDosage(notification, dosage)
                }
            )
            ScheduleExactAlarmsDialog(
                event = changeAllowExactAlarmsEvent,
                canScheduleExactAlarms = canScheduleExactAlarms,
                dialogScaffold = dialogScaffold,
                onToggleExactAlarms = {
                    context.openExactAlarmsPermission()
                }
            )
            DeleteMedicationScheduleDialog(
                event = deleteMedicationScheduleEvent,
                dialogScaffold = dialogScaffold,
                onClickAction = {
                    medicationScheduleUiState.data?.let {
                        controller.deleteMedicationSchedule(it.taskId)
                    }
                    onBack()
                }
            )
        }

        BackHandler {
            onBack()
        }
        MedicationPlanScheduleDetailScreenScaffold(
            listState = listState,
            medicationScheduleUiState = medicationScheduleUiState,
            dosageInstruction = dosageInstruction,
            currentDate = currentDate,
            canScheduleExactAlarms = canScheduleExactAlarms,
            onAddNewTimeSlot = {
                controller.onTriggerAddMedicationNotificationTimeEvent()
            },
            onRemoveNotificationTime = { notification ->
                controller.removeMedicationNotification(notification)
            },
            onClickChangeDateRange = {
                navController.navigate(
                    MedicationPlanRoutes.MedicationPlanScheduleDurationAndIntervalScreen.path(taskId = taskId)
                )
            },
            onActivateSchedule = {
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
                        controller.activateSchedule()
                    }
                )
            },
            onDeactivateSchedule = {
                controller.deactivateSchedule()
            },
            onNotificationTimeClick = { notification ->
                controller.onTriggerChangeMedicationNotificationTimeEvent(notification)
            },
            onClickDosageInfo = {
                taskId.let {
                    navController.navigate(
                        MedicationPlanRoutes.MedicationPlanDosageInstructionBottomSheetScreen.path(taskId = taskId)
                    )
                }
            },
            onDosageClicked = { notification ->
                controller.onTriggerChangeMedicationNotificationDosageEvent(notification)
            },
            onShowBatteryOptimizationDialog = { controller.onTriggerShowIgnoreBatteryOptimizationEvent() },
            onClickDelete = { controller.onTriggerShowDeleteDialogEvent() },
            onBack = { onBack() }
        )
    }
}

@Composable
fun MedicationPlanScheduleDetailScreenScaffold(
    listState: LazyListState,
    medicationScheduleUiState: UiState<MedicationSchedule>,
    dosageInstruction: MedicationPlanDosageInstruction,
    currentDate: LocalDate,
    canScheduleExactAlarms: Boolean,
    onAddNewTimeSlot: () -> Unit,
    onRemoveNotificationTime: (MedicationScheduleNotification) -> Unit,
    onNotificationTimeClick: (MedicationScheduleNotification) -> Unit,
    onClickChangeDateRange: () -> Unit,
    onClickDosageInfo: () -> Unit,
    onClickDelete: () -> Unit,
    onDosageClicked: (MedicationScheduleNotification) -> Unit,
    onActivateSchedule: () -> Unit,
    onDeactivateSchedule: () -> Unit,
    onShowBatteryOptimizationDialog: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        listState = listState,
        topBarTitle = stringResource(R.string.medication_plan_title),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        navigationMode = NavigationBarMode.Back,
        onBack = onBack,
        actions = { ThreeDotMenu(onClickDelete = onClickDelete) }
    ) { contentPadding ->
        UiStateMachine(
            state = medicationScheduleUiState,
            onLoading = {
                FullScreenLoadingIndicator()
            },
            onEmpty = {
                EmptyScreenComponent(
                    title = stringResource(R.string.empty_medication_plan_title),
                    body = stringResource(R.string.empty_medication_plan_info),
                    image = {
                        Image(
                            painter = painterResource(id = R.drawable.girl_red_oh_no),
                            contentDescription = null,
                            modifier = Modifier.size(SizeDefaults.twentyfold)
                        )
                    },
                    button = {}
                )
            },
            onError = {
                ErrorScreenComponent()
            },
            onContent = { prescriptionSchedule ->
                MedicationPlanScheduleDetailScreenContent(
                    listState = listState,
                    contentPadding = contentPadding,
                    dosageInstruction = dosageInstruction,
                    currentDate = currentDate,
                    medicationSchedule = prescriptionSchedule,
                    isIgnoringBatteryOptimizations = canScheduleExactAlarms,
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

@Suppress("LongMethod", "MaxLineLength")
@Composable
private fun MedicationPlanScheduleDetailScreenContent(
    listState: LazyListState,
    contentPadding: PaddingValues,
    medicationSchedule: MedicationSchedule,
    dosageInstruction: MedicationPlanDosageInstruction,
    currentDate: LocalDate,
    isIgnoringBatteryOptimizations: Boolean,
    onAddNewItem: () -> Unit,
    onRemoveNotificationTime: (MedicationScheduleNotification) -> Unit,
    onNotificationTimeClick: (MedicationScheduleNotification) -> Unit,
    onClickChangeDateRange: () -> Unit,
    onClickDosageInfo: () -> Unit,
    onDosageClicked: (MedicationScheduleNotification) -> Unit,
    onActivateSchedule: () -> Unit,
    onDeactivateSchedule: () -> Unit,
    onShowBatteryOptimizationDialog: () -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.XXLarge),
        modifier = Modifier.fillMaxSize().padding(horizontal = PaddingDefaults.Medium)
    ) {
        medicationScheduleScreenHeader(medicationSchedule = medicationSchedule)
        activateScheduleAndDosageInstructionCard(
            schedule = medicationSchedule,
            dosageInstruction = dosageInstruction,
            isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations,
            onIgnoreBatteryOptimizations = onShowBatteryOptimizationDialog,
            onActivateSchedule = onActivateSchedule,
            onDeactivateSchedule = onDeactivateSchedule,
            onClickDosageInfo = onClickDosageInfo
        )
        if (medicationSchedule.isActive) {
            scheduleTimeSelectionSection(
                medicationSchedule = medicationSchedule,
                currentDate = currentDate,
                onClickChangeDateRange = onClickChangeDateRange,
                onAddNewItem = onAddNewItem,
                onRemoveNotificationTime = onRemoveNotificationTime,
                onNotificationTimeClick = onNotificationTimeClick,
                onDosageClicked = onDosageClicked
            )
        }
    }
}

private fun LazyListScope.medicationScheduleScreenHeader(
    medicationSchedule: MedicationSchedule
) {
    item {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PaddingDefaults.Medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
        ) {
            Icon(
                imageVector = Icons.Sharp.Alarm,
                contentDescription = null,
                tint = AppTheme.colors.primary700,
                modifier = Modifier
                    .size(SizeDefaults.sevenfold)
            )
            Text(
                text = medicationSchedule.message.title.ifBlank {
                    stringResource(R.string.medication_plan_missing_medication_name)
                },
                style = AppTheme.typography.h6,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ThreeDotMenu(
    onClickDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.a11y_medication_schedule_three_dot_menu)
    IconButton(
        onClick = { expanded = true },
        modifier = Modifier.testTag(TestTag.Profile.ThreeDotMenuButton)
            .semantics { contentDescription = description }
    ) {
        Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral600)
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        offset = DpOffset(SizeDefaults.triple, SizeDefaults.zero)
    ) {
        DropdownMenuItem(
            modifier = Modifier
                .semantics { role = Role.Button },
            onClick = {
                expanded = false
                onClickDelete()
            }
        ) {
            Text(
                text = stringResource(R.string.remove_medication_schedule),
                color = AppTheme.colors.red600
            )
        }
    }
}

@LightDarkPreview
@Composable
fun MedicationPlanScheduleDetailScreenPreview(
    @PreviewParameter(MedicationPlanScheduleDetailScreenPreviewParameter::class) previewData: MedicationPlanScheduleDetailScreenPreview
) {
    PreviewAppTheme {
        val listState = rememberLazyListState()

        MedicationPlanScheduleDetailScreenScaffold(
            listState = listState,
            medicationScheduleUiState = previewData.state,
            dosageInstruction = previewData.dosageInstruction,
            currentDate = previewData.currentDate,
            canScheduleExactAlarms = previewData.isIgnoringBatteryOptimizations,
            onDosageClicked = {},
            onClickDosageInfo = {},
            onActivateSchedule = {},
            onDeactivateSchedule = {},
            onAddNewTimeSlot = {},
            onClickChangeDateRange = {},
            onRemoveNotificationTime = {},
            onNotificationTimeClick = {},
            onShowBatteryOptimizationDialog = {},
            onClickDelete = {},
            onBack = {}
        )
    }
}
