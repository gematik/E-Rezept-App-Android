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

package de.gematik.ti.erp.app.eurezept.domain.usecase

import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescription
import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescriptionType
import de.gematik.ti.erp.app.eurezept.domain.model.PrescriptionFilter
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class GetEuPrescriptionsUseCase(
    private val prescriptionRepository: PrescriptionRepository,
    private val profileRepository: ProfileRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke(filter: PrescriptionFilter = PrescriptionFilter.ALL): Flow<List<EuPrescription>> =
        profileRepository.activeProfile().flatMapLatest { activeProfile ->
            when (filter) {
                PrescriptionFilter.ALL -> getAllActivePrescriptions(activeProfile.id)
                PrescriptionFilter.EU_REDEEMABLE_ONLY -> getAvailableEURedeemablePrescriptions(activeProfile.id)
            }
        }.flowOn(dispatcher)

    private fun getAllActivePrescriptions(profileId: String): Flow<List<EuPrescription>> =
        combine(
            prescriptionRepository.syncedTasks(profileId),
            prescriptionRepository.scannedTasks(profileId)
        ) { syncedTasks, scannedTasks ->
            val syncedEuPrescriptions = syncedTasks.filter { it.isReady() }.map { it.toEuPrescription() }
            val scannedEuPrescriptions = scannedTasks.filter { it.isRedeemable() }.map { it.toEuPrescription() }
            val allPrescriptions = syncedEuPrescriptions + scannedEuPrescriptions

            allPrescriptions.sortedWith(
                compareByDescending {
                    it.type == EuPrescriptionType.EuRedeemable
                }
            )
        }

    private fun getAvailableEURedeemablePrescriptions(profileId: String): Flow<List<EuPrescription>> =
        getAllActivePrescriptions(profileId).map { allPrescriptions ->
            allPrescriptions.filter { prescription ->
                prescription.type == EuPrescriptionType.EuRedeemable
            }
        }

    private fun SyncedTaskData.SyncedTask.toEuPrescription(): EuPrescription {
        return EuPrescription(
            profileIdentifier = this.profileId,
            id = this.taskId,
            name = this.medicationName() ?: this.deviceRequest?.appName ?: "Unknown Medication",
            type = when {
                this.isEuRedeemable -> EuPrescriptionType.EuRedeemable
                this.medicationRequest.medication?.medicationProfile?.type == ErpMedicationProfileType.FreeText -> EuPrescriptionType.FreeText
                this.medicationRequest.medication?.medicationProfile?.type == ErpMedicationProfileType.Ingredient -> EuPrescriptionType.Ingredient
                this.medicationRequest.medication?.category == SyncedTaskData.MedicationCategory.BTM -> EuPrescriptionType.BTM
                else -> EuPrescriptionType.Unknown
            },
            isMarkedAsEuRedeemableByPatientAuthorization = isEuRedeemableByPatientAuthorization,
            expiryDate = this.expiresOn
        )
    }

    private fun ScannedTaskData.ScannedTask.toEuPrescription(): EuPrescription {
        return EuPrescription(
            profileIdentifier = this.profileId,
            id = this.taskId,
            name = this.name,
            isMarkedAsEuRedeemableByPatientAuthorization = false,
            type = EuPrescriptionType.Scanned,
            expiryDate = null
        )
    }
}
