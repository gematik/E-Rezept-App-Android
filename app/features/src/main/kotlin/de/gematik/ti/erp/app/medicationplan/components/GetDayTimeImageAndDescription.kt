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

import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotification

@Suppress("MagicNumber")
fun getDayTimeImageAndDescription(
    notification: MedicationNotification
): Pair<Int, Int> {
    val hour = notification.time.hour

    val resources = when (hour) {
        in 4..10 -> R.drawable.morning to R.string.medication_plan_morning_text
        in 10..15 -> R.drawable.noon to R.string.medication_plan_noon_text
        in 15..17 -> R.drawable.afternoon to R.string.medication_plan_afternoon_text
        else -> R.drawable.evening to R.string.medication_plan_evening_text
    }
    return resources
}
