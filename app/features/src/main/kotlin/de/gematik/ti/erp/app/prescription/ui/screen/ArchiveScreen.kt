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

package de.gematik.ti.erp.app.prescription.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.digas.navigation.DigasRoutes
import de.gematik.ti.erp.app.digas.ui.component.CustomSegmentedButton
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.prescription.detail.navigation.PrescriptionDetailRoutes
import de.gematik.ti.erp.app.prescription.presentation.rememberPrescriptionListController
import de.gematik.ti.erp.app.prescription.ui.components.DigaSection
import de.gematik.ti.erp.app.prescription.ui.components.PrescriptionSection
import de.gematik.ti.erp.app.prescription.ui.model.ArchiveSegmentedControllerTap
import de.gematik.ti.erp.app.prescription.ui.preview.PrescriptionsArchiveScreenPreviewParameterProvider
import de.gematik.ti.erp.app.prescription.ui.preview.PrescriptionsDigasArchiveScreenPreviewData
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState

class PrescriptionsArchiveScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val controller = rememberPrescriptionListController()
        val listState = rememberLazyListState()
        val archivedPrescriptions by controller.archivedPrescriptions.collectAsStateWithLifecycle()
        val archivedDigas by controller.archivedDigas.collectAsStateWithLifecycle()
        var selectedTab by remember { mutableStateOf(ArchiveSegmentedControllerTap.PRESCRIPTION) }
        BackHandler {
            navController.popBackStack()
        }

        PrescriptionsArchiveScreenScaffold(
            listState = listState,
            archivedPrescriptions = archivedPrescriptions,
            archivedDigas = archivedDigas,
            onBack = { navController.popBackStack() },
            selectedTab = selectedTab,
            onTabChange = {
                selectedTab = ArchiveSegmentedControllerTap.entries.toTypedArray()[it]
            },
            onOpenPrescriptionDetailScreen = { taskId, isDiga ->
                navController.navigate(
                    if (isDiga) {
                        DigasRoutes.DigasMainScreen.path(taskId)
                    } else {
                        PrescriptionDetailRoutes.PrescriptionDetailScreen.path(
                            taskId = taskId
                        )
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrescriptionsArchiveScreenScaffold(
    listState: LazyListState,
    archivedPrescriptions: UiState<List<Prescription>>,
    archivedDigas: UiState<List<Prescription>>,
    selectedTab: ArchiveSegmentedControllerTap,
    onTabChange: (Int) -> Unit,
    onBack: () -> Unit,
    onOpenPrescriptionDetailScreen: (String, Boolean) -> Unit
) {
    val options = listOf(stringResource(R.string.pres_bottombar_prescriptions), stringResource(R.string.digas_name))
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.archive_screen_title),
        listState = listState,
        onBack = onBack,
        navigationMode = NavigationBarMode.Back
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTag.Prescriptions.Archive.Content),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { SpacerXXLarge() }
            if (archivedDigas.isDataState) {
                item {
                    SingleChoiceSegmentedButtonRow(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PaddingDefaults.Medium)
                    ) {
                        options.forEachIndexed { index, label ->
                            CustomSegmentedButton(
                                index = index,
                                options = options,
                                selectedIndex = selectedTab.index,
                                label = label,
                                onClick = { onTabChange(index) }
                            )
                        }
                    }
                }

                when (selectedTab) {
                    ArchiveSegmentedControllerTap.PRESCRIPTION -> item {
                        PrescriptionSection(archivedPrescriptions, onOpenPrescriptionDetailScreen)
                    }

                    ArchiveSegmentedControllerTap.DIGAS -> item {
                        DigaSection(archivedDigas, onOpenPrescriptionDetailScreen)
                    }
                }
            } else {
                item { PrescriptionSection(archivedPrescriptions, onOpenPrescriptionDetailScreen) }
            }
        }
    }
}

@Composable
fun PrescriptionsArchiveEmptyScreenContent(
    modifier: Modifier = Modifier
) =
    EmptyScreenComponent(
        modifier = modifier,
        title = stringResource(R.string.prescription_archive_empty_screen_title),
        body = stringResource(R.string.prescription_archive_empty_screen_body),
        button = {}
    )

@LightDarkPreview
@Composable
fun PrescriptionsArchiveScreenPreview(
    @PreviewParameter(PrescriptionsArchiveScreenPreviewParameterProvider::class)
    mockPrescriptionsAndDigas: PrescriptionsDigasArchiveScreenPreviewData
) {
    val listState = rememberLazyListState()
    PreviewAppTheme {
        PrescriptionsArchiveScreenScaffold(
            listState = listState,
            archivedPrescriptions = mockPrescriptionsAndDigas.archivedPrescriptions,
            archivedDigas = mockPrescriptionsAndDigas.archivedDigas,
            onBack = { },
            selectedTab = mockPrescriptionsAndDigas.selectedTab,
            onTabChange = {},
            onOpenPrescriptionDetailScreen = { _, _ -> }
        )
    }
}
