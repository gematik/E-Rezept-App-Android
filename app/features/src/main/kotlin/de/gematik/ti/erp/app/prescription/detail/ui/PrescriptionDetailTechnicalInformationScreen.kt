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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberPrescriptionDetailController
import de.gematik.ti.erp.app.prescription.detail.ui.preview.PrescriptionDetailTechnicalInfoPreviewParameter
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.Label
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class PrescriptionDetailTechnicalInformationScreen(
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

        PrescriptionDetailTechnicalInformationContent(
            profilePrescriptionData = profilePrescriptionData,
            onBack = navController::popBackStack
        )
    }
}

@Composable
fun PrescriptionDetailTechnicalInformationContent(
    profilePrescriptionData: UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>,
    onBack: () -> Unit
) {
    UiStateMachine(
        state = profilePrescriptionData,
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
            val listState = rememberLazyListState()
            AnimatedElevationScaffold(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformation.Screen),
                topBarTitle = stringResource(R.string.pres_detail_technical_information),
                listState = listState,
                onBack = onBack,
                navigationMode = NavigationBarMode.Back
            ) { innerPadding ->
                PrescriptionDetailTechnicalInformationScreen(
                    listState = listState,
                    innerPadding = innerPadding,
                    taskId = prescription.taskId,
                    accessCode = prescription.accessCode
                )
            }
        }
    )
}

@Composable
private fun PrescriptionDetailTechnicalInformationScreen(
    listState: LazyListState,
    innerPadding: PaddingValues,
    taskId: String,
    accessCode: String
) {
    LazyColumn(
        modifier =
        Modifier
            .padding(innerPadding)
            .testTag(TestTag.Prescriptions.Details.TechnicalInformation.Content),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    ) {
        item {
            SpacerMedium()
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformation.AccessCode),
                text = accessCode,
                label = stringResource(R.string.access_code)
            )
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformation.TaskId),
                text = taskId,
                label = stringResource(R.string.task_id)
            )
            SpacerMedium()
        }
    }
}

@LightDarkPreview
@Composable
fun PrescriptionDetailTechnicalInformationScreenPreview(
    @PreviewParameter(PrescriptionDetailTechnicalInfoPreviewParameter::class)
    previewUiState: UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>
) {
    PreviewAppTheme {
        PrescriptionDetailTechnicalInformationContent(
            profilePrescriptionData = previewUiState,
            onBack = {}
        )
    }
}
