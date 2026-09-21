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

package de.gematik.ti.erp.app.prescription.ui.components

import androidx.compose.foundation.lazy.LazyListScope
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStateErpModel

fun LazyListScope.prescriptionContentSection(
    activePrescriptions: List<TaskErpModel>,
    onClickPrescription: (String, Boolean, Boolean) -> Unit
) {
    activePrescriptions.forEach { prescription ->
        val typeSuffix = when (prescription) {
            is TaskErpModel.Scanned -> "scanned"
            is TaskErpModel.Synced.Diga -> "diga"
            is TaskErpModel.Synced.Prescription -> "synced"
        }
        item(key = "prescription-${prescription.taskId}-$typeSuffix") {
            when (prescription) {
                is TaskErpModel.Synced.Diga ->
                    FullDetailDiga(
                        modifier = CardPaddingModifier,
                        prescription = prescription,
                        onClick = {
                            onClickPrescription(prescription.taskId, true, prescription.state() is TaskStateErpModel.Ready)
                        }
                    )

                is TaskErpModel.Synced.Prescription ->
                    FullDetailMedication(
                        modifier = CardPaddingModifier,
                        prescription = prescription,
                        onClick = {
                            onClickPrescription(prescription.taskId, false, false)
                        }
                    )

                is TaskErpModel.Scanned -> {
                    LowDetailMedication(
                        modifier = CardPaddingModifier,
                        prescription,
                        onClick = {
                            onClickPrescription(prescription.taskId, false, false)
                        }
                    )
                }
            }
        }
    }
}
