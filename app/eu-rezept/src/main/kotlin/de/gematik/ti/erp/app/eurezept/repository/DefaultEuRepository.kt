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

package de.gematik.ti.erp.app.eurezept.repository

import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrder
import de.gematik.ti.erp.app.eurezept.model.toModel
import de.gematik.ti.erp.app.fhir.FhirErpModel
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeRequestConstants.FhirEuRedeemAccessCodeRequestMeta
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirTaskEuPatchInputModelConstants.FhirTaskEuPatchMeta
import de.gematik.ti.erp.app.fhir.euredeem.model.createEuRedeemAccessCodePayload
import de.gematik.ti.erp.app.fhir.euredeem.model.createIsEuRedeemableByPatientAuthorizationPayload
import de.gematik.ti.erp.app.fhir.euredeem.model.generateAccessCode
import de.gematik.ti.erp.app.fhir.euredeem.parser.EuRedeemAccessCodeResponseParser
import de.gematik.ti.erp.app.fhir.pharmacy.parser.FhirVzdCountriesParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskMetadataParser
import de.gematik.ti.erp.app.navigation.toNavigationString
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.PharmacyRemoteDataSource
import de.gematik.ti.erp.app.prescription.repository.LegacyTaskLocalDataSource
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.utils.snapshot
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

class DefaultEuRepository(
    private val pharmacyRemoteDataSource: PharmacyRemoteDataSource,
    private val euTaskRemoteDataSource: EuTaskRemoteDataSource,
    private val euTaskLocalDataSource: EuTaskLocalDataSource,
    private val taskLocalDataSource: LegacyTaskLocalDataSource,
    private val parser: FhirVzdCountriesParser,
    private val metadataParser: TaskMetadataParser,
    private val euRedeemAccessCodeResponseParser: EuRedeemAccessCodeResponseParser
) : EuRepository {

    override suspend fun fetchAvailableCountries(): Result<FhirErpModel?> {
        return pharmacyRemoteDataSource.fetchAvailableCountries()
            .map(::parseData)
    }

    override fun observeEuOrder(orderId: String): Flow<EuOrder?> = euTaskLocalDataSource.observeEuOrder(orderId)

    override fun observeAllEuOrders(): Flow<List<EuOrder>> = euTaskLocalDataSource.observeAllEuOrders()

    override fun getEuAccessCode(accessCode: String): Flow<EuAccessCode?> = euTaskLocalDataSource.getEuAccessCode(accessCode)

    override suspend fun toggleIsEuRedeemableByPatientAuthorization(
        taskId: String,
        profileId: ProfileIdentifier,
        metadata: FhirTaskEuPatchMeta,
        isEuRedeemableByPatientAuthorization: Boolean
    ): Result<Unit> {
        // --- 1. Prepare payload ---
        val payload = createIsEuRedeemableByPatientAuthorizationPayload(
            isEuRedeemableByPatientAuthorization = isEuRedeemableByPatientAuthorization,
            meta = metadata
        )

        Napier.d("Payload: ${payload.toNavigationString()}")
        euTaskRemoteDataSource.toggleIsEuRedeemableByPatientAuthorization(
            profileIdentifier = profileId,
            taskId = taskId,
            payload = payload
        ).fold(
            onSuccess = {
                return metadataParser.extract(it)?.let { taskMetaData ->

                    val isAuthorized = taskMetaData.isEuRedeemableByPatientAuthorization
                    // adding the log that a task was added or removed to the latest order
                    euTaskLocalDataSource.addEventToValidOrders(
                        profileId = profileId,
                        taskIds = listOf(taskId),
                        eventType = if (isAuthorized) EuEventType.TASK_ADDED else EuEventType.TASK_REMOVED
                    )
                    taskLocalDataSource.saveTaskEPrescriptionMetaData(profileId, taskMetaData)
                    Result.success(Unit)
                } ?: Result.failure(IllegalStateException("Failed to parse meta task bundle for taskId: $taskId"))
            },
            onFailure = {
                return Result.failure(exception = it)
            }
        )
    }

    override suspend fun createEuRedeemAccessCode(
        profileId: ProfileIdentifier,
        metadata: FhirEuRedeemAccessCodeRequestMeta,
        countryCode: String,
        relatedTaskIds: List<String>
    ): Result<EuAccessCode> = runCatching {
        // --- 1. Prepare payload ---
        val payload = createEuRedeemAccessCodePayload(
            countryCode = countryCode,
            accessCode = generateAccessCode(),
            meta = metadata
        )

        // --- 2. Call remote API ---
        val response = euTaskRemoteDataSource
            .createEuRedeemAccessCode(profileId, payload)
            .getOrThrow()

        // --- 3. Parse access code from response ---
        val parsedAccessCode = euRedeemAccessCodeResponseParser.extract(response)
            ?: throw IllegalStateException(
                "Failed to parse access code bundle for country: $countryCode"
            )

        // --- 4. Build new EuOrder ---
        val newOrder = parsedAccessCode.toModel(
            profileId = profileId,
            relatedTaskIds = relatedTaskIds
        )

        // --- 5. Find an existing matching order (similar tasks + country + profile) ---
        val existingOrder = euTaskLocalDataSource
            .getOrdersForProfileCountryAndTasks(
                profileId = profileId,
                countryCode = countryCode,
                taskIds = relatedTaskIds
            ).snapshot()

        Napier.d("Existing order: $existingOrder")

        // --- 6. Update or create ---
        if (existingOrder != null) {
            val updatedExistingOrder = existingOrder.copyFrom(newOrder)

            euTaskLocalDataSource.saveEuOrder(
                euOrder = updatedExistingOrder,
                eventType = EuEventType.ACCESS_CODE_RECREATED
            )

            updatedExistingOrder.euAccessCode
                ?: throw IllegalStateException(
                    "Couldn't create access code for country: $countryCode"
                )
        } else {
            euTaskLocalDataSource.saveEuOrder(
                euOrder = newOrder,
                eventType = EuEventType.ACCESS_CODE_CREATED
            )

            newOrder.euAccessCode
                ?: throw IllegalStateException(
                    "Couldn't create access code for country: $countryCode"
                )
        }
    }

    private fun EuOrder.copyFrom(newOrder: EuOrder): EuOrder {
        return copy(
            createdAt = newOrder.createdAt,
            euAccessCode = newOrder.euAccessCode,
            relatedTaskIds = newOrder.relatedTaskIds
        )
    }

    override suspend fun getLatestValidEuAccessCodeByProfileIdAndCountry(
        profileId: ProfileIdentifier,
        countryCode: String
    ): Flow<EuAccessCode?> {
        return euTaskLocalDataSource.getLatestEuAccessCodeByProfileIdAndCountry(profileId = profileId, countryCode = countryCode)
    }

    override suspend fun deleteEuRedeemAccessCode(
        profileId: ProfileIdentifier,
        inProgress: () -> Unit,
        failed: (Throwable) -> Unit,
        completed: () -> Unit
    ) {
        inProgress()
        euTaskRemoteDataSource.deleteEuRedeemAccessCode(
            profileIdentifier = profileId
        ).fold(
            onSuccess = {
                euTaskLocalDataSource.deleteEuAccessCodeByProfileId(profileId)
                completed()
            },
            onFailure = {
                failed(it)
                Napier.e("Failed to delete access code for profileId: $profileId", it)
            }
        )
    }

    override suspend fun markEventsAsRead(eventIds: List<String>) {
        euTaskLocalDataSource.markEventsAsRead(eventIds)
    }

    private fun parseData(jsonElement: JsonElement): FhirErpModel? {
        return parser.extract(jsonElement)
    }
}
