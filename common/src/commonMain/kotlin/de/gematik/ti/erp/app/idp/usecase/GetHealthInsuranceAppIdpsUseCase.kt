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

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.api.models.IdpInitialData
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData
import de.gematik.ti.erp.app.idp.model.HealthInsuranceData.Companion.mapToDomain
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.vau.extractECPublicKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetHealthInsuranceAppIdpsUseCase(
    private val repository: IdpRepository,
    // TODO Usecase in a usecase is wrong
    private val basicUseCase: IdpBasicUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend operator fun invoke(): List<HealthInsuranceData> =
        withContext(dispatcher) {
            val initialData = basicUseCase.initializeConfigurationAndKeys()
            loadFederationAppList(initialData)
        }

    @Requirement(
        "A_23082#1",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Load list of external authenticators for Gesundheit ID."
    )
    /**
     * @param initialData provides the federationAuthorizationIDsEndpoint and
     * the idpPukSigKey which are required to obtain the response
     * @return A filtered [HealthInsuranceData] list that has "idp_sek_2" as true (or) "isGid" as true
     *
     */
    private suspend fun loadFederationAppList(initialData: IdpInitialData): List<HealthInsuranceData> =
        try {
            repository.fetchFederationIDList(
                url = requireNotNull(initialData.config.federationAuthorizationIDsEndpoint) { "GiD is not available" },
                idpPukSigKey = initialData.config.certificate.extractECPublicKey()
            ).map { it.mapToDomain() }
                .sortedWith(compareBy { it.name.lowercase() })
                .filter { it.isGid }
        } catch (e: Throwable) {
            throw GematikResponseError.emptyResponseError(e.message)
        }
}
