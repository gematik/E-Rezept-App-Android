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

package de.gematik.ti.erp.app.database.api.model

import kotlinx.datetime.Instant

/**
 * Result of persisting synced prescription medical data.
 *
 * - `isCompleted`: Whether the task is in a completed state after the save operation.
 * - `lastModified`: Timestamp of the last modification of the task as stored locally.
 * - `lastMedicationDispense`: Timestamp of the last recorded medication dispense if available.
 */
data class SaveTaskResult(
    val isCompleted: Boolean,
    val lastModified: Instant,
    val lastMedicationDispense: Instant? = null
)
