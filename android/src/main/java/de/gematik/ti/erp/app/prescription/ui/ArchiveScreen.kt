/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.ScannedPrescription
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.SyncedPrescription
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ArchiveScreen(
    prescriptionsController: PrescriptionsController,
    navController: NavController,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.archive_screen_title),
        listState = listState,
        onBack = onBack,
        navigationMode = NavigationBarMode.Back
    ) {
        val archivedPrescriptions by prescriptionsController.archivedPrescriptionsState

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTag.Prescriptions.Archive.Content),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { SpacerXXLarge() }

            archivedPrescriptions.forEachIndexed { index, prescription ->
                item(key = "prescription-${prescription.taskId}") {
                    val previousPrescriptionRedeemedOn =
                        archivedPrescriptions.getOrNull(index - 1)
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
                                0,
                                onClick = {
                                    navController.navigate(
                                        MainNavigationScreens.PrescriptionDetail.path(
                                            taskId = prescription.taskId
                                        )
                                    )
                                }
                            )

                        is SyncedPrescription ->
                            FullDetailMedication(
                                prescription,
                                modifier = CardPaddingModifier,
                                onClick = {
                                    navController.navigate(
                                        MainNavigationScreens.PrescriptionDetail.path(
                                            taskId = prescription.taskId
                                        )
                                    )
                                }
                            )
                    }
                }
            }
        }
    }
}
