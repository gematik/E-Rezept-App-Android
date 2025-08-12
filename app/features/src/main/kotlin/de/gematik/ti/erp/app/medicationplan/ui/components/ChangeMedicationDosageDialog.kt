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

package de.gematik.ti.erp.app.medicationplan.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosage
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotification
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.InputField
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold

@Composable
fun ChangeMedicationDosageDialog(
    event: ComposableEvent<MedicationScheduleNotification>,
    dialog: DialogScaffold,
    onDosageChanged: (MedicationScheduleNotification, MedicationScheduleNotificationDosage) -> Unit
) {
    var dosage by remember {
        mutableStateOf(event.payload?.dosage ?: MedicationScheduleNotificationDosage("", ""))
    }
    event.listen {
        dosage = it.dosage
        dialog.show { d ->
            ErezeptAlertDialog(
                title = stringResource(R.string.adjust_dosage_dialog_title),
                body = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingDefaults.Medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InputField(
                            modifier = Modifier,
                            value = dosage.ratio,
                            onValueChange = { value ->
                                dosage = dosage.copy(ratio = value)
                            },
                            label = {
                                Text(stringResource(R.string.adjust_dosage_amount_label))
                            },
                            keyBoardType = KeyboardType.Number,
                            onSubmit = {}
                        )
                        SpacerMedium()
                        InputField(
                            modifier = Modifier,
                            value = dosage.form,
                            onValueChange = { dosage = dosage.copy(form = it) },
                            label = { Text(stringResource(R.string.adjust_dosage_form_label)) },
                            keyBoardType = KeyboardType.Text,
                            onSubmit = {}
                        )
                    }
                },
                onConfirmRequest = {
                    onDosageChanged(it, dosage)
                    d.dismiss()
                },
                onDismissRequest = {
                    d.dismiss()
                }
            )
        }
    }
}
