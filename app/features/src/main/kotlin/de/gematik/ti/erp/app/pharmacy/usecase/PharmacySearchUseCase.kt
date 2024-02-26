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

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.model.CommunicationPayload
import de.gematik.ti.erp.app.fhir.model.createCommunicationDispenseRequest
import de.gematik.ti.erp.app.pharmacy.model.PharmacyData
import de.gematik.ti.erp.app.pharmacy.model.shippingContact
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.PharmacyInitialResultsPerPage
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.PharmacyNextResultsPerPage
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlin.math.max

// can't be modified; the backend will always return 80 entries on the first page
const val PharmacyInitialResultsPerPage = 80
const val PharmacyNextResultsPerPage = 10

class PharmacySearchUseCase(
    private val repository: PharmacyRepository,
    private val shippingContactRepository: ShippingContactRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: DispatchProvider
) {
    data class PharmacyPagingKey(val bundleId: String, val offset: Int)

    inner class PharmacyPagingSource(searchData: PharmacyUseCaseData.SearchData) :
        PagingSource<PharmacyPagingKey, PharmacyUseCaseData.Pharmacy>() {

        private val name = searchData.name.split(" ").filter { it.isNotEmpty() }
        private val locationMode = searchData.locationMode
        private val filter = run {
            val filterMap = mutableMapOf<String, String>()
            if (locationMode is PharmacyUseCaseData.LocationMode.Enabled) {
                filterMap += "near" to "${locationMode.location.latitude}|${locationMode.location.longitude}|999|km"
            }
            if (searchData.filter.directRedeem) {
                filterMap += "type" to "DELEGATOR"
            }
            if (searchData.filter.onlineService) {
                filterMap += "type" to "mobl"
            }
            filterMap
        }

        override fun getRefreshKey(
            state: PagingState<PharmacyPagingKey, PharmacyUseCaseData.Pharmacy>
        ): PharmacyPagingKey? = null

        override suspend fun load(
            params: LoadParams<PharmacyPagingKey>
        ): LoadResult<PharmacyPagingKey, PharmacyUseCaseData.Pharmacy> {
            val count = params.loadSize

            when (params) {
                is LoadParams.Refresh -> {
                    return repository.searchPharmacies(name, filter)
                        .map {
                            LoadResult.Page(
                                data = it.pharmacies.toModel(),
                                nextKey = if (it.bundleResultCount == PharmacyInitialResultsPerPage) {
                                    PharmacyPagingKey(
                                        it.bundleId,
                                        it.bundleResultCount
                                    )
                                } else {
                                    null
                                },
                                prevKey = null
                            )
                        }.getOrElse { LoadResult.Error(it) }
                }
                is LoadParams.Append, is LoadParams.Prepend -> {
                    val key = params.key!!

                    return repository.searchPharmaciesByBundle(key.bundleId, offset = key.offset, count = count).map {
                        val nextKey = if (it.bundleResultCount == count) {
                            PharmacyPagingKey(
                                key.bundleId,
                                key.offset + it.bundleResultCount
                            )
                        } else {
                            null
                        }
                        val prevKey = if (key.offset == 0) null else key.copy(offset = max(0, key.offset - count))

                        LoadResult.Page(
                            data = it.pharmacies.toModel(),
                            nextKey = nextKey,
                            prevKey = prevKey,
                            itemsBefore = if (prevKey != null) count else 0,
                            itemsAfter = if (nextKey != null) count else 0
                        )
                    }.getOrElse { LoadResult.Error(it) }
                }
            }
        }
    }

    @Requirement(
        "A_20182",
        "A_20183",
        "A_20208",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Search results are only based on search term and filter criteria set by the user."
    )
    suspend fun searchPharmacies(
        searchData: PharmacyUseCaseData.SearchData
    ): Flow<PagingData<PharmacyUseCaseData.Pharmacy>> {
        settingsRepository.savePharmacySearch(
            SettingsData.PharmacySearch(
                name = searchData.name,
                locationEnabled = searchData.locationMode !is PharmacyUseCaseData.LocationMode.Disabled,
                deliveryService = searchData.filter.deliveryService,
                onlineService = searchData.filter.onlineService,
                openNow = searchData.filter.openNow
            )
        )

        return Pager(
            PagingConfig(
                pageSize = PharmacyNextResultsPerPage,
                initialLoadSize = PharmacyInitialResultsPerPage,
                maxSize = PharmacyInitialResultsPerPage * 2
            ),
            pagingSourceFactory = { PharmacyPagingSource(searchData) }
        ).flow.flowOn(dispatchers.io)
    }

    fun prescriptionDetailsForOrdering(
        profileId: ProfileIdentifier
    ): Flow<PharmacyUseCaseData.OrderState> =
        combine(
            shippingContactRepository.shippingContact(),
            prescriptionRepository.syncedTasks(profileId).map { tasks ->
                tasks.filter {
                    it.redeemState().isRedeemable()
                }
            },
            prescriptionRepository.scannedTasks(profileId).map { tasks ->
                tasks.filter {
                    it.isRedeemable()
                    it.communications.isEmpty()
                }
            }
        ) { shippingContacts, syncedTasks, scannedTasks ->

            val shippingContact = if (syncedTasks.isNotEmpty()) {
                shippingContacts ?: run {
                    syncedTasks.first().shippingContact().also {
                        shippingContactRepository.saveShippingContact(it)
                    }
                }
            } else {
                shippingContacts
            }

            val tasks = scannedTasks.map { task ->
                PharmacyUseCaseData.PrescriptionOrder(
                    taskId = task.taskId,
                    accessCode = task.accessCode,
                    title = task.name,
                    index = task.index,
                    timestamp = task.scannedOn,
                    substitutionsAllowed = false
                )
            } + syncedTasks.map { task ->
                PharmacyUseCaseData.PrescriptionOrder(
                    taskId = task.taskId,
                    accessCode = task.accessCode!!, // TODO: check, why we get here a nullable!!
                    title = task.medicationName(),
                    index = null,
                    timestamp = task.authoredOn,
                    substitutionsAllowed = false
                )
            }

            PharmacyUseCaseData.OrderState(
                tasks,
                PharmacyUseCaseData.ShippingContact(
                    name = shippingContact?.name ?: "",
                    line1 = shippingContact?.line1 ?: "",
                    line2 = shippingContact?.line2 ?: "",
                    postalCode = shippingContact?.postalCode ?: "",
                    city = shippingContact?.city ?: "",
                    telephoneNumber = shippingContact?.telephoneNumber ?: "",
                    mail = shippingContact?.mail ?: "",
                    deliveryInformation = shippingContact?.deliveryInformation ?: ""
                )
            )
        }.flowOn(Dispatchers.Default)

    suspend fun saveShippingContact(contact: PharmacyUseCaseData.ShippingContact) {
        shippingContactRepository.saveShippingContact(
            mapShippingContact(contact)
        )
    }

    suspend fun redeemPrescription(
        profileId: ProfileIdentifier,
        redeemOption: RemoteRedeemOption,
        orderId: UUID,
        order: PharmacyUseCaseData.PrescriptionOrder,
        contact: PharmacyUseCaseData.ShippingContact,
        pharmacyTelematikId: String
    ): Result<Unit> {
        val comDisp = createCommunicationDispenseRequest(
            orderId = orderId.toString(),
            taskId = order.taskId,
            accessCode = order.accessCode,
            recipientTID = pharmacyTelematikId,
            payload = CommunicationPayload(
                version = 1,
                supplyOptionsType = redeemOption.type,
                name = contact.name,
                address = listOf(contact.line1, contact.line2, contact.postalCode, contact.city),
                phone = contact.telephoneNumber,
                hint = contact.deliveryInformation
            )
        )

        return prescriptionRepository.redeemPrescription(profileId, comDisp, accessCode = order.accessCode)
    }

    private fun mapShippingContact(contact: PharmacyUseCaseData.ShippingContact) =
        PharmacyData.ShippingContact(
            name = contact.name.trim(),
            line1 = contact.line1.trim(),
            line2 = contact.line2.trim(),
            postalCode = contact.postalCode.trim(),
            city = contact.city.trim(),
            telephoneNumber = contact.telephoneNumber.trim(),
            mail = contact.mail.trim(),
            deliveryInformation = contact.deliveryInformation.trim()
        )
}
