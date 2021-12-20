/*
 * Copyright (c) 2021 gematik GmbH
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
import com.squareup.moshi.Moshi
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.repository.model.CommunicationPayload
import de.gematik.ti.erp.app.pharmacy.repository.model.Pharmacy
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.UIPrescriptionOrder
import de.gematik.ti.erp.app.prescription.detail.ui.model.mapToUIPrescriptionOrder
import de.gematik.ti.erp.app.prescription.repository.Mapper
import de.gematik.ti.erp.app.prescription.repository.PROFILE
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.prescription.repository.extractMedication
import de.gematik.ti.erp.app.prescription.repository.extractMedicationRequest
import de.gematik.ti.erp.app.prescription.repository.extractPatient
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Meta
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.StringType
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.flow.first

private const val initialResultsPerPage = 80

class PharmacySearchUseCase @Inject constructor(
    private val repository: PharmacyRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val settingsUseCase: SettingsUseCase,
    private val mapper: Mapper,
    private val moshi: Moshi,
    private val dispatchProvider: DispatchProvider,
    private val profilesUseCase: ProfilesUseCase
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
            if (searchData.filter.ready) {
                filterMap += "status" to "active"
            }
            if (searchData.filter.onlineService) {
                filterMap += "type" to "mobl"
            }
            filterMap
        }

        override fun getRefreshKey(state: PagingState<PharmacyPagingKey, PharmacyUseCaseData.Pharmacy>): PharmacyPagingKey? =
            null

        override suspend fun load(params: LoadParams<PharmacyPagingKey>): LoadResult<PharmacyPagingKey, PharmacyUseCaseData.Pharmacy> {
            val count = params.loadSize

            when (params) {
                is LoadParams.Refresh -> {
                    return when (val resultSearchBundle = repository.searchPharmacies(name, filter)) {
                        is Result.Error -> LoadResult.Error(
                            resultSearchBundle.exception
                        )
                        is Result.Success -> {
                            LoadResult.Page(
                                data = mapPharmacies(resultSearchBundle.data.pharmacies),
                                nextKey = if (resultSearchBundle.data.bundleResultCount == initialResultsPerPage) {
                                    PharmacyPagingKey(
                                        resultSearchBundle.data.bundleId,
                                        resultSearchBundle.data.bundleResultCount
                                    )
                                } else {
                                    null
                                },
                                prevKey = null
                            )
                        }
                    }
                }
                is LoadParams.Append, is LoadParams.Prepend -> {
                    val key = params.key!!

                    val resultSearchBundle =
                        repository.searchPharmaciesByBundle(key.bundleId, offset = key.offset, count = count)

                    return when (resultSearchBundle) {
                        is Result.Error -> LoadResult.Error(
                            resultSearchBundle.exception
                        )
                        is Result.Success -> {
                            val nextKey = if (resultSearchBundle.data.bundleResultCount == count) {
                                PharmacyPagingKey(
                                    key.bundleId,
                                    key.offset + resultSearchBundle.data.bundleResultCount
                                )
                            } else {
                                null
                            }
                            val prevKey = if (key.offset == 0) null else key.copy(offset = max(0, key.offset - count))

                            LoadResult.Page(
                                data = mapPharmacies(resultSearchBundle.data.pharmacies),
                                nextKey = nextKey,
                                prevKey = prevKey,
                                itemsBefore = if (prevKey != null) count else 0,
                                itemsAfter = if (nextKey != null) count else 0,
                            )
                        }
                    }
                }
            }
        }
    }

    private val comAdapter by lazy {
        moshi.adapter(CommunicationPayload::class.java)
    }

    val previousSearch: Flow<PharmacyUseCaseData.SearchData> =
        settingsUseCase.pharmacySearch.map { pharmacySearchModel ->
            pharmacySearchModel.let {
                PharmacyUseCaseData.SearchData(
                    name = it.name,
                    filter = PharmacyUseCaseData.Filter(
                        ready = it.filterReady,
                        openNow = it.filterOpenNow,
                        deliveryService = it.filterDeliveryService,
                        onlineService = it.filterOnlineService,
                    ),
                    locationMode = if (it.locationEnabled) PharmacyUseCaseData.LocationMode.EnabledWithoutPosition else PharmacyUseCaseData.LocationMode.Disabled
                )
            }
        }.flowOn(dispatchProvider.io())

    suspend fun searchPharmacies(
        searchData: PharmacyUseCaseData.SearchData
    ): Flow<PagingData<PharmacyUseCaseData.Pharmacy>> {
        settingsUseCase.savePharmacySearch(
            name = searchData.name,
            locationEnabled = searchData.locationMode !is PharmacyUseCaseData.LocationMode.Disabled,
            filterReady = searchData.filter.ready,
            filterDeliveryService = searchData.filter.deliveryService,
            filterOnlineService = searchData.filter.onlineService,
            filterOpenNow = searchData.filter.openNow
        )

        return Pager(
            PagingConfig(
                pageSize = 10,
                initialLoadSize = initialResultsPerPage,
                maxSize = initialResultsPerPage * 2
            ),
            pagingSourceFactory = { PharmacyPagingSource(searchData) }
        ).flow
    }

    private suspend fun mapPharmacies(pharmacies: List<Pharmacy>): List<PharmacyUseCaseData.Pharmacy> =
        withContext(dispatchProvider.unconfined()) {
            pharmacies.map { pharmacy ->
                PharmacyUseCaseData.Pharmacy(
                    name = pharmacy.name,
                    address = pharmacy.address.let {
                        "${it.lines.joinToString()}\n${it.postalCode} ${it.city}"
                    },
                    location = pharmacy.location,
                    distance = null,
                    contacts = pharmacy.contacts,
                    provides = pharmacy.provides,
                    openingHours = pharmacy.provides.first().openingHours,
                    telematikId = pharmacy.telematikId,
                    roleCode = pharmacy.roleCode,
                    ready = pharmacy.ready
                )
            }
        }

    fun prescriptionDetailsForOrdering(
        vararg taskIds: String
    ): Flow<List<UIPrescriptionOrder>> {
        return prescriptionRepository.loadTasksForTaskId(*taskIds).take(1).map { taskList ->
            taskList.map { task ->
                val bundle = mapper.parseKBVBundle(requireNotNull(task.rawKBVBundle))
                mapToUIPrescriptionOrder(
                    task,
                    requireNotNull(bundle.extractMedication()),
                    requireNotNull(bundle.extractMedicationRequest()),
                    requireNotNull(bundle.extractPatient()),
                )
            }
        }.flowOn(Dispatchers.Default)
    }

    suspend fun redeemPrescription(
        redeemOption: RemoteRedeemOption,
        order: UIPrescriptionOrder,
        pharmacyTelematikId: String
    ): Result<ResponseBody> {
        val profileName = profilesUseCase.activeProfileName().first()
        val payload = generatePayLoad(redeemOption, order.patientName, order.address)
        val reference = assembleReference(order.taskId, order.accessCode)
        val communication = generateFhirObject(reference, pharmacyTelematikId, payload)
        return prescriptionRepository.redeemPrescription(profileName, communication)
    }

    private fun generatePayLoad(
        redeemOption: RemoteRedeemOption,
        patientName: String,
        address: String
    ): String {
        val com = CommunicationPayload(
            version = "1",
            supplyOptionsType = redeemOption.type,
            name = patientName,
            address = address.split(",").toTypedArray(),
            phone = null
        )
        return comAdapter.toJson(com)
    }

    private fun generateFhirObject(reference: String, telematicsId: String, payload: String) =
        Communication().apply {
            meta = Meta().addProfile(PROFILE)
            addBasedOn(Reference(reference))
            addPayload(
                Communication.CommunicationPayloadComponent().apply {
                    content = StringType(payload)
                }
            )
            status = Communication.CommunicationStatus.UNKNOWN
            addRecipient(Reference().setIdentifier(createIdentifier(telematicsId)))
        }

    private fun createIdentifier(pharmacyTelematikId: String): Identifier {
        val identifier = Identifier()
        identifier.system = "https://gematik.de/fhir/NamingSystem/TelematikID"
        identifier.value = pharmacyTelematikId
        return identifier
    }

    private fun assembleReference(taskId: String, accessCode: String): String {
        return "Task/$taskId\$accept?ac=$accessCode"
    }
}
