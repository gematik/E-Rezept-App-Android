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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.prescription.ui.screen.PrescriptionsArchiveEmptyScreenContent
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.ScannedPrescription
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.SyncedPrescription
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PrescriptionSection(
    prescriptions: UiState<List<Prescription>>,
    onOpenPrescriptionDetailScreen: (String, Boolean) -> Unit
) {
    UiStateMachine(
        state = prescriptions,
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
    ) { prescriptionList ->
        Column {
            prescriptionList.forEachIndexed { index, prescription ->
                val previousPrescriptionRedeemedOn =
                    prescriptionList.getOrNull(index - 1)
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
                                onOpenPrescriptionDetailScreen(prescription.taskId, false)
                            }
                        )

                    is SyncedPrescription ->
                        if (!prescription.isDiga) {
                            FullDetailMedication(
                                prescription,
                                modifier = CardPaddingModifier,
                                onClick = {
                                    onOpenPrescriptionDetailScreen(prescription.taskId, prescription.isDiga)
                                }
                            )
                        }
                }
            }
        }
    }
}
