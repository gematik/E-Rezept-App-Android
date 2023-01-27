/*
 * Copyright (c) 2023 gematik GmbH
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ArchiveScreen(prescriptionViewModel: PrescriptionViewModel, navController: NavController, onBack: () -> Unit) {
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.archive_screen_title),
        listState = listState,
        onBack = onBack,
        navigationMode = NavigationBarMode.Back
    ) {
        val state by produceState<PrescriptionScreenData.State?>(null) {
            prescriptionViewModel.screenState().collect {
                value = it
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { SpacerXXLarge() }

            state?.let {
                it.redeemedPrescriptions.forEachIndexed { index, prescription ->
                    item {
                        val previousPrescriptionRedeemedOn =
                            it.redeemedPrescriptions.getOrNull(index - 1)
                                ?.redeemedOrExpiredOn()
                                ?.atZone(ZoneId.systemDefault())?.toLocalDate()

                        val redeemedOn = prescription.redeemedOrExpiredOn()
                            .atZone(ZoneId.systemDefault()).toLocalDate()

                        val yearChanged = remember {
                            previousPrescriptionRedeemedOn?.year != redeemedOn.year
                        }

                        if (yearChanged) {
                            val instantOfArchivedPrescription = remember {
                                val dateFormatter = DateTimeFormatter.ofPattern("yyyy")
                                redeemedOn.format(dateFormatter)
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
                            is PrescriptionUseCaseData.Prescription.Scanned ->
                                LowDetailMedication(
                                    modifier = CardPaddingModifier,
                                    prescription,
                                    onClick = {
                                        navController.navigate(
                                            MainNavigationScreens.PrescriptionDetail.path(
                                                taskId = prescription.taskId
                                            )
                                        )
                                    }
                                )

                            is PrescriptionUseCaseData.Prescription.Synced ->
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
}
