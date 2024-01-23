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

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.model.UniversalLinkIdp
import de.gematik.ti.erp.app.idp.model.error.UniversalLinkError
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

class GetUniversalLinkForHealthInsuranceAppsUseCase(
    private val idpBasicUseCase: IdpBasicUseCase,
    private val preferences: IdpPreferenceProvider,
    private val repository: IdpRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        universalLinkIdp: UniversalLinkIdp,
        idpScope: IdpScope = IdpScope.Default
    ): Result<URI> =
        try {
            withContext(dispatcher) {
                val initialData = idpBasicUseCase.initializeConfigurationAndKeys()

                val authenticationUrl = requireNotNull(initialData.config.federationAuthorizationEndpoint) {
                    "authentication URL is required for universal link"
                }

                val authenticatorId = universalLinkIdp.authenticatorId
                val authenticatorName = universalLinkIdp.authenticatorName
                val profileId = universalLinkIdp.profileId

                repository.getGidAuthorizationRedirect(
                    url = authenticationUrl,
                    state = initialData.state,
                    codeChallenge = initialData.codeChallenge,
                    nonce = initialData.nonce,
                    externalAppId = universalLinkIdp.authenticatorId,
                    idpScope = idpScope
                )
                    .fold(
                        onSuccess = { parsedUri ->

                            val authenticationState = initialData.state.state

                            preferences.externalAuthenticationPreferences =
                                ExternalAuthenticationPreferences(
                                    extAuthCodeChallenge = initialData.codeChallenge,
                                    extAuthCodeVerifier = initialData.codeVerifier,
                                    extAuthState = authenticationState,
                                    extAuthNonce = initialData.nonce.nonce,
                                    extAuthId = authenticatorId,
                                    extAuthScope = idpScope.name,
                                    extAuthName = authenticatorName,
                                    extAuthProfile = profileId
                                )

                            Result.success(parsedUri)
                        },
                        onFailure = {
                            Result.failure(it)
                        }
                    )
            }
        } catch (e: Throwable) {
            Napier.e { "Exception on GetUniversalLinkForHealthInsuranceAppsUseCase ${e.message}" }
            Result.failure(UniversalLinkError(e.message))
        }
}
