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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.pharmacy.mapper.toPrescriptionInOrder
import de.gematik.ti.erp.app.pharmacy.model.shippingContact
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.pharmacy.model.OrderStateErpModel
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionInOrderErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel.Companion.EmptyShippingInfoErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull

/**
 * Gets the activeProfile from the [profileRepository]. Then it gets redeemed (scanned and synced) tasks for this
 * profile from the [prescriptionRepository] and converts them into [PrescriptionInOrderErpModel].
 *
 * Now it checks the [shippingContactRepository] for a shipping contact, if not present gets it from
 * the [prescriptionRepository] and saves it into the [shippingContactRepository].
 * Finally it returns a [OrderStateErpModel] with the orders and shippingContact.
 */
class GetOrderStateUseCase(
    private val profileRepository: ProfileRepository,
    private val taskOperationsRepository: TaskOperationsRepository,
    private val shippingContactRepository: ShippingContactRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<OrderStateErpModel> =
        profileRepository.activeProfile().flatMapLatest { profile ->
            combine(
                shippingContactRepository.shippingContact(),
                getRedeemableSyncedTasks(profile.id),
                getRedeemableScannedTasks(profile.id)
            ) { contact, syncedTasks, scannedTasks ->
                val updatedContact = when {
                    syncedTasks.isNotEmpty() && contact == null -> syncedTasks.first().shippingContact()
                    else -> contact
                }
                val orders = syncedTasks.map(TaskErpModel.Synced.Prescription::toPrescriptionInOrder) +
                    scannedTasks.map(TaskErpModel.Scanned::toPrescriptionInOrder)
                val selfPayerPrescriptionIds = orders.filter { it.isSelfPayerPrescription }.map { it.taskId }
                val shippingContact = updatedContact ?: EmptyShippingInfoErpModel
                OrderStateErpModel(
                    prescriptionsInOrder = orders,
                    selfPayerPrescriptionIds = selfPayerPrescriptionIds,
                    contact = shippingContact
                )
            }
        }.flowOn(dispatcher)

    private fun getRedeemableSyncedTasks(id: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Prescription>> =
        taskOperationsRepository.loadSyncedTaskListByProfileId(id)
            .mapNotNull { tasks ->
                tasks.filter { it.redeemState().isRedeemable() }
                    .sortedByDescending { it.authoredOn }
            }.flowOn(dispatcher)

    private fun getRedeemableScannedTasks(id: ProfileIdentifier): Flow<List<TaskErpModel.Scanned>> =
        taskOperationsRepository.loadScannedTaskListByProfileId(id)
            .mapNotNull { tasks ->
                tasks.filter {
                    it.isRedeemable()
                    // TODO: (Check) Keeping this comment in-case we need the check for redeem enabled
                    // it.communications.isEmpty()
                }
                    .sortedByDescending { it.scannedOn }
            }.flowOn(dispatcher)
}
