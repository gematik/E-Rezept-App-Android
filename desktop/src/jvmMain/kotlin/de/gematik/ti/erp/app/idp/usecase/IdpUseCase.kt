/*
 * Copyright (c) 2022 gematik GmbH
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

import de.gematik.ti.erp.app.core.ApiCallException
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import io.github.aakira.napier.Napier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

/**
 * Exception thrown by [IdpUseCase.loadAccessToken].
 */
class RefreshFlowException : IOException {
    /**
     * Is true if the sso token is not valid anymore and the user is required to authenticate again.
     */
    val userActionRequired: Boolean
    val tokenScope: SingleSignOnToken.Scope?

    constructor(userActionRequired: Boolean, tokenScope: SingleSignOnToken.Scope?, cause: Throwable) : super(cause) {
        this.userActionRequired = userActionRequired
        this.tokenScope = tokenScope
    }

    constructor(userActionRequired: Boolean, tokenScope: SingleSignOnToken.Scope?, message: String) : super(message) {
        this.userActionRequired = userActionRequired
        this.tokenScope = tokenScope
    }
}

class AltAuthenticationCryptoException(cause: Throwable) : IllegalStateException(cause)

class IdpUseCase(
    private val repository: IdpRepository,
    private val basicUseCase: IdpBasicUseCase,
) {
    private val lock = Mutex()

    /**
     * If no bearer token is set or [refresh] is true, this will trigger [IdpBasicUseCase.refreshAccessTokenWithSsoFlow].
     */
    suspend fun loadAccessToken(refresh: Boolean = false): String =
        lock.withLock {
            val ssoToken = repository.getSingleSignOnToken()
            if (ssoToken == null) {
                repository.invalidateDecryptedAccessToken()
                throw RefreshFlowException(
                    true,
                    repository.getSingleSignOnTokenScope(),
                    "SSO token not set!"
                )
            }
            val accToken = repository.decryptedAccessToken

            if (refresh || accToken == null) {
                repository.invalidateDecryptedAccessToken()

                val initialData = basicUseCase.initializeConfigurationAndKeys()
                try {
                    val refreshData = basicUseCase.refreshAccessTokenWithSsoFlow(
                        initialData,
                        scope = IdpScope.Default,
                        ssoToken = ssoToken.token
                    )
                    refreshData.accessToken
                } catch (e: Exception) {
                    Napier.e("Couldn't refresh access token", e)
                    (e as? ApiCallException)?.also {
                        when (it.response.code()) {
                            // 400 returned by redirect call if sso token is not valid anymore
                            400, 401, 403 -> {
                                repository.invalidateSingleSignOnTokenRetainingScope()
                                throw RefreshFlowException(true, ssoToken.scope, e)
                            }
                        }
                    }
                    throw RefreshFlowException(false, null, e)
                }
            } else {
                accToken
            }.also {
                repository.decryptedAccessToken = it
            }
        }

    /**
     * Initial flow fetching the sso & access token requiring the health card to sign the challenge.
     */
    suspend fun authenticationFlowWithHealthCard(
        healthCardCertificate: suspend () -> ByteArray,
        sign: suspend (hash: ByteArray) -> ByteArray
    ) = lock.withLock {
        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val challengeData = basicUseCase.challengeFlow(initialData, scope = IdpScope.Default)
        val basicData = basicUseCase.basicAuthFlow(
            initialData = initialData,
            challengeData = challengeData,
            healthCardCertificate = healthCardCertificate(),
            sign = sign
        )
        repository.setSingleSignOnToken(SingleSignOnToken(basicData.ssoToken))
        repository.decryptedAccessToken = basicData.accessToken
    }
}
