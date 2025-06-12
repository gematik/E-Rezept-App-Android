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

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.diga.repository.DigaRepository
import de.gematik.ti.erp.app.fhir.model.DigaStatus
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

@Suppress("MagicNumber")
class ArchiveExpiredDigasUseCase(
    private val repository: DigaRepository
) {
    suspend operator fun invoke(prescriptions: List<Prescription>): List<Prescription> {
        val now = Clock.System.now()
        val threshold: Instant = now - 100.days

        return prescriptions.map { prescription ->
            if (prescription is Prescription.SyncedPrescription &&
                prescription.lastModified < threshold &&
                prescription.deviceRequestState != DigaStatus.SelfArchiveDiga &&
                prescription.isDiga
            ) {
                repository.updateArchiveStatus(
                    taskId = prescription.taskId,
                    lastModified = Clock.System.now(),
                    setArchiveStatus = true
                )
            }
            prescription
        }
    }
}
