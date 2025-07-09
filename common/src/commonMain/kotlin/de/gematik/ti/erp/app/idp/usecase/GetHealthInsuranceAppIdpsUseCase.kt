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
        rationale = """
            Load and validate the list of external federation authenticators (health insurance providers) 
            for Gesundheit ID integration. This retrieves the authorized identity providers that users can 
            select for authentication, ensuring only GID-compliant health insurance data is presented. 
            The list is fetched from the federation authorization endpoint, verified using the IDP public 
            signature key, and filtered to maintain security and compliance standards.
        """
    )
    /**
     * Loads the federation application list for Gesundheit ID authentication.
     *
     * This function retrieves a list of external authenticators (health insurance providers)
     * that are authorized for use with the Gesundheit ID federation system. The list is
     * fetched from the federation authorization endpoint, validated using cryptographic
     * verification, and filtered to ensure GID compliance.
     *
     * @param initialData provides the federationAuthorizationIDsEndpoint and
     * the idpPukSigKey which are required to obtain and verify the response
     * @return A filtered and sorted [HealthInsuranceData] list that is GID compliant,
     * ordered alphabetically by insurance provider name.
     * Based on https://gemspec.gematik.de/docs/gemSpec/gemSpec_IDP_Dienst/latest/#A_23681-02
     *
     * @throws GematikResponseError.emptyResponseError if the federation endpoint is unavailable
     * or if the response cannot be validated
     */
    private suspend fun loadFederationAppList(initialData: IdpInitialData): List<HealthInsuranceData> =
        try {
            repository.fetchFederationIDList(
                url = requireNotNull(initialData.config.federationAuthorizationIDsEndpoint) { "GiD is not available" },
                idpPukSigKey = initialData.config.certificate.extractECPublicKey()
            ).map { it.mapToDomain() }
                .sortedWith(compareBy { it.name.lowercase() })
        } catch (e: Throwable) {
            throw GematikResponseError.emptyResponseError(e.message)
        }
}
