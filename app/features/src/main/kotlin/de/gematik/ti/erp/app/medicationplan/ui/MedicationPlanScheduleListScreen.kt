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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.ProfileWithSchedules
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.medicationplan.presentation.rememberMedicationPlanScheduleListScreenController
import de.gematik.ti.erp.app.medicationplan.ui.components.ProfileHeader
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationPlanScheduleListScreenPreview
import de.gematik.ti.erp.app.medicationplan.ui.preview.MedicationPlanScheduleListScreenPreviewParameter
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.FullScreenLoadingIndicator
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class MedicationPlanScheduleListScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val listState = rememberLazyListState()
        val controller = rememberMedicationPlanScheduleListScreenController()
        val profilesWithSchedulesData by controller.profilesWithSchedules.collectAsStateWithLifecycle()
        val onBack by rememberUpdatedState {
            navController.popBackStack()
        }

        BackHandler {
            onBack()
        }
        MedicationPlanScheduleListScreenScaffold(
            profilesWithSchedules = profilesWithSchedulesData,
            listState = listState,
            onClickSchedule = { taskId ->
                navController.navigate(MedicationPlanRoutes.MedicationPlanScheduleDetailScreen.path(taskId))
            },
            onOpenMedicationPlanOverview = {}, // later version
            onBack = {
                onBack()
            }
        )
    }
}

@Composable
private fun MedicationPlanScheduleListScreenScaffold(
    profilesWithSchedules: UiState<List<ProfileWithSchedules>>,
    listState: LazyListState,
    onClickSchedule: (String) -> Unit,
    onOpenMedicationPlanOverview: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier,
        topBarTitle = stringResource(R.string.medication_plan_title),
        backLabel = stringResource(R.string.back),
        closeLabel = stringResource(R.string.cancel),
        listState = listState,
        onBack = onBack,
        navigationMode = NavigationBarMode.Back,
        content = { contentPadding ->
            UiStateMachine(
                state = profilesWithSchedules,
                onLoading = {
                    FullScreenLoadingIndicator()
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
                    MedicationPlanScheduleListScreenContent(
                        listState = listState,
                        contentPadding = contentPadding,
                        profilesWithSchedules = profilesWithSchedules,
                        onClickSchedule = onClickSchedule,
                        onOpenMedicationPlanOverview = onOpenMedicationPlanOverview
                    )
                }
            )
        }
    )
}

@Composable
private fun MedicationPlanScheduleListScreenContent(
    listState: LazyListState,
    contentPadding: PaddingValues,
    profilesWithSchedules: List<ProfileWithSchedules>,
    onOpenMedicationPlanOverview: () -> Unit,
    onClickSchedule: (String) -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = listState
    ) {
        items(
            items = profilesWithSchedules
        ) { profileWithSchedules ->
            ProfileWithSchedulesComponent(
                profile = profileWithSchedules.profile,
                profileSchedules = profileWithSchedules.medicationSchedules,
                onOpenMedicationPlanOverview = onOpenMedicationPlanOverview,
                onClickSchedule = onClickSchedule
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ProfileWithSchedulesComponent(
    profile: ProfilesUseCaseData.Profile,
    profileSchedules: List<MedicationSchedule>,
    onOpenMedicationPlanOverview: () -> Unit,
    onClickSchedule: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = PaddingDefaults.Medium),
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        ProfileHeader(
            profile = profile
        )
        /* comes in later version
        MedicationPlanOverviewSection(
            onOpenMedicationPlanOverview = onOpenMedicationPlanOverview
        )
         */
        SchedulesSection(
            profileSchedules = profileSchedules,
            onClickSchedule = onClickSchedule
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MedicationPlanOverviewSection(
    onOpenMedicationPlanOverview: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(
            onClick = { onOpenMedicationPlanOverview() },
            role = Role.Button
        ),
        text = {
            Text(
                text = stringResource(R.string.medication_plan_overview),
                style = AppTheme.typography.body1
            )
        },
        trailing = {
            Icon(
                Icons.Rounded.ChevronRight,
                tint = AppTheme.colors.neutral600,
                contentDescription = null
            )
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SchedulesSection(
    profileSchedules: List<MedicationSchedule>,
    onClickSchedule: (String) -> Unit
) {
    profileSchedules.forEach { schedule ->
        val info = when {
            schedule.isActive -> stringResource(R.string.medication_plan_active)
            else -> stringResource(R.string.medication_plan_inactive)
        }
        ListItem(
            modifier = Modifier
                .clickable(
                    onClick = { onClickSchedule(schedule.taskId) },
                    role = Role.Button
                ),
            text = {
                Text(
                    text = schedule.message.title,
                    style = AppTheme.typography.body1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)) {
                    Text(
                        text = info,
                        style = AppTheme.typography.body1,
                        color = AppTheme.colors.neutral600
                    )
                    Icon(Icons.Rounded.ChevronRight, null, tint = AppTheme.colors.neutral600)
                }
            }
        )
    }
}

@LightDarkPreview
@Composable
fun MedicationPlanScheduleListScreenPreview(
    @PreviewParameter(
        MedicationPlanScheduleListScreenPreviewParameter::class
    ) previewData: MedicationPlanScheduleListScreenPreview
) {
    PreviewAppTheme {
        val listState = rememberLazyListState()

        MedicationPlanScheduleListScreenScaffold(
            listState = listState,
            profilesWithSchedules = previewData.state,
            onClickSchedule = {},
            onOpenMedicationPlanOverview = {},
            onBack = {}
        )
    }
}
