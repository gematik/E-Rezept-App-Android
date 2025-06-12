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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.ui.component.Label
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.toNavigationString
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberPrescriptionDetailController
import de.gematik.ti.erp.app.prescription.detail.ui.preview.PrescriptionDetailMedicationOverviewPreviewParameter
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class PrescriptionDetailMedicationOverviewScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val taskId = remember {
            navBackStackEntry.arguments?.getString(
                PrescriptionDetailRoutes.PRESCRIPTION_DETAIL_NAV_TASK_ID
            )
        } ?: ""

        val prescriptionDetailsController = rememberPrescriptionDetailController(taskId)
        val profilePrescriptionData by prescriptionDetailsController.profilePrescription.collectAsStateWithLifecycle()

        PrescriptionDetailMedicationOverviewScreenScaffold(
            state = profilePrescriptionData,
            onBack = navController::popBackStack,
            onNavigateToMedicationDetail = { labelTaskId, selectedMedication ->
                navController.navigate(
                    PrescriptionDetailRoutes.PrescriptionDetailMedicationScreen.path(
                        taskId = labelTaskId,
                        selectedMedication = selectedMedication
                    )
                )
            }
        )
    }
}

@Composable
private fun PrescriptionDetailMedicationOverviewScreenScaffold(
    state: UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>,
    onBack: () -> Unit,
    onNavigateToMedicationDetail: (String, String) -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val listState = rememberLazyListState()

    AnimatedElevationScaffold(
        scaffoldState = scaffoldState,
        listState = listState,
        onBack = onBack,
        topBarTitle = stringResource(R.string.synced_medication_detail_header),
        navigationMode = NavigationBarMode.Back,
        snackbarHost = { SnackbarHost(it, modifier = Modifier.navigationBarsPadding()) },
        actions = {}
    ) { innerPadding ->
        UiStateMachine(
            state = state,
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
            onContent = { (_, prescription) ->
                val syncedPrescription = prescription as? PrescriptionData.Synced
                syncedPrescription?.medicationRequest?.medication?.let { medication ->
                    PrescriptionDetailMedicationOverviewScreenContent(
                        listState = listState,
                        innerPadding = innerPadding,
                        medication = medication,
                        syncedPrescription = syncedPrescription,
                        taskId = syncedPrescription.taskId,
                        onLabelClick = onNavigateToMedicationDetail
                    )
                }
            }
        )
    }
}

@Composable
private fun PrescriptionDetailMedicationOverviewScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    medication: SyncedTaskData.Medication,
    syncedPrescription: PrescriptionData.Synced,
    taskId: String,
    onLabelClick: (String, String) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            SpacerMedium()
            Text(
                stringResource(R.string.medication_overview_prescribed_header),
                style = AppTheme.typography.h6,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
            SpacerMedium()
            Label(
                text = medication.name(),
                label = null,
                onClick = {
                    onLabelClick(
                        taskId,
                        PrescriptionData.Medication.Request(syncedPrescription.medicationRequest).toNavigationString()
                    )
                }
            )
        }
        item {
            SpacerXXLarge()
            Text(
                stringResource(R.string.medication_overview_dispenses_header),
                style = AppTheme.typography.h6,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
            SpacerMedium()
        }

        syncedPrescription.medicationDispenses.forEach { dispense ->
            dispense.medication?.let { medication ->
                item {
                    Label(
                        text = medication.name(),
                        label = null,
                        onClick = {
                            onLabelClick(
                                taskId,
                                PrescriptionData.Medication.Dispense(dispense).toNavigationString()
                            )
                        }
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun PrescriptionDetailMedicationOverviewScreenPreview(
    @PreviewParameter(PrescriptionDetailMedicationOverviewPreviewParameter::class)
    state: UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>
) {
    PreviewAppTheme {
        PrescriptionDetailMedicationOverviewScreenScaffold(
            state = state,
            onBack = {},
            onNavigateToMedicationDetail = { _, _ -> }
        )
    }
}
