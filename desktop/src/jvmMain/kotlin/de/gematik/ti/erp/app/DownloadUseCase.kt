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

package de.gematik.ti.erp.app

import de.gematik.ti.erp.app.communication.repository.DesktopCommunicationRepository
import de.gematik.ti.erp.app.prescription.repository.DesktopPrescriptionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

class DownloadUseCase(
    private val prescriptionRepository: DesktopPrescriptionRepository,
    private val communicationRepository: DesktopCommunicationRepository
) {
    suspend fun update() =
        supervisorScope {
            awaitAll(
                async {
                    prescriptionRepository.download()
                },
                async {
                    communicationRepository.download()
                }
            ).find { it.isFailure } ?: Result.success(Unit)
        }.onFailure {
            prescriptionRepository.invalidate()
            communicationRepository.invalidate()
        }
}
