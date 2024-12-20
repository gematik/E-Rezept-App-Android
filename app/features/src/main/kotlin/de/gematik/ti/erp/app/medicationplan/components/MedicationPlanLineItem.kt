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

package de.gematik.ti.erp.app.medicationplan.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.utils.compose.Label

@Composable
fun MedicationPlanLineItem(
    medicationSchedule: MedicationSchedule?,
    onClick: () -> Unit
) {
    val text = when {
        medicationSchedule == null -> stringResource(R.string.pres_details_schedule_medication_title_not_scheduled)
        medicationSchedule.isActive -> stringResource(R.string.pres_details_schedule_medication_title_scheduled_active)
        else -> stringResource(R.string.pres_details_schedule_medication_title_scheduled_not_active)
    }
    Label(
        text = text,
        label = stringResource(R.string.pres_details_schedule_medication_label),
        onClick = onClick
    )
}
