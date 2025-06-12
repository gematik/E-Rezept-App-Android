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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.ProfileWithSchedules
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.medicationplan.presentation.rememberMedicationListScheduleScreenController
import de.gematik.ti.erp.app.medicationplan.ui.components.ProfileHeader
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationListScheduleScreenPreview
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationListScheduleScreenPreviewParameter
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class MedicationListScheduleScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val controller = rememberMedicationListScheduleScreenController()
        val profilesWithSchedulesData by controller.profilesWithSchedules.collectAsStateWithLifecycle()
        BackHandler {
            navController.navigateUp()
        }
        MedicationListScheduleScreenScaffold(
            profilesWithSchedules = profilesWithSchedulesData,
            listState = listState,
            onClickSchedule = { taskId ->
                navController.navigate(MedicationPlanRoutes.MedicationPlanPerPrescription.path(taskId))
            },
            onBack = {
                navController.navigateUp()
            }
        )
    }
}

@Composable
private fun MedicationListScheduleScreenScaffold(
    profilesWithSchedules: UiState<List<ProfileWithSchedules>>,
    listState: LazyListState,
    onClickSchedule: (String) -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier,
        topBarTitle = stringResource(R.string.medication_plan_title),
        listState = listState,
        onBack = onBack,
        navigationMode = NavigationBarMode.Back,
        content = { contentPadding ->

            UiStateMachine(
                state = profilesWithSchedules,
                onLoading = {
                    Center {
                        CircularProgressIndicator()
                    }
                },
                onEmpty = {
                    EmptyScreenComponent(
                        title = stringResource(R.string.empty_medication_plan_title),
                        body = stringResource(R.string.empty_medication_plan_info),
                        button = {}
                    )
                },
                onError = {
                    ErrorScreenComponent()
                },
                onContent = { profilesWithSchedules ->

                    MedicationListScheduleScreenContent(
                        listState = listState,
                        contentPadding = contentPadding,
                        profilesWithSchedules = profilesWithSchedules,
                        onClickSchedule = onClickSchedule
                    )
                }
            )
        }
    )
}

@Composable
private fun MedicationListScheduleScreenContent(
    listState: LazyListState,
    contentPadding: PaddingValues,
    profilesWithSchedules: List<ProfileWithSchedules>,
    onClickSchedule: (String) -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = listState
    ) {
        items(
            items = profilesWithSchedules
        ) { profileWithSchedules ->
            SpacerMedium()
            ProfileHeader(profileWithSchedules.profile)
            SpacerMedium()
            profileWithSchedules.medicationSchedules.forEach { schedule ->
                ScheduleLineItem(schedule, onClickSchedule)
            }
            SpacerMedium()
        }
    }
}

@Composable
private fun ScheduleLineItem(schedule: MedicationSchedule, onClickSchedule: (String) -> Unit) {
    val info = when {
        schedule.isActive -> stringResource(R.string.medication_plan_active)
        else -> stringResource(R.string.medication_plan_inactive)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClickSchedule(schedule.taskId) },
                role = Role.Button
            )
            .padding(
                PaddingDefaults.Medium
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = schedule.message.title,
            style = AppTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        SpacerMedium()
        Text(
            text = info,
            style = AppTheme.typography.body1
        )
        SpacerMedium()
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
    }
}

@LightDarkPreview
@Composable
fun MedicationListScheduleScreenPreview(
    @PreviewParameter(
        MedicationListScheduleScreenPreviewParameter::class
    ) previewData: MedicationListScheduleScreenPreview
) {
    PreviewAppTheme {
        val listState = rememberLazyListState()

        MedicationListScheduleScreenScaffold(
            listState = listState,
            profilesWithSchedules = previewData.state,
            onClickSchedule = {},
            onBack = {}
        )
    }
}
