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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import de.gematik.ti.erp.app.prescription.presentation.rememberPrescriptionsController
import de.gematik.ti.erp.app.prescription.ui.components.CardPaddingModifier
import de.gematik.ti.erp.app.prescription.ui.components.FullDetailMedication
import de.gematik.ti.erp.app.prescription.ui.components.LowDetailMedication
import de.gematik.ti.erp.app.prescription.ui.preview.PrescriptionsArchiveScreenPreviewParameterProvider
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.ScannedPrescription
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.SyncedPrescription
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.EmptyScreenComponent
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

class PrescriptionsArchiveScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val controller = rememberPrescriptionsController()
        val listState = rememberLazyListState()
        val archivedPrescriptions by controller.archivedPrescriptions.collectAsStateWithLifecycle()

        BackHandler {
            navController.popBackStack()
        }

        PrescriptionsArchiveScreenScaffold(
            listState = listState,
            archivedPrescriptions = archivedPrescriptions,
            onBack = { navController.popBackStack() },
            onOpenPrescriptionDetailScreen = {
                navController.navigate(
                    PrescriptionDetailRoutes.PrescriptionDetailScreen.path(
                        taskId = it
                    )
                )
            }
        )
    }
}

@Composable
private fun PrescriptionsArchiveScreenScaffold(
    listState: LazyListState,
    archivedPrescriptions: UiState<List<Prescription>>,
    onBack: () -> Unit,
    onOpenPrescriptionDetailScreen: (String) -> Unit
) {
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.archive_screen_title),
        listState = listState,
        onBack = onBack,
        navigationMode = NavigationBarMode.Back
    ) {
        UiStateMachine(
            state = archivedPrescriptions,
            onError = {
                ErrorScreenComponent()
            },
            onEmpty = {
                PrescriptionsArchiveEmptyScreenContent()
            },
            onLoading = {
                Center {
                    CircularProgressIndicator()
                }
            }
        ) { prescriptions ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTag.Prescriptions.Archive.Content),
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { SpacerXXLarge() }

                prescriptions.forEachIndexed { index, prescription ->
                    item(key = "prescription-${prescription.taskId}") {
                        val previousPrescriptionRedeemedOn =
                            prescriptions.getOrNull(index - 1)
                                ?.redeemedOrExpiredOn()
                                ?.toLocalDateTime(TimeZone.currentSystemDefault())

                        val redeemedOn = prescription.redeemedOrExpiredOn()
                            .toLocalDateTime(TimeZone.currentSystemDefault())

                        val yearChanged = remember {
                            previousPrescriptionRedeemedOn?.year != redeemedOn.year
                        }

                        if (yearChanged) {
                            val instantOfArchivedPrescription = remember {
                                val dateFormatter = DateTimeFormatter.ofPattern("yyyy")
                                redeemedOn.toJavaLocalDateTime().format(dateFormatter)
                            }

                            Text(
                                text = instantOfArchivedPrescription,
                                style = AppTheme.typography.h6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(CardPaddingModifier)
                            )
                        }

                        when (prescription) {
                            is ScannedPrescription ->
                                LowDetailMedication(
                                    modifier = CardPaddingModifier,
                                    prescription,
                                    onClick = {
                                        onOpenPrescriptionDetailScreen(prescription.taskId)
                                    }
                                )

                            is SyncedPrescription ->
                                FullDetailMedication(
                                    prescription,
                                    modifier = CardPaddingModifier,
                                    onClick = {
                                        onOpenPrescriptionDetailScreen(prescription.taskId)
                                    }
                                )
                        }
                    }
                }
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
    archivedPrescriptions: UiState<List<Prescription>>
) {
    val listState = rememberLazyListState()

    PreviewAppTheme {
        PrescriptionsArchiveScreenScaffold(
            listState = listState,
            archivedPrescriptions = archivedPrescriptions,
            onBack = { },
            onOpenPrescriptionDetailScreen = {}
        )
    }
}
