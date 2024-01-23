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
 * profile from the [prescriptionRepository] and converts them into [PharmacyUseCaseData.PrescriptionOrder].
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
                getRedeemedSyncedTasks(profile.id),
                getRedeemedScannedTasks(profile.id)
            ) { contact, syncedTasks, scannedTasks ->
                val updatedContact = when {
                    syncedTasks.isNotEmpty() && contact == null ->
                        syncedTasks.first().shippingContact().updateShippingContactRepo()

                    else -> contact
                }
                val orders = syncedTasks.map { it.toOrder() } + scannedTasks.map { it.toOrder() }
                val shippingContact = updatedContact?.toModel() ?: EmptyShippingContact
                PharmacyUseCaseData.OrderState(
                    orders = orders,
                    contact = shippingContact
                )
            }.flowOn(dispatcher)
        }.flowOn(dispatcher)

    private suspend fun PharmacyData.ShippingContact.updateShippingContactRepo():
        PharmacyData.ShippingContact {
        shippingContactRepository.saveShippingContact(this)
        return this
    }

    private fun getRedeemedSyncedTasks(id: ProfileIdentifier): Flow<List<SyncedTask>> =
        prescriptionRepository.syncedTasks(id)
            .mapNotNull { tasks ->
                tasks.filter { it.redeemState().isRedeemable() }
            }.flowOn(dispatcher)

    private fun getRedeemedScannedTasks(id: ProfileIdentifier): Flow<List<ScannedTask>> =
        prescriptionRepository.scannedTasks(id)
            .mapNotNull { tasks ->
                tasks.filter {
                    it.isRedeemable()
                    it.communications.isEmpty()
                }
            }.flowOn(dispatcher)
}
