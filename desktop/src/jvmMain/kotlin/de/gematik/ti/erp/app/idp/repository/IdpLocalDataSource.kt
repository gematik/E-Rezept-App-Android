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

package de.gematik.ti.erp.app.idp.repository

import java.time.Instant
import org.bouncycastle.cert.X509CertificateHolder

data class IdpConfiguration(
    val authorizationEndpoint: String,
    val ssoEndpoint: String,
    val tokenEndpoint: String,
    val pairingEndpoint: String,
    val authenticationEndpoint: String,
    val pukIdpEncEndpoint: String,
    val pukIdpSigEndpoint: String,
    val certificate: X509CertificateHolder,
    val expirationTimestamp: Instant,
    val issueTimestamp: Instant
)

data class IdpAuthenticationDataEntity(
    val singleSignOnToken: String? = null,
    val singleSignOnTokenScope: SingleSignOnToken.Scope? = null,

    val healthCardCertificate: ByteArray? = null,
    val aliasOfSecureElementEntry: ByteArray? = null
)

class IdpLocalDataSource {
    private var idpConfiguration: IdpConfiguration? = null
    private var authData: IdpAuthenticationDataEntity? = null

    suspend fun saveIdpInfo(idpConfiguration: IdpConfiguration) {
        this.idpConfiguration = idpConfiguration
    }

    suspend fun loadIdpInfo(): IdpConfiguration? {
        return idpConfiguration
    }

    suspend fun clearIdpInfo() {
        idpConfiguration = null
    }

    suspend fun saveSingleSignOnToken(token: String?, scope: SingleSignOnToken.Scope?) {
        authData = authData?.copy(singleSignOnToken = token, singleSignOnTokenScope = scope)
            ?: IdpAuthenticationDataEntity(singleSignOnToken = token, singleSignOnTokenScope = scope)
    }

    suspend fun saveSingleSignOnToken(token: String?) {
        authData = authData?.copy(singleSignOnToken = token) ?: IdpAuthenticationDataEntity(singleSignOnToken = token)
    }

    suspend fun loadIdpAuthData(): IdpAuthenticationDataEntity {
        return authData ?: IdpAuthenticationDataEntity()
    }

    suspend fun clearIdpAuthData() {
        authData = null
    }
}
