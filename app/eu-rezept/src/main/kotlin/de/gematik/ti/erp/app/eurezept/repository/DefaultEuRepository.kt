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
import de.gematik.ti.erp.app.eurezept.model.toEuOrder
import de.gematik.ti.erp.app.fhir.FhirErpModel
import de.gematik.ti.erp.app.fhir.euredeem.model.createEuRedeemAccessCodePayload
import de.gematik.ti.erp.app.fhir.euredeem.model.createIsEuRedeemableByPatientAuthorizationPayload
import de.gematik.ti.erp.app.fhir.euredeem.model.generateAccessCode
import de.gematik.ti.erp.app.fhir.euredeem.parser.EuRedeemAccessCodeResponseParser
import de.gematik.ti.erp.app.fhir.pharmacy.parser.FhirVzdCountriesParser
import de.gematik.ti.erp.app.fhir.prescription.parser.TaskMetadataParser
import de.gematik.ti.erp.app.pharmacy.repository.datasource.remote.FhirVzdRemoteDataSource
import de.gematik.ti.erp.app.prescription.repository.LegacyTaskLocalDataSource
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

class DefaultEuRepository(
    private val fhirVzdRemoteDataSource: FhirVzdRemoteDataSource,
    private val euTaskRemoteDataSource: EuTaskRemoteDataSource,
    private val euTaskLocalDataSource: EuTaskLocalDataSource,
    private val taskLocalDataSource: LegacyTaskLocalDataSource,
    private val parser: FhirVzdCountriesParser,
    private val metadataParser: TaskMetadataParser,
    private val euRedeemAccessCodeResponseParser: EuRedeemAccessCodeResponseParser
) : EuRepository {

    override suspend fun fetchAvailableCountries(): Result<FhirErpModel?> {
        return fhirVzdRemoteDataSource.fetchAvailableCountries()
            .map(::parseData)
    }

    override suspend fun toggleIsEuRedeemableByPatientAuthorization(
        taskId: String,
        profileId: ProfileIdentifier,
        isEuRedeemableByPatientAuthorization: Boolean
    ): Result<Unit> {
        val payload = createIsEuRedeemableByPatientAuthorizationPayload(isEuRedeemableByPatientAuthorization = isEuRedeemableByPatientAuthorization)
        euTaskRemoteDataSource.toggleIsEuRedeemableByPatientAuthorization(
            profileIdentifier = profileId,
            taskId = taskId,
            payload = payload
        ).fold(
            onSuccess = {
                val taskMetaData = metadataParser.extract(it)
                taskMetaData?.let { metaData ->
                    taskLocalDataSource.saveTaskEPrescriptionMetaData(profileId, metaData)
                    return Result.success(Unit)
                }
                    ?: return Result.failure(IllegalStateException("Failed to parse meta task bundle for taskId: $taskId"))
            },
            onFailure = {
                return Result.failure(exception = it)
            }
        )
    }

    override suspend fun createEuRedeemAccessCode(
        profileId: ProfileIdentifier,
        countryCode: String,
        relatedTaskIds: List<String>
    ): Result<EuAccessCode> {
        val payload = createEuRedeemAccessCodePayload(
            countryCode = countryCode,
            accessCode = generateAccessCode()
        )
        euTaskRemoteDataSource.createEuRedeemAccessCode(
            profileIdentifier = profileId,
            authorizationRequest = payload
        ).fold(
            onSuccess = {
                return euRedeemAccessCodeResponseParser.extract(it)?.let { accessCode ->
                    val euOrder = accessCode.toEuOrder(
                        profileId = profileId,
                        relatedTaskIds = relatedTaskIds
                    )
                    euTaskLocalDataSource.saveEuOrder(euOrder = euOrder)
                    euOrder.euAccessCode?.let { Result.success(it) }
                        ?: Result.failure(IllegalStateException("Couldn't create access code for country: $countryCode"))
                }
                    ?: return Result.failure(IllegalStateException("Failed to parse access Code bundle for country: $countryCode"))
            },
            onFailure = {
                return Result.failure(exception = it)
            }
        )
    }

    override suspend fun getLatestValidEuAccessCodeByProfileIdAndCountry(
        profileId: ProfileIdentifier,
        countryCode: String
    ): Flow<EuAccessCode?> {
        return euTaskLocalDataSource.getLatestEuAccessCodeByProfileIdAndCountry(profileId = profileId, countryCode = countryCode)
    }

    override suspend fun deleteEuRedeemAccessCode(profileId: ProfileIdentifier) {
        euTaskRemoteDataSource.deleteEuRedeemAccessCode(
            profileIdentifier = profileId
        )
    }

    private fun parseData(jsonElement: JsonElement): FhirErpModel? {
        return parser.extract(jsonElement)
    }
}
