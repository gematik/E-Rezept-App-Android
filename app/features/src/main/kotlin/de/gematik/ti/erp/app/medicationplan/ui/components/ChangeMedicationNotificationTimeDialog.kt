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

import androidx.compose.runtime.Composable
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotification
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.extensions.DialogScaffold
import kotlinx.datetime.LocalTime

@Composable
internal fun ChangeMedicationNotificationTimeDialog(
    event: ComposableEvent<MedicationScheduleNotification>,
    dialogScaffold: DialogScaffold,
    onConfirmChosenTime: (notification: MedicationScheduleNotification, time: LocalTime) -> Unit
) {
    event.listen { notification ->
        dialogScaffold.show { dialog ->
            GemTimePickerDialog(
                initialHour = notification.time.hour,
                initialMinute = notification.time.minute,
                onConfirm = {
                    onConfirmChosenTime(notification, it)
                    dialog.dismiss()
                },
                onDismiss = {
                    dialog.dismiss()
                }
            )
        }
    }
}
