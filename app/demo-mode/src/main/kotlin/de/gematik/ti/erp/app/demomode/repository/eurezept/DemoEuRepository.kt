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

package de.gematik.ti.erp.app.demomode.repository.eurezept

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.fhir.FhirCountryErpModel
import de.gematik.ti.erp.app.fhir.FhirCountryErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirErpModel
import de.gematik.ti.erp.app.fhir.euredeem.model.generateAccessCode
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.hours

class DemoEuRepository(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : EuRepository {

    override suspend fun fetchAvailableCountries(): Result<FhirErpModel?> {
        val countries = dataSource.euCountries.first()
        val fhirCountries = countries.map { country ->
            FhirCountryErpModel(
                name = country.name,
                code = country.code
            )
        }
        val fhirModel = FhirCountryErpModelCollection(countries = fhirCountries)
        return Result.success(fhirModel)
    }

    override suspend fun toggleIsEuRedeemableByPatientAuthorization(
        taskId: String,
        profileId: String,
        isEuRedeemableByPatientAuthorization: Boolean
    ): Result<Unit> =
        withContext(dispatcher) {
            val data = dataSource.syncedTasks.updateAndGet { syncedList ->
                val index = syncedList.indexOfFirst { it.taskId == taskId }
                val updatedList = syncedList
                if (index != INDEX_OUT_OF_BOUNDS) {
                    val updatedItem = syncedList[index].copy(
                        isEuRedeemableByPatientAuthorization = isEuRedeemableByPatientAuthorization,
                        lastModified = Clock.System.now()
                    )
                    updatedList[index] = updatedItem
                }
                updatedList
            }
            dataSource.syncedTasks.value = emptyList<SyncedTaskData.SyncedTask>().toMutableList()
            delay(1)
            dataSource.syncedTasks.value = data
            Result.success(Unit)
        }

    override suspend fun createEuRedeemAccessCode(profileId: ProfileIdentifier, countryCode: String, relatedTaskIds: List<String>): Result<EuAccessCode> {
        val now = Clock.System.now()
        return Result.success(
            EuAccessCode(
                accessCode = generateAccessCode(),
                countryCode = countryCode,
                validUntil = now.plus(1.hours),
                createdAt = now,
                profileIdentifier = profileId
            )
        )
    }

    override suspend fun getLatestValidEuAccessCodeByProfileIdAndCountry(profileId: ProfileIdentifier, countryCode: String): Flow<EuAccessCode?> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEuRedeemAccessCode(profileId: ProfileIdentifier) {
        // TODO EUREZEPT
    }
}
