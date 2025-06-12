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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.pharmacy.mapper.toOrder
import de.gematik.ti.erp.app.pharmacy.model.PharmacyData
import de.gematik.ti.erp.app.pharmacy.model.shippingContact
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.ShippingContact.Companion.EmptyShippingContact
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData.ScannedTask
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.SyncedTask
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
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
 * profile from the [prescriptionRepository] and converts them into [PharmacyUseCaseData.PrescriptionInOrder].
 *
 * Now it checks the [shippingContactRepository] for a shipping contact, if not present gets it from
 * the [prescriptionRepository] and saves it into the [shippingContactRepository].
 * Finally it returns a [PharmacyUseCaseData.OrderState] with the orders and shippingContact.
 */
class GetOrderStateUseCase(
    private val profileRepository: ProfileRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val shippingContactRepository: ShippingContactRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PharmacyUseCaseData.OrderState> =
        profileRepository.activeProfile().flatMapLatest { profile ->
            combine(
                shippingContactRepository.shippingContact(),
                getRedeemableSyncedTasks(profile.id),
                getRedeemableScannedTasks(profile.id)
            ) { contact, syncedTasks, scannedTasks ->
                val updatedContact = when {
                    syncedTasks.isNotEmpty() && contact == null ->
                        syncedTasks.first().shippingContact().updateShippingContactRepo()

                    else -> contact
                }
                val orders = syncedTasks.map { it.toOrder() } + scannedTasks.map { it.toOrder() }
                val selfPayerPrescriptionIds = orders.filter { it.isSelfPayerPrescription }.map { it.taskId }
                val shippingContact = updatedContact?.toModel() ?: EmptyShippingContact
                PharmacyUseCaseData.OrderState(
                    prescriptionsInOrder = orders,
                    selfPayerPrescriptionIds = selfPayerPrescriptionIds,
                    contact = shippingContact
                )
            }
        }.flowOn(dispatcher)

    private suspend fun PharmacyData.ShippingContact.updateShippingContactRepo():
        PharmacyData.ShippingContact {
        shippingContactRepository.saveShippingContact(this)
        return this
    }

    private fun getRedeemableSyncedTasks(id: ProfileIdentifier): Flow<List<SyncedTask>> =
        prescriptionRepository.syncedTasks(id)
            .mapNotNull { tasks ->
                tasks.filter { it.redeemState().isRedeemable() && it.deviceRequest == null } // TODO: define as a type
                    .sortedByDescending { it.authoredOn }
            }.flowOn(dispatcher)

    private fun getRedeemableScannedTasks(id: ProfileIdentifier): Flow<List<ScannedTask>> =
        prescriptionRepository.scannedTasks(id)
            .mapNotNull { tasks ->
                tasks.filter {
                    it.isRedeemable()
                    // TODO: (Check) Keeping this comment in-case we need the check for redeem enabled
                    // it.communications.isEmpty()
                }
                    .sortedByDescending { it.scannedOn }
            }.flowOn(dispatcher)
}
