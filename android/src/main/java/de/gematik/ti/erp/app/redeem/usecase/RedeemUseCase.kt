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

package de.gematik.ti.erp.app.redeem.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.Instant

class RedeemUseCase(
    private val prescriptionRepository: PrescriptionRepository,
    private val dispatchers: DispatchProvider
) {
    fun hasRedeemablePrescriptions(
        profileId: ProfileIdentifier
    ): Flow<Boolean> =
        combine(
            prescriptionRepository.syncedTasks(profileId).map { tasks ->
                tasks.filter {
                    it.redeemState().isRedeemable()
                }
            },
            prescriptionRepository.scannedTasks(profileId).map { tasks ->
                tasks.filter {
                    it.isRedeemable()
                }
            }
        ) { syncedTasks, scannedTasks ->
            syncedTasks.isNotEmpty() || scannedTasks.isNotEmpty()
        }.flowOn(dispatchers.IO)

    suspend fun redeemScannedTasks(taskIds: List<String>) {
        val redeemedOn = Instant.now()
        taskIds.forEach { taskId ->
            prescriptionRepository.updateRedeemedOn(taskId, redeemedOn)
        }
    }
}
