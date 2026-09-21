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
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.digas.ui.component.Label
import de.gematik.ti.erp.app.error.ErrorScreenComponent
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.detail.presentation.rememberPrescriptionDetailTeratogenicPrescriptionController
import de.gematik.ti.erp.app.prescription.detail.ui.preview.PrescriptionDetailTeratogenicPrescriptionPreviewData
import de.gematik.ti.erp.app.prescription.detail.ui.preview.PrescriptionDetailTeratogenicPrescriptionPreviewParameter
import de.gematik.ti.erp.app.task.model.TeratogenicPrescriptionErpModel
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState

class PrescriptionDetailTeratogenicPrescriptionScreen(
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
        val controller = rememberPrescriptionDetailTeratogenicPrescriptionController(taskId)
        val teratogenicPrescription by controller.teratogenicPrescription.collectAsStateWithLifecycle()
        PrescriptionDetailTeratogenicPrescriptionScreen(
            prescription = teratogenicPrescription,
            onBack = navController::popBackStack
        )
    }
}

@Composable
fun PrescriptionDetailTeratogenicPrescriptionScreen(
    prescription: UiState<TeratogenicPrescriptionErpModel>,
    onBack: () -> Unit
) {
    UiStateMachine(
        state = prescription,
        onLoading = {
            Center {
                CircularProgressIndicator()
            }
        },
        onEmpty = {
            ErrorScreenComponent(
                titleText = stringResource(R.string.generic_error_title),
                bodyText = stringResource(R.string.generic_error_info),
                tryAgainText = stringResource(R.string.cdw_fasttrack_try_again)
            )
        },
        onError = {
            ErrorScreenComponent(
                titleText = stringResource(R.string.generic_error_title),
                bodyText = stringResource(R.string.generic_error_info),
                tryAgainText = stringResource(R.string.cdw_fasttrack_try_again)
            )
        },
        onContent = { prescription ->
            val listState = rememberLazyListState()
            AnimatedElevationScaffold(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformation.Screen),
                topBarTitle = stringResource(R.string.pres_detail_teratogenic_prescription_screen_title),
                backLabel = stringResource(R.string.back),
                closeLabel = stringResource(R.string.cancel),
                listState = listState,
                onBack = onBack,
                navigationMode = NavigationBarMode.Back
            ) { innerPadding ->
                PrescriptionDetailTeratogenicPrescriptionScreenContent(
                    listState = listState,
                    innerPadding = innerPadding,
                    teratogenicPrescription = prescription
                )
            }
        }
    )
}

@Composable
private fun PrescriptionDetailTeratogenicPrescriptionScreenContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    teratogenicPrescription: TeratogenicPrescriptionErpModel
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
                text = if (teratogenicPrescription.einhaltungSicherheitsmassnahmen) {
                    stringResource(R.string.pres_detail_yes)
                } else { stringResource(R.string.pres_detail_no) },
                label = stringResource(R.string.pres_detail_teratogenic_prescription_screen_savety)
            )
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformation.AccessCode),
                text = if (teratogenicPrescription.aushaendigungInformationsmaterialien) {
                    stringResource(R.string.pres_detail_yes)
                } else { stringResource(R.string.pres_detail_no) },
                label = stringResource(R.string.pres_detail_teratogenic_prescription_screen_info)
            )
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformation.AccessCode),
                text = if (teratogenicPrescription.erklaerungSachkenntnis) {
                    stringResource(R.string.pres_detail_yes)
                } else { stringResource(R.string.pres_detail_no) },
                label = stringResource(R.string.pres_detail_teratogenic_prescription_screen_prescriber)
            )
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformation.AccessCode),
                text = if (teratogenicPrescription.offLabel) {
                    stringResource(R.string.pres_detail_yes)
                } else { stringResource(R.string.pres_detail_no) },
                label = stringResource(R.string.pres_detail_teratogenic_prescription_screen_offlabel)
            )
        }
        item {
            Label(
                modifier = Modifier.testTag(TestTag.Prescriptions.Details.TechnicalInformation.AccessCode),
                text = if (teratogenicPrescription.gebaerfaehigeFrau) {
                    stringResource(R.string.pres_detail_yes)
                } else { stringResource(R.string.pres_detail_no) },
                label = stringResource(R.string.pres_detail_teratogenic_prescription_screen_child_bearing)
            )
            SpacerMedium()
        }
    }
}

@LightDarkPreview
@Composable
fun PrescriptionDetailTeratogenicPrescriptionScreenPreview(
    @PreviewParameter(PrescriptionDetailTeratogenicPrescriptionPreviewParameter::class)
    previewData: PrescriptionDetailTeratogenicPrescriptionPreviewData
) {
    PreviewAppTheme {
        PrescriptionDetailTeratogenicPrescriptionScreen(
            prescription = previewData.state,
            onBack = {}
        )
    }
}
